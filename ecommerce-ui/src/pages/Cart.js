import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  TextField,
  CircularProgress,
  Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const RAZORPAY_KEY_ID = 'rzp_test_nwnTt5aqKf3z6f';

const Cart = () => {
  const { cartItems, removeFromCart, updateQuantity, getTotalPrice, clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Function to ensure Razorpay script is loaded
  const ensureRazorpayLoaded = () => {
    return new Promise((resolve, reject) => {
      if (window.Razorpay) {
        resolve();
        return;
      }

      // Check if script already exists
      const existingScript = document.querySelector('script[src*="razorpay"]');
      if (existingScript) {
        // Wait for script to load
        existingScript.onload = () => resolve();
        existingScript.onerror = () => reject(new Error('Failed to load Razorpay script'));
        return;
      }

      // Create and load script
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load Razorpay script'));
      document.head.appendChild(script);
    });
  };

  const handleCheckout = async () => {
    if (!user) {
      navigate('/login');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Ensure Razorpay script is loaded
      console.log('Checking Razorpay availability...');
      try {
        await ensureRazorpayLoaded();
        console.log('Razorpay script is loaded');
      } catch (scriptError) {
        console.error('Failed to load Razorpay script:', scriptError);
        setError('Failed to load payment gateway. Please check your internet connection and try again.');
        setLoading(false);
        return;
      }

      // Double check Razorpay is available
      if (!window.Razorpay || typeof window.Razorpay !== 'function') {
        setError('Razorpay payment gateway is not available. Please refresh the page.');
        setLoading(false);
        return;
      }

      // Step 1: Create order
      console.log('Step 1: Creating order...', { userId: user.id, items: cartItems });
      const orderItems = cartItems.map(item => ({
        productId: item.id,
        quantity: item.quantity,
      }));

      const orderRequest = {
        userId: user.id,
        shippingAddress: 'Default Address',
        items: orderItems,
      };

      let orderResponse;
      try {
        orderResponse = await api.post('/api/orders', orderRequest);
        console.log('Order created:', orderResponse.data);
      } catch (orderError) {
        console.error('Order creation error:', orderError);
        const errorMsg = orderError.response?.data?.message || orderError.response?.data?.error || orderError.message || 'Failed to create order';
        setError(`Order creation failed: ${errorMsg}`);
        setLoading(false);
        return;
      }

      const orderId = orderResponse.data.id;
      const totalAmount = getTotalPrice();

      // Step 2: Create Razorpay order
      console.log('Step 2: Creating Razorpay order...', { orderId, amount: totalAmount });
      let razorpayOrderResponse;
      try {
        razorpayOrderResponse = await api.post(
          `/api/payments/create-order?orderId=${orderId}&amount=${totalAmount}&currency=INR`
        );
        console.log('Razorpay order created:', razorpayOrderResponse.data);
      } catch (razorpayError) {
        console.error('Razorpay order creation error:', razorpayError);
        const errorMsg = razorpayError.response?.data?.error || razorpayError.response?.data?.message || razorpayError.message || 'Failed to create payment order';
        setError(`Payment setup failed: ${errorMsg}. Please try again.`);
        setLoading(false);
        return;
      }

      const razorpayOrder = razorpayOrderResponse.data;

      if (!razorpayOrder.id || !razorpayOrder.amount) {
        setError('Invalid payment order response. Please try again.');
        setLoading(false);
        return;
      }

      // Step 3: Open Razorpay checkout
      console.log('Step 3: Opening Razorpay checkout...', { 
        orderId: razorpayOrder.id, 
        amount: razorpayOrder.amount,
        currency: razorpayOrder.currency,
        razorpayOrderData: razorpayOrder
      });
      
      // Double-check Razorpay is loaded
      if (typeof window.Razorpay === 'undefined') {
        setError('Razorpay payment gateway is not loaded. Please refresh the page and try again.');
        setLoading(false);
        return;
      }
      
      const options = {
        key: RAZORPAY_KEY_ID,
        amount: razorpayOrder.amount, // Amount in paise (already from backend)
        currency: razorpayOrder.currency || 'INR',
        name: 'E-Commerce Store',
        description: `Order #${orderId}`,
        order_id: razorpayOrder.id,
        handler: async function (response) {
          console.log('Payment successful, verifying...', response);
          try {
            // Step 4: Verify payment on server
            const verifyParams = new URLSearchParams({
              orderId: orderId.toString(),
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              amount: totalAmount.toString(),
            });
            const verifyResponse = await api.post(`/api/payments/verify?${verifyParams.toString()}`);
            console.log('Payment verified:', verifyResponse.data);

            if (verifyResponse.data.success) {
              clearCart();
              navigate('/orders');
            } else {
              setError('Payment verification failed. Please contact support.');
              setLoading(false);
            }
          } catch (verifyError) {
            console.error('Payment verification error:', verifyError);
            const errorMsg = verifyError.response?.data?.message || verifyError.message || 'Verification failed';
            setError(`Payment verification failed: ${errorMsg}. Payment ID: ${response.razorpay_payment_id}`);
            setLoading(false);
          }
        },
        prefill: {
          name: user.username || '',
          email: user.email || '',
          contact: '',
        },
        notes: {
          orderId: orderId.toString(),
        },
        theme: {
          color: '#1976d2',
        },
        modal: {
          ondismiss: function() {
            setLoading(false);
            setError('Payment was cancelled');
          },
        },
      };

      try {
        console.log('Initializing Razorpay with options:', options);
        const rzp = new window.Razorpay(options);
        console.log('Razorpay instance created, opening modal...');
        rzp.on('payment.failed', function (response) {
          console.error('Payment failed:', response);
          setError(`Payment failed: ${response.error.description || response.error.reason || 'Unknown error'}`);
          setLoading(false);
        });
        rzp.open();
        console.log('Razorpay modal should be opening now...');
        // Don't set loading to false here - let the handler or error handler do it
      } catch (rzpError) {
        console.error('Razorpay initialization error:', rzpError);
        setError(`Failed to initialize payment: ${rzpError.message || 'Unknown error'}. Please try again.`);
        setLoading(false);
      }

    } catch (error) {
      console.error('Unexpected error during checkout:', error);
      const errorMsg = error.response?.data?.message || error.response?.data?.error || error.message || 'An unexpected error occurred';
      setError(`Checkout failed: ${errorMsg}. Please try again.`);
      setLoading(false);
    }
  };

  if (cartItems.length === 0) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Typography variant="h4" gutterBottom>
          Your Cart is Empty
        </Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom>
        Shopping Cart
      </Typography>
      
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Product</TableCell>
              <TableCell>Price</TableCell>
              <TableCell>Quantity</TableCell>
              <TableCell>Total</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {cartItems.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.name}</TableCell>
                <TableCell>₹{item.price}</TableCell>
                <TableCell>
                  <TextField
                    type="number"
                    value={item.quantity}
                    onChange={(e) => updateQuantity(item.id, parseInt(e.target.value) || 1)}
                    inputProps={{ min: 1 }}
                    sx={{ width: 80 }}
                  />
                </TableCell>
                <TableCell>₹{(item.price * item.quantity).toFixed(2)}</TableCell>
                <TableCell>
                  <IconButton onClick={() => removeFromCart(item.id)} color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ mt: 3 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h5">
            Total: ₹{getTotalPrice().toFixed(2)}
          </Typography>
          <Button 
            variant="contained" 
            size="large" 
            onClick={handleCheckout}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : null}
          >
            {loading ? 'Processing...' : 'Checkout with Razorpay'}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default Cart;

