import React, { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';
import api from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  // Restore user and token from localStorage on mount
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      // Ensure user data is also restored if token exists but user doesn't
      if (!user) {
        const savedUser = localStorage.getItem('user');
        if (savedUser) {
          setUser(JSON.parse(savedUser));
        }
      }
    } else {
      // Clear user if no token
      setUser(null);
      localStorage.removeItem('user');
    }
    setLoading(false);
  }, [token, user]);

  const login = async (username, password) => {
    try {
      // Try direct service call first (bypassing gateway for testing)
      const directApi = axios.create({
        baseURL: 'http://localhost:8081',
        headers: { 'Content-Type': 'application/json' }
      });
      const response = await directApi.post('/api/auth/login', { username, password });
      const { token: newToken, id, username: userUsername, email, role } = response.data;
      
      // Log role received from server for debugging
      console.log('🔐 Login Response:', { username: userUsername, role, email });
      
      const userData = { id, username: userUsername, email, role: role || 'USER' };
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(userData));
      setToken(newToken);
      setUser(userData);
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      
      console.log('✅ User data saved to localStorage:', userData);
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      const errorMessage = error.response?.data?.message || error.response?.data || error.message || 'Login failed. Please check your credentials.';
      return { success: false, error: errorMessage };
    }
  };

  const register = async (userData) => {
    try {
      const response = await api.post('/api/users/auth/register', userData);
      const { token: newToken, id, username, email, role } = response.data;
      
      const savedUserData = { id, username, email, role: role || 'USER' };
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(savedUserData));
      setToken(newToken);
      setUser(savedUserData);
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      
      return { success: true };
    } catch (error) {
      return { success: false, error: error.response?.data || 'Registration failed' };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
    delete api.defaults.headers.common['Authorization'];
  };

  const refreshUser = async () => {
    if (!token) return { success: false };
    try {
      // Try direct API call to user-service
      const directApi = axios.create({
        baseURL: 'http://localhost:8081',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });
      const response = await directApi.get('/api/auth/me');
      const { id, username, email, role } = response.data;
      const userData = { id, username, email, role: role || 'USER' };
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      console.log('✅ Role refreshed:', userData);
      return { success: true, user: userData };
    } catch (error) {
      console.error('Error refreshing user:', error);
      // Fallback: fetch user by username from login
      try {
        const username = user?.username;
        if (username) {
          // Re-login to get fresh role
          return { success: false, needsRelogin: true };
        }
      } catch (e) {
        // Ignore
      }
      return { success: false };
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout, refreshUser, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

