import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Box, Container, Grid, Typography, TextField, Select, MenuItem,
  FormControl, InputLabel, Pagination, InputAdornment, Skeleton, Chip,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ProductCard from '../components/ProductCard';
import { productApi, categoryApi } from '../api';

export default function Products() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [products,   setProducts]   = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading,    setLoading]    = useState(true);
  const [totalPages, setTotalPages] = useState(0);

  const search     = searchParams.get('search')     || '';
  const categoryId = searchParams.get('categoryId') || '';
  const page       = parseInt(searchParams.get('page') || '0');
  const sortBy     = searchParams.get('sortBy')     || 'createdAt';
  const direction  = searchParams.get('direction')  || 'DESC';

  useEffect(() => {
    categoryApi.getAll().then(r => setCategories(r.data || [])).catch(console.error);
  }, []);

  useEffect(() => {
    setLoading(true);
    const params = { page, size: 12, sortBy, direction };
    if (search)     params.search     = search;
    if (categoryId) params.categoryId = categoryId;

    productApi.getAll(params)
      .then(r => {
        setProducts(r.data.content || []);
        setTotalPages(r.data.totalPages || 0);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [search, categoryId, page, sortBy, direction]);

  const update = (key, val) => {
    const p = new URLSearchParams(searchParams);
    p.set(key, val);
    if (key !== 'page') p.set('page', '0');
    setSearchParams(p);
  };

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      <Typography variant="h4" fontWeight={800} gutterBottom>Products</Typography>

      {/* Filters */}
      <Box sx={{
        display: 'flex', gap: 2, flexWrap: 'wrap', mb: 4, p: 3,
        bgcolor: 'white', borderRadius: 3, border: '1px solid rgba(0,0,0,0.06)',
        boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
      }}>
        <TextField size="small" placeholder="Search products…" value={search}
          onChange={e => update('search', e.target.value)}
          sx={{ flex: 1, minWidth: 200 }}
          InputProps={{ startAdornment: <InputAdornment position="start"><SearchIcon /></InputAdornment> }}
        />

        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Category</InputLabel>
          <Select value={categoryId} label="Category" onChange={e => update('categoryId', e.target.value)}>
            <MenuItem value="">All Categories</MenuItem>
            {categories.map(c => <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>)}
          </Select>
        </FormControl>

        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Sort By</InputLabel>
          <Select value={`${sortBy}_${direction}`} label="Sort By"
            onChange={e => { const [s, d] = e.target.value.split('_'); update('sortBy', s); update('direction', d); }}>
            <MenuItem value="createdAt_DESC">Newest First</MenuItem>
            <MenuItem value="price_ASC">Price: Low to High</MenuItem>
            <MenuItem value="price_DESC">Price: High to Low</MenuItem>
            <MenuItem value="name_ASC">Name: A–Z</MenuItem>
          </Select>
        </FormControl>

        {(search || categoryId) && (
          <Chip label="Clear Filters" onDelete={() => setSearchParams({})} color="primary" variant="outlined" />
        )}
      </Box>

      {/* Results count */}
      {!loading && (
        <Typography color="text.secondary" mb={3}>
          Showing {products.length} products
          {search ? ` for "${search}"` : ''}
        </Typography>
      )}

      {/* Product Grid */}
      <Grid container spacing={3}>
        {loading
          ? Array.from({ length: 12 }).map((_, i) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={i}>
                <Skeleton variant="rounded" height={380} animation="wave" />
              </Grid>
            ))
          : products.length > 0
            ? products.map(p => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={p.id}>
                  <ProductCard product={p} />
                </Grid>
              ))
            : (
              <Grid item xs={12}>
                <Box textAlign="center" py={8}>
                  <Typography variant="h5" color="text.secondary" gutterBottom>No products found</Typography>
                  <Typography color="text.disabled">Try changing your search or filters</Typography>
                </Box>
              </Grid>
            )
        }
      </Grid>

      {/* Pagination */}
      {totalPages > 1 && (
        <Box display="flex" justifyContent="center" mt={5}>
          <Pagination count={totalPages} page={page + 1}
            onChange={(_, v) => update('page', v - 1)}
            color="primary" size="large" />
        </Box>
      )}
    </Container>
  );
}
