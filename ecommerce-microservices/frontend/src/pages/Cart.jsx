import React, { useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import {
  Box, Container, Grid, Typography, Button, Divider, Paper,
  Alert, CircularProgress, TextField,
} from '@mui/material';
import ShoppingCartOutlinedIcon from '@mui/icons-material/ShoppingCartOutlined';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import CartItem from '../components/CartItem';
import { selectCartTotal, clearCart } from '../store/cartSlice';
import { orderApi } from '../api';

export default function Cart() {
  const dispatch   = useDispatch();
  const navigate   = useNavigate();
  const items      = useSelector(s => s.cart.items);
  const total      = useSelector(selectCartTotal);
  const { isAuthenticated } = useSelector(s => s.auth);

  const [address,  setAddress]  = useState('');
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState(null);

  const handleCheckout = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    if (!address.trim()) { setError('Please enter a shipping address'); return; }

    setLoading(true); setError(null);
    try {
      const payload = {
        items: items.map(i => ({
          productId:   i.productId,
          productName: i.productName,
          quantity:    i.quantity,
          unitPrice:   i.price,
        })),
        shippingAddress: address,
      };
      const res = await orderApi.place(payload);
      dispatch(clearCart());
      navigate(`/orders/${res.data.id}`, { state: { order: res.data } });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to place order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (items.length === 0) return (
    <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
      <ShoppingCartOutlinedIcon sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
      <Typography variant="h5" fontWeight={700} gutterBottom>Your cart is empty</Typography>
      <Typography color="text.secondary" mb={4}>
        Looks like you haven't added any items yet.
      </Typography>
      <Button component={Link} to="/products" variant="contained" size="large"
        sx={{ borderRadius: 3, fontWeight: 700,
              background: 'linear-gradient(135deg, #6d28d9, #2563eb)' }}>
        Start Shopping
      </Button>
    </Container>
  );

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      <Typography variant="h4" fontWeight={800} gutterBottom>Shopping Cart</Typography>

      <Grid container spacing={4}>
        {/* Cart items */}
        <Grid item xs={12} md={7}>
          {items.map(item => <CartItem key={item.productId} item={item} />)}
        </Grid>

        {/* Order Summary */}
        <Grid item xs={12} md={5}>
          <Paper elevation={0} sx={{
            p: 3.5, borderRadius: 4, position: 'sticky', top: 90,
            border: '1px solid rgba(0,0,0,0.08)',
            boxShadow: '0 8px 32px rgba(0,0,0,0.06)',
          }}>
            <Typography variant="h6" fontWeight={800} mb={3}>Order Summary</Typography>

            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography color="text.secondary">Subtotal ({items.length} items)</Typography>
              <Typography fontWeight={700}>${total.toFixed(2)}</Typography>
            </Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1.5 }}>
              <Typography color="text.secondary">Shipping</Typography>
              <Typography color="success.main" fontWeight={600}>FREE</Typography>
            </Box>

            <Divider sx={{ my: 2 }} />

            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
              <Typography fontWeight={800} variant="h6">Total</Typography>
              <Typography fontWeight={900} variant="h5"
                sx={{ background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                      WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                ${total.toFixed(2)}
              </Typography>
            </Box>

            <TextField
              label="Shipping Address" multiline rows={2} fullWidth
              value={address} onChange={e => setAddress(e.target.value)}
              placeholder="Enter your delivery address"
              sx={{ mb: 2 }}
            />

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Button fullWidth variant="contained" size="large"
              endIcon={loading ? null : <ArrowForwardIcon />}
              disabled={loading}
              onClick={handleCheckout}
              sx={{
                py: 1.5, borderRadius: 2.5, fontWeight: 700, fontSize: '1rem',
                background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)' },
              }}>
              {loading ? <CircularProgress size={22} color="inherit" /> : 'Place Order'}
            </Button>

            <Button fullWidth variant="text" component={Link} to="/products" sx={{ mt: 1 }}>
              Continue Shopping
            </Button>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}
