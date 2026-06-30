import api from './axiosConfig';

// ── Auth / Customer ──────────────────────────────────────────────────────────
export const authApi = {
  register: (data)   => api.post('/api/customers/register', data),
  login:    (data)   => api.post('/api/customers/login', data),
  getProfile: ()     => api.get('/api/customers/profile'),
  updateProfile:(d)  => api.put('/api/customers/profile', d),
  getAddresses: ()   => api.get('/api/customers/addresses'),
  addAddress: (d)    => api.post('/api/customers/addresses', d),
  deleteAddress:(id) => api.delete(`/api/customers/addresses/${id}`),
};

// ── Products ─────────────────────────────────────────────────────────────────
export const productApi = {
  getAll: (params)   => api.get('/api/products', { params }),
  getById:(id)       => api.get(`/api/products/${id}`),
  create: (data)     => api.post('/api/products', data),
  update: (id, data) => api.put(`/api/products/${id}`, data),
  delete: (id)       => api.delete(`/api/products/${id}`),
};

// ── Categories ────────────────────────────────────────────────────────────────
export const categoryApi = {
  getAll: ()         => api.get('/api/categories'),
  create: (data)     => api.post('/api/categories', data),
};

// ── Orders ───────────────────────────────────────────────────────────────────
export const orderApi = {
  place:      (data)         => api.post('/api/orders', data),
  getMyOrders:(params)       => api.get('/api/orders', { params }),
  getById:    (id)           => api.get(`/api/orders/${id}`),
  cancel:     (id)           => api.put(`/api/orders/${id}/cancel`),
  getAllAdmin: (params)       => api.get('/api/orders/admin/all', { params }),
  updateStatus:(id, status)  => api.put(`/api/orders/admin/${id}/status?status=${status}`),
};

// ── Payments ─────────────────────────────────────────────────────────────────
export const paymentApi = {
  getByOrder: (orderId) => api.get(`/api/payments/order/${orderId}`),
  getMyPayments: ()     => api.get('/api/payments/my'),
};

// ── Admin ────────────────────────────────────────────────────────────────────
export const adminApi = {
  getDashboard: ()           => api.get('/api/admin/dashboard'),
  getSalesReport:(from, to)  => api.get('/api/admin/reports/sales', { params: { from, to } }),
  exportOrders:(from, to)    => api.get('/api/admin/reports/export', {
    params: { from, to },
    responseType: 'blob',
  }),
};
