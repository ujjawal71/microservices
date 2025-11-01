import axios from 'axios';

// Use relative URLs to leverage the proxy in package.json
// This avoids CORS issues since requests go through the React dev server proxy
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '', // Empty string = relative URLs (uses proxy)
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 60000, // 60 second timeout (increased for order creation which may call multiple services)
});

// Add request interceptor for logging
api.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
    return config;
  },
  (error) => {
    console.error('❌ Request Error:', error);
    return Promise.reject(error);
  }
);

// Add response interceptor for logging
api.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    // Enhanced error logging for network issues
    if (error.code === 'ERR_NETWORK' || error.message === 'Network Error') {
      console.error('❌ Network Error Details:', {
        message: error.message,
        code: error.code,
        config: {
          method: error.config?.method,
          url: error.config?.url,
          baseURL: error.config?.baseURL,
          fullUrl: error.config ? `${error.config.baseURL}${error.config.url}` : 'N/A',
          timeout: error.config?.timeout,
        },
        note: 'This usually means CORS is blocking the request or the server is unreachable',
      });
    } else {
      console.error('❌ Response Error:', {
        message: error.message,
        code: error.code,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        headers: error.response?.headers,
        url: error.config?.url,
        baseURL: error.config?.baseURL,
      });
    }
    return Promise.reject(error);
  }
);

export default api;

