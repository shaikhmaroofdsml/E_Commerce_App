import { createSlice } from '@reduxjs/toolkit';

const storedUser  = localStorage.getItem('user');
const storedToken = localStorage.getItem('token');

const initialState = {
  user:          storedUser ? JSON.parse(storedUser) : null,
  token:         storedToken || null,
  isAuthenticated: !!storedToken,
  loading:       false,
  error:         null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginStart:   (state) => { state.loading = true; state.error = null; },
    loginSuccess: (state, action) => {
      const { token, ...user } = action.payload;
      state.loading        = false;
      state.isAuthenticated = true;
      state.token          = token;
      state.user           = user;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    },
    loginFailure: (state, action) => {
      state.loading = false;
      state.error   = action.payload;
    },
    logout: (state) => {
      state.user           = null;
      state.token          = null;
      state.isAuthenticated = false;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },
    clearError: (state) => { state.error = null; },
  },
});

export const { loginStart, loginSuccess, loginFailure, logout, clearError } = authSlice.actions;
export default authSlice.reducer;
