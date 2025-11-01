import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Paper,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Alert,
  Chip,
  CircularProgress,
  Pagination,
  TablePagination,
} from '@mui/material';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const ORDER_STATUSES = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

const AdminPanel = () => {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState({});
  const [refreshing, setRefreshing] = useState(false);
  const [paymentsMap, setPaymentsMap] = useState({});
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  useEffect(() => {
    if (user && user.role === 'ADMIN') {
      fetchOrders();
    } else if (user && user.username === 'user41') {
      // Auto-refresh role if user41 (might be stale cache)
      refreshUser().then(() => {
        setLoading(false);
      });
    } else {
      setLoading(false);
    }
  }, [user]);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get('/api/orders/all');
      const ordersData = response.data;
      setOrders(ordersData);
      
      // Fetch payment information for each order
      const paymentsPromises = ordersData.map(async (order) => {
        try {
          const paymentResponse = await api.get(`/api/payments/order/${order.id}`);
          return { orderId: order.id, payments: paymentResponse.data };
        } catch (error) {
          console.error(`Error fetching payment for order ${order.id}:`, error);
          return { orderId: order.id, payments: [] };
        }
      });
      
      const paymentsResults = await Promise.all(paymentsPromises);
      const paymentsMapData = {};
      paymentsResults.forEach(({ orderId, payments }) => {
        paymentsMapData[orderId] = payments;
      });
      setPaymentsMap(paymentsMapData);
    } catch (error) {
      console.error('Error fetching orders:', error);
      setError('Failed to load orders. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      setUpdating(prev => ({ ...prev, [orderId]: true }));
      const response = await api.put(`/api/orders/${orderId}/status?status=${newStatus}`);
      
      // Update local state
      setOrders(prevOrders => 
        prevOrders.map(order => 
          order.id === orderId ? response.data : order
        )
      );
      
      console.log(`Order ${orderId} status updated to ${newStatus}`);
    } catch (error) {
      console.error('Error updating order status:', error);
      alert(`Failed to update order status: ${error.response?.data?.message || error.message}`);
    } finally {
      setUpdating(prev => ({ ...prev, [orderId]: false }));
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'CONFIRMED':
        return 'info';
      case 'SHIPPED':
        return 'primary';
      case 'DELIVERED':
        return 'success';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  if (!user) {
    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Paper elevation={3} sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h4" component="h1" gutterBottom>
            Admin Panel
          </Typography>
          <Alert severity="warning" sx={{ mb: 3 }}>
            Please login to access admin panel
          </Alert>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
            <Button
              variant="contained"
              size="large"
              component={Link}
              to="/login?redirect=/admin"
            >
              Admin Login
            </Button>
            <Button
              variant="outlined"
              size="large"
              component={Link}
              to="/"
            >
              Go to Home
            </Button>
          </Box>
        </Paper>
      </Container>
    );
  }

  if (user.role !== 'ADMIN') {
    const handleRefresh = async () => {
      setRefreshing(true);
      const result = await refreshUser();
      if (result.success) {
        // Reload page to update state
        window.location.reload();
      } else {
        // If refresh fails, clear cache and redirect to login
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = '/login?redirect=/admin';
      }
      setRefreshing(false);
    };

    return (
      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Paper elevation={3} sx={{ p: 4 }}>
          <Alert severity="error" sx={{ mb: 2 }}>
            Access Denied. You need ADMIN role to access this panel.
            <br />
            <Typography variant="body2" sx={{ mt: 1 }}>
              Current role: {user.role || 'USER'}
            </Typography>
          </Alert>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, alignItems: 'center', mt: 2 }}>
            <Button
              variant="contained"
              color="error"
              size="large"
              onClick={() => {
                // Clear ALL storage
                localStorage.clear();
                sessionStorage.clear();
                // Redirect to login
                window.location.href = '/login?redirect=/admin';
              }}
            >
              🗑️ Clear Cache & Go to Login
            </Button>
            <Button
              variant="outlined"
              onClick={handleRefresh}
              disabled={refreshing}
            >
              {refreshing ? 'Refreshing...' : 'Try Refresh Role (May Not Work)'}
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 2, textAlign: 'center' }}>
            <strong>⚠️ IMPORTANT:</strong> The role is cached in your browser. You MUST logout and login again to refresh it.
            <br />
            <strong>Quick Fix:</strong> Open browser console (F12), run: <code>localStorage.clear(); location.reload();</code>
          </Typography>
        </Paper>
      </Container>
    );
  }

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ mt: 4, textAlign: 'center' }}>
        <CircularProgress />
        <Typography variant="h6" sx={{ mt: 2 }}>Loading orders...</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Admin Panel - Order Management
        </Typography>
        <Button variant="outlined" onClick={fetchOrders}>
          Refresh
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {orders.length === 0 ? (
        <Paper sx={{ p: 3, textAlign: 'center' }}>
          <Typography variant="body1">No orders found.</Typography>
        </Paper>
      ) : (
        <>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Order ID</TableCell>
                  <TableCell>User ID</TableCell>
                  <TableCell>Date</TableCell>
                  <TableCell>Total Amount</TableCell>
                  <TableCell>Order Status</TableCell>
                  <TableCell>Payment ID</TableCell>
                  <TableCell>Razorpay Order ID</TableCell>
                  <TableCell>Payment Status</TableCell>
                  <TableCell>Change Status</TableCell>
                  <TableCell>Shipping Address</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {orders
                  .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                  .map((order) => {
                  const payments = paymentsMap[order.id] || [];
                  const payment = payments.length > 0 ? payments[0] : null; // Get first payment if exists
                  
                  const getPaymentStatusColor = (status) => {
                    switch (status) {
                      case 'COMPLETED':
                        return 'success';
                      case 'PENDING':
                        return 'warning';
                      case 'FAILED':
                        return 'error';
                      default:
                        return 'default';
                    }
                  };
                  
                  return (
                    <TableRow key={order.id}>
                      <TableCell>{order.id}</TableCell>
                      <TableCell>{order.userId}</TableCell>
                      <TableCell>
                        {order.orderDate 
                          ? new Date(order.orderDate).toLocaleString()
                          : 'N/A'}
                      </TableCell>
                      <TableCell>₹{order.totalAmount?.toFixed(2) || '0.00'}</TableCell>
                      <TableCell>
                        <Chip 
                          label={order.status} 
                          color={getStatusColor(order.status)} 
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        {payment?.razorpayPaymentId ? (
                          <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>
                            {payment.razorpayPaymentId.substring(0, 12)}...
                          </Typography>
                        ) : (
                          <Typography variant="body2" color="text.secondary">
                            No payment
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        {payment?.razorpayOrderId ? (
                          <Typography variant="body2" sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>
                            {payment.razorpayOrderId.substring(0, 12)}...
                          </Typography>
                        ) : (
                          <Typography variant="body2" color="text.secondary">
                            No payment
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        {payment ? (
                          <Chip 
                            label={payment.status} 
                            color={getPaymentStatusColor(payment.status)} 
                            size="small"
                          />
                        ) : (
                          <Typography variant="body2" color="text.secondary">
                            No payment
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <FormControl size="small" sx={{ minWidth: 150 }}>
                          <Select
                            value={order.status || 'PENDING'}
                            onChange={(e) => handleStatusChange(order.id, e.target.value)}
                            disabled={updating[order.id]}
                          >
                            {ORDER_STATUSES.map((status) => (
                              <MenuItem key={status} value={status}>
                                {status}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                        {updating[order.id] && (
                          <CircularProgress size={20} sx={{ ml: 1 }} />
                        )}
                      </TableCell>
                      <TableCell>{order.shippingAddress || 'N/A'}</TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
            <Typography variant="body2" color="text.secondary">
              Showing {page * rowsPerPage + 1} to {Math.min((page + 1) * rowsPerPage, orders.length)} of {orders.length} orders
            </Typography>
            <TablePagination
              component="div"
              count={orders.length}
              page={page}
              onPageChange={(event, newPage) => setPage(newPage)}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={(event) => {
                setRowsPerPage(parseInt(event.target.value, 10));
                setPage(0);
              }}
              rowsPerPageOptions={[5, 10, 25, 50, 100]}
            />
          </Box>
        </>
      )}
    </Container>
  );
};

export default AdminPanel;

