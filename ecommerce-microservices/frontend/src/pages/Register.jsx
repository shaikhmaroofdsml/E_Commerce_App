import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Box, Container, TextField, Button, Typography, Paper,
  Alert, CircularProgress, InputAdornment, Grid,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import EmailIcon from '@mui/icons-material/Email';
import LockIcon from '@mui/icons-material/Lock';
import PhoneIcon from '@mui/icons-material/Phone';
import { authApi } from '../api';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', password: '', phone: '',
  });
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState(null);
  const [success, setSuccess] = useState(false);

  const handleChange = e => setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError(null);
    try {
      await authApi.register(form);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{
      minHeight: '100vh', display: 'flex', alignItems: 'center',
      background: 'linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)', py: 4,
    }}>
      <Container maxWidth="sm">
        <Paper elevation={0} sx={{
          p: 5, borderRadius: 4,
          border: '1px solid rgba(109,40,217,0.12)',
          boxShadow: '0 20px 60px rgba(109,40,217,0.08)',
        }}>
          <Box textAlign="center" mb={4}>
            <Typography variant="h4" fontWeight={800} gutterBottom>Create Account 🎉</Typography>
            <Typography color="text.secondary">Join thousands of happy shoppers</Typography>
          </Box>

          {error   && <Alert severity="error"   sx={{ mb: 3 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 3 }}>Account created! Redirecting to login…</Alert>}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TextField id="reg-firstname" label="First Name" name="firstName"
                  value={form.firstName} onChange={handleChange} required fullWidth
                  InputProps={{ startAdornment: <InputAdornment position="start"><PersonIcon color="action" /></InputAdornment> }} />
              </Grid>
              <Grid item xs={6}>
                <TextField id="reg-lastname" label="Last Name" name="lastName"
                  value={form.lastName} onChange={handleChange} required fullWidth />
              </Grid>
            </Grid>

            <TextField id="reg-email" label="Email Address" name="email" type="email"
              value={form.email} onChange={handleChange} required fullWidth
              InputProps={{ startAdornment: <InputAdornment position="start"><EmailIcon color="action" /></InputAdornment> }} />

            <TextField id="reg-phone" label="Phone Number" name="phone" type="tel"
              value={form.phone} onChange={handleChange} required fullWidth
              placeholder="+919876543210"
              InputProps={{ startAdornment: <InputAdornment position="start"><PhoneIcon color="action" /></InputAdornment> }} />

            <TextField id="reg-password" label="Password" name="password" type="password"
              value={form.password} onChange={handleChange} required fullWidth
              helperText="Minimum 8 characters"
              InputProps={{ startAdornment: <InputAdornment position="start"><LockIcon color="action" /></InputAdornment> }} />

            <Button type="submit" variant="contained" size="large" fullWidth disabled={loading || success}
              sx={{ mt: 1, py: 1.5, borderRadius: 2.5, fontWeight: 700, fontSize: '1rem',
                    background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
                    '&:hover': { background: 'linear-gradient(135deg, #7c3aed, #3b82f6)' } }}>
              {loading ? <CircularProgress size={22} color="inherit" /> : 'Create Account'}
            </Button>
          </Box>

          <Typography textAlign="center" mt={3} color="text.secondary">
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#6d28d9', fontWeight: 700, textDecoration: 'none' }}>
              Sign in
            </Link>
          </Typography>
        </Paper>
      </Container>
    </Box>
  );
}
