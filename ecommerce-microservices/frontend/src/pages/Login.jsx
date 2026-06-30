import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Box, Container, TextField, Button, Typography, Paper,
  Alert, CircularProgress, InputAdornment, IconButton,
} from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import LockIcon from '@mui/icons-material/Lock';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import { loginStart, loginSuccess, loginFailure } from '../store/authSlice';
import { authApi } from '../api';

export default function Login() {
  const dispatch  = useDispatch();
  const navigate  = useNavigate();
  const { loading, error } = useSelector(s => s.auth);

  const [form, setForm] = useState({ email: '', password: '' });
  const [showPass, setShowPass] = useState(false);

  const handleChange = e => setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    dispatch(loginStart());
    try {
      const res = await authApi.login(form);
      dispatch(loginSuccess(res.data));
      navigate(res.data.role === 'ROLE_ADMIN' ? '/admin' : '/');
    } catch (err) {
      dispatch(loginFailure(err.response?.data?.message || 'Invalid email or password'));
    }
  };

  return (
    <Box sx={{
      minHeight: '100vh', display: 'flex', alignItems: 'center',
      background: 'linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)',
    }}>
      <Container maxWidth="sm">
        <Paper elevation={0} sx={{
          p: 5, borderRadius: 4,
          border: '1px solid rgba(109,40,217,0.12)',
          boxShadow: '0 20px 60px rgba(109,40,217,0.08)',
        }}>
          {/* Header */}
          <Box textAlign="center" mb={4}>
            <Typography variant="h4" fontWeight={800} gutterBottom>
              Welcome Back 👋
            </Typography>
            <Typography color="text.secondary">
              Sign in to continue shopping
            </Typography>
          </Box>

          {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <TextField
              id="login-email"
              label="Email Address" name="email" type="email"
              value={form.email} onChange={handleChange} required fullWidth
              InputProps={{ startAdornment: (
                <InputAdornment position="start"><EmailIcon color="action" /></InputAdornment>
              )}}
            />
            <TextField
              id="login-password"
              label="Password" name="password"
              type={showPass ? 'text' : 'password'}
              value={form.password} onChange={handleChange} required fullWidth
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start"><LockIcon color="action" /></InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setShowPass(p => !p)} edge="end">
                      {showPass ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <Button type="submit" variant="contained" size="large" fullWidth
              disabled={loading}
              sx={{
                mt: 1, py: 1.5, borderRadius: 2.5, fontWeight: 700, fontSize: '1rem',
                background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)' },
              }}>
              {loading ? <CircularProgress size={22} color="inherit" /> : 'Sign In'}
            </Button>
          </Box>

          <Typography textAlign="center" mt={3} color="text.secondary">
            Don't have an account?{' '}
            <Link to="/register" style={{ color: '#6d28d9', fontWeight: 700, textDecoration: 'none' }}>
              Sign up
            </Link>
          </Typography>
        </Paper>
      </Container>
    </Box>
  );
}
