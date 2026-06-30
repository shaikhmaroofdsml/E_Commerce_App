import React, { useEffect, useState } from 'react';
import {
  Container, Typography, Grid, Paper, Box, Button,
  Divider, CircularProgress,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { adminApi } from '../api';

const StatCard = ({ label, value, icon, color }) => (
  <Paper elevation={0} sx={{
    p: 3, borderRadius: 3, border: '1px solid rgba(0,0,0,0.07)',
    '&:hover': { boxShadow: '0 8px 24px rgba(0,0,0,0.08)' }, transition: 'box-shadow 0.2s',
  }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      <Box sx={{ fontSize: 36 }}>{icon}</Box>
      <Box>
        <Typography variant="h4" fontWeight={900} color={color || 'text.primary'}>{value}</Typography>
        <Typography variant="body2" color="text.secondary">{label}</Typography>
      </Box>
    </Box>
  </Paper>
);

export default function AdminDashboard() {
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    adminApi.getDashboard()
      .then(r => setStats(r.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const handleExport = async () => {
    setExporting(true);
    try {
      const from = new Date(); from.setMonth(from.getMonth() - 1);
      const to   = new Date();
      const res  = await adminApi.exportOrders(
        from.toISOString().split('T')[0],
        to.toISOString().split('T')[0]
      );
      const url  = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href  = url;
      link.setAttribute('download', `orders_export.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      console.error('Export failed:', e);
    } finally {
      setExporting(false);
    }
  };

  if (loading) return (
    <Container sx={{ py: 5, textAlign: 'center' }}>
      <CircularProgress size={48} />
      <Typography mt={2}>Loading dashboard…</Typography>
    </Container>
  );

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 5 }}>
        <Box>
          <Typography variant="overline" color="primary" fontWeight={700} letterSpacing={2}>
            Admin
          </Typography>
          <Typography variant="h4" fontWeight={800}>Dashboard</Typography>
        </Box>
        <Button variant="contained" startIcon={<DownloadIcon />}
          onClick={handleExport} disabled={exporting}
          sx={{
            borderRadius: 2.5, fontWeight: 700,
            background: 'linear-gradient(135deg, #6d28d9, #2563eb)',
          }}>
          {exporting ? 'Exporting…' : 'Export to Excel'}
        </Button>
      </Box>

      {/* Stats Grid */}
      <Grid container spacing={3} mb={5}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard label="Total Orders"    value={stats?.totalOrders    ?? 0} icon="📦" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard label="Total Customers" value={stats?.totalCustomers ?? 0} icon="👥" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard label="Total Products"  value={stats?.totalProducts  ?? 0} icon="🏷️" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            label="Total Revenue"
            value={`$${Number(stats?.totalRevenue || 0).toFixed(2)}`}
            icon="💰"
            color="success.main"
          />
        </Grid>
      </Grid>

      <Divider sx={{ mb: 4 }} />

      {/* Info panel */}
      <Paper elevation={0} sx={{ p: 4, borderRadius: 3, bgcolor: '#f8faff', border: '1px solid rgba(37,99,235,0.15)' }}>
        <Typography variant="h6" fontWeight={700} gutterBottom>System Status</Typography>
        <Typography color="text.secondary">
          All microservices are running. For detailed management, use the REST API endpoints
          directly or extend this dashboard with order management tables and analytics charts.
        </Typography>
        <Box mt={2}>
          {[
            ['Config Server',       'http://localhost:8888'],
            ['Eureka Dashboard',    'http://localhost:8761'],
            ['API Gateway',         'http://localhost:8080'],
            ['Customer Service',    'http://localhost:8081'],
            ['Product Service',     'http://localhost:8082'],
            ['Order Service',       'http://localhost:8083'],
            ['Notification Service','http://localhost:8084'],
            ['Admin Service',       'http://localhost:8085'],
            ['Payment Service',     'http://localhost:8086'],
          ].map(([name, url]) => (
            <Box key={name} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.5 }}>
              <Typography variant="body2" fontWeight={600}>{name}</Typography>
              <Typography variant="body2" color="primary" component="a" href={url} target="_blank"
                sx={{ textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}>
                {url}
              </Typography>
            </Box>
          ))}
        </Box>
      </Paper>
    </Container>
  );
}
