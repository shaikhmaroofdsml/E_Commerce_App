import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import {
  AppBar, Toolbar, Typography, IconButton, Badge, Button,
  Avatar, Menu, MenuItem, Box, InputBase, Tooltip,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import SearchIcon from '@mui/icons-material/Search';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import DashboardIcon from '@mui/icons-material/Dashboard';
import { logout } from '../store/authSlice';
import { selectCartItemCount } from '../store/cartSlice';

export default function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector(s => s.auth);
  const cartCount = useSelector(selectCartItemCount);

  const [anchorEl, setAnchorEl] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const handleSearch = (e) => {
    if (e.key === 'Enter' && searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    setAnchorEl(null);
    navigate('/');
  };

  return (
    <AppBar position="sticky" elevation={0} sx={{
      background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
      borderBottom: '1px solid rgba(255,255,255,0.08)',
    }}>
      <Toolbar sx={{ gap: 2, py: 0.5 }}>
        {/* Logo */}
        <Typography
          component={Link} to="/"
          variant="h5" fontWeight={800}
          sx={{ textDecoration: 'none', color: 'white', flexShrink: 0,
                background: 'linear-gradient(90deg, #a78bfa, #60a5fa)',
                WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}
        >
          ShopEnterprise
        </Typography>

        {/* Search */}
        <Box sx={{
          display: 'flex', alignItems: 'center',
          bgcolor: 'rgba(255,255,255,0.08)', borderRadius: 3,
          px: 2, flex: 1, maxWidth: 480,
          border: '1px solid rgba(255,255,255,0.12)',
          '&:hover': { bgcolor: 'rgba(255,255,255,0.12)' },
        }}>
          <SearchIcon sx={{ color: 'rgba(255,255,255,0.5)', mr: 1 }} />
          <InputBase
            placeholder="Search products..."
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
            onKeyDown={handleSearch}
            sx={{ color: 'white', flex: 1, '& ::placeholder': { color: 'rgba(255,255,255,0.4)' } }}
          />
        </Box>

        <Box sx={{ flex: 1 }} />

        {/* Navigation links */}
        <Button component={Link} to="/products" sx={{ color: 'rgba(255,255,255,0.85)', textTransform: 'none' }}>
          Products
        </Button>

        {/* Cart */}
        <Tooltip title="Cart">
          <IconButton component={Link} to="/cart" sx={{ color: 'white' }}>
            <Badge badgeContent={cartCount} color="error">
              <ShoppingCartIcon />
            </Badge>
          </IconButton>
        </Tooltip>

        {/* Auth */}
        {isAuthenticated ? (
          <>
            {user?.role === 'ROLE_ADMIN' && (
              <Tooltip title="Admin Dashboard">
                <IconButton component={Link} to="/admin" sx={{ color: '#a78bfa' }}>
                  <DashboardIcon />
                </IconButton>
              </Tooltip>
            )}
            <Tooltip title={user?.firstName}>
              <IconButton onClick={e => setAnchorEl(e.currentTarget)} sx={{ p: 0 }}>
                <Avatar sx={{ bgcolor: '#6d28d9', width: 36, height: 36, fontSize: 14 }}>
                  {user?.firstName?.[0]}{user?.lastName?.[0]}
                </Avatar>
              </IconButton>
            </Tooltip>
            <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
              <MenuItem component={Link} to="/profile" onClick={() => setAnchorEl(null)}>My Profile</MenuItem>
              <MenuItem component={Link} to="/orders"  onClick={() => setAnchorEl(null)}>My Orders</MenuItem>
              <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>Logout</MenuItem>
            </Menu>
          </>
        ) : (
          <>
            <Button component={Link} to="/login"
              sx={{ color: 'white', textTransform: 'none', border: '1px solid rgba(255,255,255,0.3)', borderRadius: 2 }}>
              Login
            </Button>
            <Button component={Link} to="/register"
              variant="contained"
              sx={{ textTransform: 'none', borderRadius: 2,
                    background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                    '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)' } }}>
              Sign Up
            </Button>
          </>
        )}
      </Toolbar>
    </AppBar>
  );
}
