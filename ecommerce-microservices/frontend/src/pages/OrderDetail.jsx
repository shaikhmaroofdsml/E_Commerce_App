import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container, Typography, Box, Paper, Chip, Divider,
  Button, Grid, Skeleton, Alert,
} from '@mui/material';
import LocalShippingIcon from '@mui/icons-material/LocalShipping';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { orderApi } from '../api';

const STATUS_COLOR = {
  PENDING: 'warning', CONFIRMED: 'info', PROCESSING: 'info',
  SHIPPED: 'primary', DELIVERED: 'success', CANCELLED: 'default', PAYMENT_FAILED: 'error',
};

export default function OrderDetail() {
  const { id }  = useParams();
  const navigate = useNavigate();
  const [order,   setOrder]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    orderApi.getById(id)
      .then(r => setOrder(r.data))
      .catch(e => setError(e.response?.data?.message || 'Order not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleCancel = async () => {
    setCancelling(true);
    try {
      const res = await orderApi.cancel(id);
      setOrder(res.data);
    } catch (e) {
      setError(e.response?.data?.message || 'Cannot cancel this order');
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <Container sx={{ py: 5 }}><Skeleton height={400} variant="rounded" /></Container>;

  if (error || !order) return (
    <Container sx={{ py: 5, textAlign: 'center' }}>
      <Alert severity="error">{error || 'Order not found'}</Alert>
      <Button onClick={() => navigate('/orders')} startIcon={<ArrowBackIcon />} sx={{ mt: 2 }}>
        Back to Orders
      </Button>
    </Container>
  );

  const canCancel = !['SHIPPED', 'DELIVERED', 'CANCELLED', 'PAYMENT_FAILED'].includes(order.status);

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Button onClick={() => navigate('/orders')} startIcon={<ArrowBackIcon />} sx={{ mb: 3 }}>
        Back to Orders
      </Button>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800}>Order #{order.id}</Typography>
          <Typography color="text.secondary">
            Placed on {new Date(order.createdAt).toLocaleString()}
          </Typography>
        </Box>
        <Chip label={order.status} color={STATUS_COLOR[order.status] || 'default'} size="medium" />
      </Box>

      {/* Tracking */}
      <Paper elevation={0} sx={{ p: 3, mb: 3, borderRadius: 3, bgcolor: '#f8faff', border: '1px solid rgba(37,99,235,0.15)' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
          <LocalShippingIcon color="primary" />
          <Typography fontWeight={700}>Tracking Number</Typography>
        </Box>
        <Typography variant="h6" color="primary" fontWeight={800}>{order.trackingNumber}</Typography>
        <Typography variant="body2" color="text.secondary" mt={1}>
          Shipping to: {order.shippingAddress}
        </Typography>
      </Paper>

      {/* Items */}
      <Paper elevation={0} sx={{ p: 3, mb: 3, borderRadius: 3, border: '1px solid rgba(0,0,0,0.07)' }}>
        <Typography fontWeight={800} mb={2}>Order Items</Typography>
        {order.items?.map((item, i) => (
          <Box key={item.id}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', py: 1.5 }}>
              <Box>
                <Typography fontWeight={600}>{item.productName}</Typography>
                <Typography variant="body2" color="text.secondary">
                  ${Number(item.unitPrice).toFixed(2)} × {item.quantity}
                </Typography>
              </Box>
              <Typography fontWeight={700}>${Number(item.subtotal).toFixed(2)}</Typography>
            </Box>
            {i < order.items.length - 1 && <Divider />}
          </Box>
        ))}
        <Divider sx={{ my: 2 }} />
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography fontWeight={800} variant="h6">Total</Typography>
          <Typography fontWeight={900} variant="h5"
            sx={{ background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                  WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            ${Number(order.totalAmount).toFixed(2)}
          </Typography>
        </Box>
      </Paper>

      {canCancel && (
        <Button variant="outlined" color="error" onClick={handleCancel} disabled={cancelling}
          sx={{ borderRadius: 2, fontWeight: 600 }}>
          {cancelling ? 'Cancelling…' : 'Cancel Order'}
        </Button>
      )}
    </Container>
  );
}
