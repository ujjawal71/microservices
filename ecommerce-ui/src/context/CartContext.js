import React, { createContext, useState, useContext, useEffect } from 'react';

const CartContext = createContext();

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

const CART_STORAGE_KEY = 'ecommerce_cart';

export const CartProvider = ({ children }) => {
  // Load cart from localStorage on mount
  const [cartItems, setCartItems] = useState(() => {
    try {
      const savedCart = localStorage.getItem(CART_STORAGE_KEY);
      return savedCart ? JSON.parse(savedCart) : [];
    } catch (error) {
      console.error('Failed to load cart from localStorage:', error);
      return [];
    }
  });

  // Save cart to localStorage whenever it changes
  useEffect(() => {
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cartItems));
    } catch (error) {
      console.error('Failed to save cart to localStorage:', error);
    }
  }, [cartItems]);

  const addToCart = (product) => {
    // Check if product is in stock before adding to cart
    if (product.stockQuantity !== null && product.stockQuantity !== undefined && product.stockQuantity <= 0) {
      alert('This product is out of stock. Cannot add to cart.');
      return;
    }
    
    setCartItems((prevItems) => {
      const existingItem = prevItems.find(item => item.id === product.id);
      if (existingItem) {
        const newQuantity = existingItem.quantity + 1;
        // Check if adding one more would exceed stock
        if (product.stockQuantity !== null && product.stockQuantity !== undefined && newQuantity > product.stockQuantity) {
          alert(`Only ${product.stockQuantity} items available in stock. Cannot add more.`);
          return prevItems;
        }
        return prevItems.map(item =>
          item.id === product.id
            ? { ...item, quantity: newQuantity }
            : item
        );
      }
      return [...prevItems, { ...product, quantity: 1 }];
    });
  };

  const removeFromCart = (productId) => {
    setCartItems((prevItems) => prevItems.filter(item => item.id !== productId));
  };

  const updateQuantity = (productId, quantity, availableStock = null) => {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }
    
    setCartItems((prevItems) => {
      const item = prevItems.find(i => i.id === productId);
      if (!item) return prevItems;
      
      // Check stock limit if available stock is provided
      // availableStock can come from:
      // 1. item.stockQuantity (stored when product was added to cart)
      // 2. availableStock parameter (latest stock from API)
      const stockLimit = availableStock !== null ? availableStock : 
                        (item.stockQuantity !== null && item.stockQuantity !== undefined ? item.stockQuantity : null);
      
      if (stockLimit !== null && quantity > stockLimit) {
        alert(`Only ${stockLimit} items available in stock. Cannot set quantity to ${quantity}.`);
        // Set quantity to max available stock instead of rejecting
        return prevItems.map(i =>
          i.id === productId ? { ...i, quantity: stockLimit } : i
        );
      }
      
      return prevItems.map(i =>
        i.id === productId ? { ...i, quantity } : i
      );
    });
  };

  const clearCart = () => {
    setCartItems([]);
    localStorage.removeItem(CART_STORAGE_KEY);
  };

  const getTotalPrice = () => {
    return cartItems.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  return (
    <CartContext.Provider value={{
      cartItems,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
      getTotalPrice
    }}>
      {children}
    </CartContext.Provider>
  );
};

