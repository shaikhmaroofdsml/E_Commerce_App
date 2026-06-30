import React from 'react';
import { useDispatch } from 'react-redux';
import { Box, Typography, IconButton, Button, Avatar } from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import { updateQuantity, removeFromCart } from '../store/cartSlice';

export default function CartItem({ item }) {
  const dispatch = useDispatch();

  return (
    <Box sx={{
      display: 'flex', alignItems: 'center', gap: 2, p: 2,
      borderRadius: 3, border: '1px solid rgba(0,0,0,0.08)',
      mb: 2, bgcolor: 'white',
      transition: 'box-shadow 0.2s',
      '&:hover': { boxShadow: '0 4px 16px rgba(0,0,0,0.08)' },
    }}>
      {/* Image */}
      <Avatar
        src={item.imageUrl || `https://picsum.photos/seed/${item.productId}/80`}
        variant="rounded"
        sx={{ width: 72, height: 72 }}
      />

      {/* Info */}
      <Box flex={1}>
        <Typography fontWeight={700} variant="body1">{item.productName}</Typography>
        <Typography color="primary" fontWeight={700} variant="body2">
          ${Number(item.price).toFixed(2)} each
        </Typography>
      </Box>

      {/* Quantity controls */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5,
                 border: '1px solid rgba(0,0,0,0.12)', borderRadius: 2, px: 1 }}>
        <IconButton size="small"
          onClick={() => dispatch(updateQuantity({ productId: item.productId, quantity: item.quantity - 1 }))}>
          <RemoveIcon fontSize="small" />
        </IconButton>
        <Typography fontWeight={700} sx={{ minWidth: 28, textAlign: 'center' }}>
          {item.quantity}
        </Typography>
        <IconButton size="small"
          onClick={() => dispatch(updateQuantity({ productId: item.productId, quantity: item.quantity + 1 }))}>
          <AddIcon fontSize="small" />
        </IconButton>
      </Box>

      {/* Subtotal */}
      <Typography fontWeight={800} sx={{ minWidth: 80, textAlign: 'right' }}>
        ${(item.price * item.quantity).toFixed(2)}
      </Typography>

      {/* Remove */}
      <IconButton
        onClick={() => dispatch(removeFromCart(item.productId))}
        sx={{ color: 'error.main' }}
      >
        <DeleteOutlineIcon />
      </IconButton>
    </Box>
  );
}
