import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Container, Typography, Box, Paper, Chip, Divider,
  Skeleton, Button, Pagination,
} from '@mui/material';
import ReceiptLongIcon from '@mui/icons-material/ReceiptLong';
import { orderApi } from '../api';

const STATUS_COLOR = {
  PENDING:        'warning',
  CONFIRMED:      'info',
  PROCESSING:     'info',
  SHIPPED:        'primary',
  DELIVERED:      'success',
  CANCELLED:      'default',
  PAYMENT_FAILED: 'error',
};

export default function Orders() {
  const [orders,     setOrders]     = useState([]);
  const [loading,    setLoading]    = useState(true);
  const [page,       setPage]       = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    orderApi.getMyOrders({ page, size: 10 })
      .then(r => {
        setOrders(r.data.content || []);
        setTotalPages(r.data.totalPages || 0);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [page]);

  if (!loading && orders.length === 0) return (
    <Container maxWidth="sm" sx={{ py: 10, textAlign: 'center' }}>
      <ReceiptLongIcon sx={{ fontSize: 80, color: 'text.disabled', mb: 2 }} />
      <Typography variant="h5" fontWeight={700} gutterBottom>No orders yet</Typography>
      <Typography color="text.secondary" mb={4}>Start shopping to place your first order!</Typography>
      <Button component={Link} to="/products" variant="contained"
        sx={{ borderRadius: 3, fontWeight: 700,
              background: 'linear-gradient(135deg, #6d28d9, #2563eb)' }}>
        Shop Now
      </Button>
    </Container>
  );

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Typography variant="h4" fontWeight={800} gutterBottom>My Orders</Typography>

      {loading
        ? Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} height={160} sx={{ mb: 2, borderRadius: 3 }} />)
        : orders.map(order => (
          <Paper key={order.id} elevation={0} sx={{
            p: 3, mb: 3, borderRadius: 3,
            border: '1px solid rgba(0,0,0,0.07)',
            '&:hover': { boxShadow: '0 6px 24px rgba(0,0,0,0.08)' },
            transition: 'box-shadow 0.2s',
          }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1, mb: 2 }}>
              <Box>
                <Typography fontWeight={800}>Order #{order.id}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {order.trackingNumber} • {new Date(order.createdAt).toLocaleDateString()}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
                <Chip label={order.status} color={STATUS_COLOR[order.status] || 'default'} size="small" />
              </Box>
            </Box>

            <Divider sx={{ mb: 2 }} />

            {/* Items summary */}
            {order.items?.slice(0, 3).map(item => (
              <Box key={item.id} sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography variant="body2">{item.productName} × {item.quantity}</Typography>
                <Typography variant="body2" fontWeight={600}>${item.subtotal?.toFixed(2)}</Typography>
              </Box>
            ))}
            {order.items?.length > 3 && (
              <Typography variant="body2" color="text.secondary">
                +{order.items.length - 3} more items
              </Typography>
            )}

            <Divider sx={{ my: 2 }} />
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography fontWeight={800} variant="h6">
                Total: ${Number(order.totalAmount).toFixed(2)}
              </Typography>
              <Button component={Link} to={`/orders/${order.id}`} size="small" variant="outlined"
                sx={{ borderRadius: 2 }}>
                View Details
              </Button>
            </Box>
          </Paper>
        ))
      }

      {totalPages > 1 && (
        <Box display="flex" justifyContent="center" mt={3}>
          <Pagination count={totalPages} page={page + 1}
            onChange={(_, v) => setPage(v - 1)} color="primary" />
        </Box>
      )}
    </Container>
  );
}
