import React from 'react';
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  Card, CardMedia, CardContent, CardActions,
  Typography, Button, Chip, Box, Tooltip, IconButton,
} from '@mui/material';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { addToCart } from '../store/cartSlice';

export default function ProductCard({ product }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleAddToCart = (e) => {
    e.stopPropagation();
    dispatch(addToCart({
      productId:   product.id,
      productName: product.name,
      price:       product.price,
      imageUrl:    product.imageUrl,
      quantity:    1,
    }));
  };

  return (
    <Card
      elevation={0}
      onClick={() => navigate(`/products/${product.id}`)}
      sx={{
        height: '100%', display: 'flex', flexDirection: 'column',
        cursor: 'pointer', borderRadius: 3,
        border: '1px solid rgba(0,0,0,0.06)',
        transition: 'all 0.25s ease',
        '&:hover': {
          transform: 'translateY(-6px)',
          boxShadow: '0 20px 40px rgba(109, 40, 217, 0.15)',
          border: '1px solid rgba(109, 40, 217, 0.3)',
        },
      }}
    >
      {/* Product Image */}
      <Box sx={{ position: 'relative', overflow: 'hidden' }}>
        <CardMedia
          component="img"
          height="220"
          image={product.imageUrl || `https://picsum.photos/seed/${product.id}/400/220`}
          alt={product.name}
          sx={{ transition: 'transform 0.3s ease',
                '&:hover': { transform: 'scale(1.05)' } }}
        />
        {product.stockQuantity === 0 && (
          <Chip label="Out of Stock" color="error" size="small"
            sx={{ position: 'absolute', top: 12, right: 12 }} />
        )}
        {product.stockQuantity > 0 && product.stockQuantity < 5 && (
          <Chip label={`Only ${product.stockQuantity} left`} color="warning" size="small"
            sx={{ position: 'absolute', top: 12, right: 12 }} />
        )}
      </Box>

      <CardContent sx={{ flex: 1, pb: 1 }}>
        {product.categoryName && (
          <Typography variant="caption" color="text.secondary" textTransform="uppercase"
            letterSpacing={1} fontWeight={600}>
            {product.categoryName}
          </Typography>
        )}
        <Typography variant="h6" fontWeight={700} gutterBottom
          sx={{ overflow: 'hidden', textOverflow: 'ellipsis',
                display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
          {product.name}
        </Typography>
        <Typography variant="h5" fontWeight={800}
          sx={{ background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          ${Number(product.price).toFixed(2)}
        </Typography>
      </CardContent>

      <CardActions sx={{ px: 2, pb: 2, gap: 1 }}>
        <Button
          fullWidth
          variant="contained"
          startIcon={<AddShoppingCartIcon />}
          disabled={product.stockQuantity === 0}
          onClick={handleAddToCart}
          sx={{
            textTransform: 'none', borderRadius: 2, fontWeight: 600,
            background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
            '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)' },
          }}
        >
          Add to Cart
        </Button>
        <Tooltip title="View Details">
          <IconButton size="small" sx={{ border: '1px solid rgba(0,0,0,0.12)', borderRadius: 2 }}
            onClick={(e) => { e.stopPropagation(); navigate(`/products/${product.id}`); }}>
            <VisibilityIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </CardActions>
    </Card>
  );
}
