import React from 'react';
import { useSelector } from 'react-redux';
import { Navigate } from 'react-router-dom';

export function ProtectedRoute({ children, adminOnly = false }) {
  const { isAuthenticated, user } = useSelector(s => s.auth);

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (adminOnly && user?.role !== 'ROLE_ADMIN') return <Navigate to="/" replace />;

  return children;
}
