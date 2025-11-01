import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Container,
  Typography,
  Button,
  Grid,
  Box,
  TextField,
} from '@mui/material';
import api from '../services/api';
import { useCart } from '../context/CartContext';

const ProductDetail = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const { addToCart } = useCart();

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        setError(null);
        console.log('Fetching product with ID:', id);
        const response = await api.get(`/api/products/${id}`);
        console.log('Product response:', response.data);
        setProduct(response.data);
      } catch (error) {
        console.error('Error fetching product:', {
          message: error.message,
          code: error.code,
          status: error.response?.status,
          data: error.response?.data,
          url: error.config?.url,
        });
        setError(`Failed to load product: ${error.response?.status === 404 ? 'Product not found' : error.message}`);
        setProduct(null);
      } finally {
        setLoading(false);
      }
    };
    if (id) {
      fetchProduct();
    }
  }, [id]);

  const handleAddToCart = () => {
    if (product) {
      for (let i = 0; i < quantity; i++) {
        addToCart(product);
      }
    }
  };

  if (loading) {
    return (
      <Container maxWidth="md" sx={{ mt: 4, mb: 4, textAlign: 'center' }}>
        <Typography variant="h6">Loading...</Typography>
      </Container>
    );
  }

  if (error || !product) {
    return (
      <Container maxWidth="md" sx={{ mt: 4, mb: 4, textAlign: 'center' }}>
        <Typography variant="h5" color="error" gutterBottom>
          {error || 'Product not found'}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          The product you're looking for doesn't exist or couldn't be loaded.
        </Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Grid container spacing={4}>
        <Grid item xs={12} md={6}>
          {product.imageUrl && (
            <Box
              component="img"
              src={product.imageUrl}
              alt={product.name}
              sx={{ width: '100%', height: 'auto' }}
            />
          )}
        </Grid>
        <Grid item xs={12} md={6}>
          <Typography variant="h4" component="h1" gutterBottom>
            {product.name}
          </Typography>
          <Typography variant="h5" color="primary" gutterBottom>
            ${product.price}
          </Typography>
          <Typography variant="body1" paragraph>
            {product.description}
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Category: {product.category}
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Stock: {product.stockQuantity !== null && product.stockQuantity !== undefined ? product.stockQuantity : 'N/A'}
          </Typography>
          {/* Stock Status Display */}
          <Box sx={{ mt: 1, mb: 2 }}>
            {product.stockQuantity !== null && product.stockQuantity !== undefined && (
              <Typography 
                variant="body1" 
                sx={{ 
                  fontWeight: 'bold',
                  color: product.stockQuantity > 0 ? 'success.main' : 'error.main'
                }}
              >
                {product.stockQuantity > 0 ? '✅ In Stock' : '❌ Out of Stock'}
              </Typography>
            )}
          </Box>
          <Box sx={{ mt: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
            <TextField
              type="number"
              label="Quantity"
              value={quantity}
              onChange={(e) => {
                const newQuantity = Math.max(1, parseInt(e.target.value) || 1);
                // Limit quantity to available stock
                const maxQuantity = (product.stockQuantity && product.stockQuantity > 0) ? product.stockQuantity : 1;
                setQuantity(Math.min(newQuantity, maxQuantity));
              }}
              inputProps={{ 
                min: 1, 
                max: product.stockQuantity && product.stockQuantity > 0 ? product.stockQuantity : 1
              }}
              sx={{ width: 100 }}
              disabled={!product.stockQuantity || product.stockQuantity <= 0}
            />
            <Button 
              variant="contained" 
              size="large" 
              onClick={handleAddToCart}
              disabled={!product.stockQuantity || product.stockQuantity <= 0}
            >
              {product.stockQuantity && product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
            </Button>
          </Box>
          {/* Warning message if out of stock */}
          {product.stockQuantity !== null && product.stockQuantity !== undefined && product.stockQuantity <= 0 && (
            <Typography variant="body2" color="error" sx={{ mt: 1 }}>
              This product is currently out of stock. Please check back later.
            </Typography>
          )}
        </Grid>
      </Grid>
    </Container>
  );
};

export default ProductDetail;

