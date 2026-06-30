import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import {
  Box, Container, Grid, Typography, Button, Chip, Divider,
  TextField, Skeleton, Alert, Breadcrumbs, Link,
} from '@mui/material';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { productApi } from '../api';
import { addToCart } from '../store/cartSlice';

export default function ProductDetail() {
  const { id }     = useParams();
  const navigate   = useNavigate();
  const dispatch   = useDispatch();
  const [product,  setProduct]  = useState(null);
  const [qty,      setQty]      = useState(1);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [added,    setAdded]    = useState(false);

  useEffect(() => {
    productApi.getById(id)
      .then(r => setProduct(r.data))
      .catch(() => setError('Product not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = () => {
    dispatch(addToCart({
      productId:   product.id,
      productName: product.name,
      price:       product.price,
      imageUrl:    product.imageUrl,
      quantity:    qty,
    }));
    setAdded(true);
    setTimeout(() => setAdded(false), 2500);
  };

  if (loading) return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      <Grid container spacing={5}>
        <Grid item xs={12} md={6}><Skeleton variant="rounded" height={450} /></Grid>
        <Grid item xs={12} md={6}><Skeleton variant="rounded" height={350} /></Grid>
      </Grid>
    </Container>
  );

  if (error || !product) return (
    <Container sx={{ py: 8, textAlign: 'center' }}>
      <Typography variant="h5" color="error">{error || 'Product not found'}</Typography>
      <Button onClick={() => navigate('/products')} startIcon={<ArrowBackIcon />} sx={{ mt: 2 }}>
        Back to Products
      </Button>
    </Container>
  );

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      {/* Breadcrumb */}
      <Breadcrumbs sx={{ mb: 3 }}>
        <Link underline="hover" color="inherit" href="/" onClick={e => { e.preventDefault(); navigate('/'); }}>Home</Link>
        <Link underline="hover" color="inherit" href="/products" onClick={e => { e.preventDefault(); navigate('/products'); }}>Products</Link>
        <Typography color="text.primary">{product.name}</Typography>
      </Breadcrumbs>

      <Grid container spacing={6}>
        {/* Image */}
        <Grid item xs={12} md={6}>
          <Box sx={{
            borderRadius: 4, overflow: 'hidden',
            border: '1px solid rgba(0,0,0,0.06)',
            boxShadow: '0 8px 32px rgba(0,0,0,0.08)',
          }}>
            <img
              src={product.imageUrl || `https://picsum.photos/seed/${product.id}/600/500`}
              alt={product.name}
              style={{ width: '100%', objectFit: 'cover', display: 'block' }}
            />
          </Box>
        </Grid>

        {/* Info */}
        <Grid item xs={12} md={6}>
          {product.categoryName && (
            <Chip label={product.categoryName} size="small" color="primary" variant="outlined" sx={{ mb: 2 }} />
          )}

          <Typography variant="h4" fontWeight={800} gutterBottom>{product.name}</Typography>

          <Typography variant="h3" fontWeight={900} gutterBottom
            sx={{ background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                  WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            ${Number(product.price).toFixed(2)}
          </Typography>

          <Box sx={{ display: 'flex', gap: 1, mb: 3 }}>
            {product.stockQuantity > 10 && <Chip label="✓ In Stock" color="success" size="small" />}
            {product.stockQuantity > 0 && product.stockQuantity <= 10 && (
              <Chip label={`Only ${product.stockQuantity} left!`} color="warning" size="small" />
            )}
            {product.stockQuantity === 0 && <Chip label="Out of Stock" color="error" size="small" />}
          </Box>

          <Divider sx={{ mb: 3 }} />

          <Typography color="text.secondary" variant="body1" sx={{ lineHeight: 1.8, mb: 4 }}>
            {product.description || 'No description available for this product.'}
          </Typography>

          {added && <Alert severity="success" sx={{ mb: 3 }}>Added to cart!</Alert>}

          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 3 }}>
            <TextField
              label="Qty" type="number" size="medium"
              value={qty}
              onChange={e => setQty(Math.max(1, Math.min(product.stockQuantity, parseInt(e.target.value) || 1)))}
              inputProps={{ min: 1, max: product.stockQuantity }}
              sx={{ width: 100 }}
            />
            <Button variant="contained" size="large" fullWidth
              startIcon={<AddShoppingCartIcon />}
              disabled={product.stockQuantity === 0}
              onClick={handleAddToCart}
              sx={{
                py: 1.5, borderRadius: 2.5, fontWeight: 700, fontSize: '1rem',
                background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)' },
              }}>
              Add to Cart
            </Button>
          </Box>

          <Button onClick={() => navigate('/cart')} variant="outlined" fullWidth size="large"
            sx={{ borderRadius: 2.5, fontWeight: 600 }}>
            View Cart
          </Button>
        </Grid>
      </Grid>
    </Container>
  );
}
