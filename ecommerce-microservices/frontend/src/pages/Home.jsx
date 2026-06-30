import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Box, Typography, Button, Container, Grid, Chip, Skeleton } from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import ProductCard from '../components/ProductCard';
import { productApi, categoryApi } from '../api';

export default function Home() {
  const [products,   setProducts]   = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading,    setLoading]    = useState(true);

  useEffect(() => {
    Promise.all([
      productApi.getAll({ page: 0, size: 8, sortBy: 'createdAt', direction: 'DESC' }),
      categoryApi.getAll(),
    ]).then(([pRes, cRes]) => {
      setProducts(pRes.data.content || []);
      setCategories(cRes.data || []);
    }).catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  return (
    <Box>
      {/* ── Hero Section ──────────────────────────────────────────────────── */}
      <Box sx={{
        background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%)',
        minHeight: '70vh', display: 'flex', alignItems: 'center',
        position: 'relative', overflow: 'hidden',
      }}>
        {/* Decorative orbs */}
        {[
          { top: '10%', left: '5%',  size: 300, color: 'rgba(109,40,217,0.15)' },
          { top: '50%', right: '5%', size: 400, color: 'rgba(37,99,235,0.12)'  },
          { bottom: '5%', left:'40%',size: 200, color: 'rgba(167,139,250,0.1)' },
        ].map((orb, i) => (
          <Box key={i} sx={{
            position: 'absolute', borderRadius: '50%',
            width: orb.size, height: orb.size,
            bgcolor: orb.color, filter: 'blur(60px)',
            top: orb.top, left: orb.left, right: orb.right, bottom: orb.bottom,
          }} />
        ))}

        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <Box maxWidth={640}>
            <Chip label="🚀 Enterprise-Grade Shopping" size="small"
              sx={{ mb: 3, bgcolor: 'rgba(109,40,217,0.3)', color: '#a78bfa',
                    border: '1px solid rgba(167,139,250,0.3)' }} />
            <Typography variant="h2" fontWeight={800} color="white" gutterBottom
              sx={{ fontSize: { xs: '2.5rem', md: '3.5rem' }, lineHeight: 1.2 }}>
              Shop Smarter,{' '}
              <Box component="span"
                sx={{ background: 'linear-gradient(90deg, #a78bfa, #60a5fa)',
                      WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                Live Better
              </Box>
            </Typography>
            <Typography variant="h6" color="rgba(255,255,255,0.65)" mb={4} fontWeight={400}>
              Discover premium products with blazing-fast delivery, top-tier security,
              and a shopping experience you'll love.
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Button component={Link} to="/products" variant="contained" size="large"
                endIcon={<ArrowForwardIcon />}
                sx={{ borderRadius: 3, px: 4, fontWeight: 700,
                      background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                      '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)',
                                   transform: 'translateY(-2px)', boxShadow: '0 8px 20px rgba(109,40,217,0.4)' },
                      transition: 'all 0.2s' }}>
                Shop Now
              </Button>
              <Button component={Link} to="/register" variant="outlined" size="large"
                sx={{ borderRadius: 3, px: 4, color: 'white',
                      border: '1px solid rgba(255,255,255,0.3)',
                      '&:hover': { border: '1px solid rgba(255,255,255,0.6)',
                                   bgcolor: 'rgba(255,255,255,0.08)' } }}>
                Create Account
              </Button>
            </Box>
          </Box>
        </Container>
      </Box>

      {/* ── Categories Row ────────────────────────────────────────────────── */}
      {categories.length > 0 && (
        <Box sx={{ bgcolor: '#f8faff', py: 3, borderBottom: '1px solid rgba(0,0,0,0.06)' }}>
          <Container maxWidth="lg">
            <Box sx={{ display: 'flex', gap: 1.5, flexWrap: 'wrap', alignItems: 'center' }}>
              <Typography variant="body2" color="text.secondary" fontWeight={600} mr={1}>
                Browse:
              </Typography>
              {categories.map(cat => (
                <Chip key={cat.id} label={cat.name} clickable
                  component={Link} to={`/products?categoryId=${cat.id}`}
                  sx={{ fontWeight: 600, '&:hover': { bgcolor: 'primary.main', color: 'white' } }}
                />
              ))}
            </Box>
          </Container>
        </Box>
      )}

      {/* ── Featured Products ─────────────────────────────────────────────── */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 5 }}>
          <Box>
            <Typography variant="overline" color="primary" fontWeight={700} letterSpacing={2}>
              Featured
            </Typography>
            <Typography variant="h4" fontWeight={800}>Latest Products</Typography>
          </Box>
          <Button component={Link} to="/products" endIcon={<ArrowForwardIcon />}
            sx={{ fontWeight: 600 }}>
            View All
          </Button>
        </Box>

        <Grid container spacing={3}>
          {loading
            ? Array.from({ length: 8 }).map((_, i) => (
                <Grid item xs={12} sm={6} md={3} key={i}>
                  <Skeleton variant="rounded" height={380} animation="wave" />
                </Grid>
              ))
            : products.map(product => (
                <Grid item xs={12} sm={6} md={3} key={product.id}>
                  <ProductCard product={product} />
                </Grid>
              ))
          }
        </Grid>
      </Container>

      {/* ── Features Strip ────────────────────────────────────────────────── */}
      <Box sx={{ bgcolor: 'linear-gradient(135deg, #6d28d9, #2563eb)', py: 6 }}>
        <Container maxWidth="lg">
          <Grid container spacing={4}>
            {[
              { icon: '🚚', title: 'Fast Delivery',     desc: 'Orders shipped within 24 hours' },
              { icon: '🔒', title: 'Secure Payments',   desc: 'RS256 JWT + BCrypt encrypted'   },
              { icon: '↩️', title: 'Easy Returns',      desc: '30-day hassle-free returns'     },
              { icon: '💬', title: '24/7 Support',      desc: 'Always here to help you'        },
            ].map(f => (
              <Grid item xs={12} sm={6} md={3} key={f.title}>
                <Box textAlign="center">
                  <Typography fontSize={40} mb={1}>{f.icon}</Typography>
                  <Typography fontWeight={700} variant="h6" gutterBottom>{f.title}</Typography>
                  <Typography color="text.secondary">{f.desc}</Typography>
                </Box>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>
    </Box>
  );
}
