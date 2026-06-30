import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { ThemeProvider, createTheme, CssBaseline, Box, CircularProgress } from '@mui/material';
import store from './store';
import Navbar from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';

// ── Lazy-loaded pages for code splitting ──────────────────────────────────────
const Home           = lazy(() => import('./pages/Home'));
const Login          = lazy(() => import('./pages/Login'));
const Register       = lazy(() => import('./pages/Register'));
const Products       = lazy(() => import('./pages/Products'));
const ProductDetail  = lazy(() => import('./pages/ProductDetail'));
const Cart           = lazy(() => import('./pages/Cart'));
const Orders         = lazy(() => import('./pages/Orders'));
const OrderDetail    = lazy(() => import('./pages/OrderDetail'));
const Profile        = lazy(() => import('./pages/Profile'));
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'));

// ── MUI Theme ─────────────────────────────────────────────────────────────────
const theme = createTheme({
  palette: {
    primary: { main: '#6d28d9' },
    secondary: { main: '#2563eb' },
    background: { default: '#fafbff' },
  },
  typography: {
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { textTransform: 'none', borderRadius: 10 },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined' },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
  },
});

const Loader = () => (
  <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
    <CircularProgress size={48} thickness={4} />
  </Box>
);

export default function App() {
  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <Navbar />
          <Suspense fallback={<Loader />}>
            <Routes>
              {/* Public */}
              <Route path="/"               element={<Home />} />
              <Route path="/login"          element={<Login />} />
              <Route path="/register"       element={<Register />} />
              <Route path="/products"       element={<Products />} />
              <Route path="/products/:id"   element={<ProductDetail />} />
              <Route path="/cart"           element={<Cart />} />

              {/* Protected — authenticated users */}
              <Route path="/profile" element={
                <ProtectedRoute><Profile /></ProtectedRoute>
              } />
              <Route path="/orders" element={
                <ProtectedRoute><Orders /></ProtectedRoute>
              } />
              <Route path="/orders/:id" element={
                <ProtectedRoute><OrderDetail /></ProtectedRoute>
              } />

              {/* Admin only */}
              <Route path="/admin" element={
                <ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>
              } />

              {/* 404 */}
              <Route path="*" element={
                <Box sx={{ textAlign: 'center', py: 10 }}>
                  <CircularProgress sx={{ display: 'none' }} />
                  <Box sx={{ fontSize: 64 }}>🔍</Box>
                  <h1>404 — Page Not Found</h1>
                  <a href="/" style={{ color: '#6d28d9' }}>Go Home</a>
                </Box>
              } />
            </Routes>
          </Suspense>
        </BrowserRouter>
      </ThemeProvider>
    </Provider>
  );
}
