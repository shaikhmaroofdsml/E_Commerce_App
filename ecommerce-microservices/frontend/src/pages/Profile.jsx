import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import {
  Container, Typography, Box, Paper, Grid, TextField, Button,
  Alert, CircularProgress, Chip, Avatar, Divider, List,
  ListItem, ListItemText, ListItemIcon, IconButton,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import StarIcon from '@mui/icons-material/Star';
import { authApi } from '../api';

export default function Profile() {
  const { user } = useSelector(s => s.auth);

  const [profile,    setProfile]    = useState(null);
  const [addresses,  setAddresses]  = useState([]);
  const [loading,    setLoading]    = useState(true);
  const [saving,     setSaving]     = useState(false);
  const [success,    setSuccess]    = useState(null);
  const [error,      setError]      = useState(null);
  const [showAddrForm, setShowAddrForm] = useState(false);

  const [form, setForm] = useState({ firstName: '', lastName: '', phone: '' });
  const [addrForm, setAddrForm] = useState({
    street: '', city: '', state: '', zipCode: '', country: '', isDefault: false
  });

  useEffect(() => {
    Promise.all([authApi.getProfile(), authApi.getAddresses()])
      .then(([pRes, aRes]) => {
        setProfile(pRes.data);
        setForm({ firstName: pRes.data.firstName, lastName: pRes.data.lastName, phone: pRes.data.phone || '' });
        setAddresses(aRes.data || []);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleProfileSave = async (e) => {
    e.preventDefault();
    setSaving(true); setError(null); setSuccess(null);
    try {
      const res = await authApi.updateProfile({ ...form, email: profile.email });
      setProfile(res.data);
      setSuccess('Profile updated successfully!');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleAddAddress = async (e) => {
    e.preventDefault();
    try {
      const res = await authApi.addAddress(addrForm);
      setAddresses(prev => [...prev, res.data]);
      setShowAddrForm(false);
      setAddrForm({ street: '', city: '', state: '', zipCode: '', country: '', isDefault: false });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add address');
    }
  };

  const handleDeleteAddress = async (id) => {
    try {
      await authApi.deleteAddress(id);
      setAddresses(prev => prev.filter(a => a.id !== id));
    } catch (err) {
      setError('Failed to delete address');
    }
  };

  if (loading) return (
    <Container sx={{ py: 5, textAlign: 'center' }}><CircularProgress size={48} /></Container>
  );

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Typography variant="h4" fontWeight={800} gutterBottom>My Profile</Typography>

      <Grid container spacing={4}>
        {/* Profile Info */}
        <Grid item xs={12} md={7}>
          <Paper elevation={0} sx={{ p: 4, borderRadius: 3, border: '1px solid rgba(0,0,0,0.07)' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
              <Avatar sx={{ width: 64, height: 64, bgcolor: '#6d28d9', fontSize: 24, fontWeight: 800 }}>
                {profile?.firstName?.[0]}{profile?.lastName?.[0]}
              </Avatar>
              <Box>
                <Typography variant="h6" fontWeight={800}>
                  {profile?.firstName} {profile?.lastName}
                </Typography>
                <Chip label={profile?.role === 'ROLE_ADMIN' ? 'Admin' : 'Customer'}
                  color={profile?.role === 'ROLE_ADMIN' ? 'secondary' : 'primary'}
                  size="small" />
              </Box>
            </Box>

            {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
            {error   && <Alert severity="error"   sx={{ mb: 2 }}>{error}</Alert>}

            <Box component="form" onSubmit={handleProfileSave} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <TextField label="First Name" fullWidth value={form.firstName}
                    onChange={e => setForm(p => ({ ...p, firstName: e.target.value }))} required />
                </Grid>
                <Grid item xs={6}>
                  <TextField label="Last Name" fullWidth value={form.lastName}
                    onChange={e => setForm(p => ({ ...p, lastName: e.target.value }))} required />
                </Grid>
              </Grid>
              <TextField label="Email" fullWidth value={profile?.email || ''} disabled
                helperText="Email cannot be changed here" />
              <TextField label="Phone" fullWidth value={form.phone}
                onChange={e => setForm(p => ({ ...p, phone: e.target.value }))} />

              <Button type="submit" variant="contained" disabled={saving}
                sx={{ mt: 1, py: 1.2, borderRadius: 2.5, fontWeight: 700,
                      background: 'linear-gradient(135deg, #6d28d9, #2563eb)' }}>
                {saving ? <CircularProgress size={20} color="inherit" /> : 'Save Changes'}
              </Button>
            </Box>
          </Paper>
        </Grid>

        {/* Addresses */}
        <Grid item xs={12} md={5}>
          <Paper elevation={0} sx={{ p: 3, borderRadius: 3, border: '1px solid rgba(0,0,0,0.07)' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography fontWeight={800}>Saved Addresses</Typography>
              <Button size="small" startIcon={<AddIcon />} onClick={() => setShowAddrForm(v => !v)}>
                Add New
              </Button>
            </Box>

            {showAddrForm && (
              <Box component="form" onSubmit={handleAddAddress}
                sx={{ mb: 2, p: 2, bgcolor: '#f8faff', borderRadius: 2, display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                <TextField size="small" label="Street" fullWidth required
                  value={addrForm.street} onChange={e => setAddrForm(p => ({ ...p, street: e.target.value }))} />
                <Grid container spacing={1}>
                  <Grid item xs={6}>
                    <TextField size="small" label="City" fullWidth required
                      value={addrForm.city} onChange={e => setAddrForm(p => ({ ...p, city: e.target.value }))} />
                  </Grid>
                  <Grid item xs={6}>
                    <TextField size="small" label="State" fullWidth required
                      value={addrForm.state} onChange={e => setAddrForm(p => ({ ...p, state: e.target.value }))} />
                  </Grid>
                </Grid>
                <Grid container spacing={1}>
                  <Grid item xs={6}>
                    <TextField size="small" label="ZIP" fullWidth required
                      value={addrForm.zipCode} onChange={e => setAddrForm(p => ({ ...p, zipCode: e.target.value }))} />
                  </Grid>
                  <Grid item xs={6}>
                    <TextField size="small" label="Country" fullWidth required
                      value={addrForm.country} onChange={e => setAddrForm(p => ({ ...p, country: e.target.value }))} />
                  </Grid>
                </Grid>
                <Button type="submit" variant="contained" size="small" fullWidth
                  sx={{ background: 'linear-gradient(135deg, #6d28d9, #2563eb)', fontWeight: 700 }}>
                  Save Address
                </Button>
              </Box>
            )}

            <Divider sx={{ mb: 1 }} />

            {addresses.length === 0 ? (
              <Typography color="text.secondary" textAlign="center" py={3}>
                No saved addresses yet.
              </Typography>
            ) : (
              <List dense disablePadding>
                {addresses.map(addr => (
                  <ListItem key={addr.id} disablePadding
                    sx={{ py: 1, borderBottom: '1px solid rgba(0,0,0,0.05)' }}
                    secondaryAction={
                      <IconButton edge="end" size="small" color="error"
                        onClick={() => handleDeleteAddress(addr.id)}>
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    }>
                    <ListItemIcon sx={{ minWidth: 36 }}>
                      {addr.isDefault
                        ? <StarIcon fontSize="small" color="warning" />
                        : <LocationOnIcon fontSize="small" color="action" />}
                    </ListItemIcon>
                    <ListItemText
                      primary={<Typography variant="body2" fontWeight={600}>{addr.street}</Typography>}
                      secondary={`${addr.city}, ${addr.state} ${addr.zipCode}, ${addr.country}`}
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}
