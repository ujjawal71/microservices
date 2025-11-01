import React, { useEffect, useState, useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Button,
  TextField,
  Box,
  CircularProgress,
} from '@mui/material';
import api from '../services/api';

const Products = () => {
  const [products, setProducts] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const pageSize = 20;
  const observerRef = useRef(null);

  const fetchProducts = useCallback(async (pageNum = 0, append = false) => {
    try {
      if (pageNum === 0) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }
      setError(null);
      
      const url = `/api/products?page=${pageNum}&size=${pageSize}`;
      const fullUrl = api.defaults.baseURL + url;
      console.log('Fetching products from:', fullUrl);
      
      const response = await api.get(url, {
        timeout: 30000,
        headers: {
          'Accept': 'application/json',
        },
        validateStatus: function (status) {
          return status < 500; // Resolve only if the status code is less than 500
        }
      });
      
      if (response.status >= 400) {
        throw new Error(`HTTP ${response.status}: ${response.statusText || 'Request failed'}`);
      }
      
      console.log('Response received:', {
        status: response.status,
        dataLength: response.data?.length,
        totalCount: response.headers['x-total-count'],
      });
      
      if (!response.data) {
        throw new Error('No data received from server');
      }
      
      if (!Array.isArray(response.data)) {
        throw new Error('Expected array but received: ' + typeof response.data);
      }
      
      const newProducts = response.data;
      const totalCount = parseInt(response.headers['x-total-count'] || '0', 10);
      
      setProducts(prev => {
        if (append) {
          const updated = [...prev, ...newProducts];
          // Check if there are more products to load
          const currentTotal = updated.length;
          setHasMore(currentTotal < totalCount && newProducts.length === pageSize);
          console.log(`✅ Loaded ${newProducts.length} more products (Total: ${currentTotal}/${totalCount})`);
          return updated;
        } else {
          const currentTotal = newProducts.length;
          setHasMore(currentTotal < totalCount && newProducts.length === pageSize);
          console.log(`✅ Loaded ${newProducts.length} products (Total: ${currentTotal}/${totalCount})`);
          return newProducts;
        }
      });
    } catch (error) {
      console.error('❌ Error fetching products:', {
        message: error.message,
        code: error.code,
        response: error.response?.data,
        status: error.response?.status,
        statusText: error.response?.statusText,
        config: error.config?.url,
        baseURL: error.config?.baseURL,
        fullUrl: error.config ? `${error.config.baseURL}${error.config.url}` : 'N/A',
      });
      
      // Provide more specific error messages
      let errorMessage = 'Failed to load products. ';
      if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
        errorMessage += 'Network error - please check if API Gateway is running and CORS is configured.';
      } else if (error.response) {
        errorMessage += `Server error: ${error.response.status} ${error.response.statusText}`;
      } else {
        errorMessage += error.message || 'Unknown error';
      }
      errorMessage += ' Please check console for details.';
      
      setError(errorMessage);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [pageSize]);
  
  useEffect(() => {
    // Only fetch on initial mount
    fetchProducts(0, false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  
  // Infinite scroll observer
  const lastProductElementRef = useCallback((node) => {
    if (loading || loadingMore) return;
    if (observerRef.current) observerRef.current.disconnect();
    
    observerRef.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore && !loadingMore) {
        setPage(prevPage => {
          const nextPage = prevPage + 1;
          fetchProducts(nextPage, true);
          return nextPage;
        });
      }
    }, {
      threshold: 0.1,
      rootMargin: '100px',
    });
    
    if (node) observerRef.current.observe(node);
  }, [loading, loadingMore, hasMore, fetchProducts]);


  const handleSearch = async () => {
    try {
      setLoading(true);
      setError(null);
      setHasMore(false);
      setPage(0);
      
      if (!searchTerm.trim()) {
        // If search is empty, reload all products
        fetchProducts(0, false);
        return;
      }
      
      const response = await api.get(`/api/products/search?q=${searchTerm}`);
      setProducts(response.data);
      setHasMore(false); // Search results don't paginate
    } catch (error) {
      console.error('Error searching products:', error);
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Products {products.length > 0 && `(${products.length})`}
      </Typography>

      <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
        <TextField
          fullWidth
          label="Search Products"
          variant="outlined"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <Button variant="contained" onClick={handleSearch} disabled={loading}>
          Search
        </Button>
        {searchTerm && (
          <Button variant="outlined" onClick={() => {
            setSearchTerm('');
            setPage(0);
            setHasMore(true);
            fetchProducts(0, false);
          }}>
            Clear
          </Button>
        )}
      </Box>

      {loading && (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography>Loading products...</Typography>
        </Box>
      )}

      {error && (
        <Box sx={{ textAlign: 'center', py: 4, color: 'error.main' }}>
          <Typography>{error}</Typography>
        </Box>
      )}

      {!loading && !error && products.length === 0 && (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <Typography>No products found.</Typography>
        </Box>
      )}

      {!loading && !error && products.length > 0 && (
        <>
          <Grid container spacing={3}>
            {products.map((product, index) => (
          <Grid 
            item 
            xs={12} 
            sm={6} 
            md={4} 
            key={product.id}
            ref={index === products.length - 1 ? lastProductElementRef : null}
          >
            <Card>
              {product.imageUrl && (
                <CardMedia
                  component="img"
                  height="200"
                  image={product.imageUrl}
                  alt={product.name}
                />
              )}
              <CardContent>
                <Typography variant="h6" component="h3" gutterBottom>
                  {product.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" paragraph>
                  {product.description}
                </Typography>
                <Typography variant="h6" color="primary" gutterBottom>
                  ${product.price}
                </Typography>
                <Button
                  variant="contained"
                  fullWidth
                  component={Link}
                  to={`/products/${product.id}`}
                >
                  View Details
                </Button>
              </CardContent>
            </Card>
          </Grid>
            ))}
          </Grid>

          {loadingMore && (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
              <Typography sx={{ ml: 2, alignSelf: 'center' }}>
                Loading more products...
              </Typography>
            </Box>
          )}
          
          {!hasMore && products.length > 0 && (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="body2" color="text.secondary">
                No more products to load
              </Typography>
            </Box>
          )}
        </>
      )}
    </Container>
  );
};

export default Products;

