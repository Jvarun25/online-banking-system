import api from './api'

export const authService = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
}

export const accountService = {
  list: () => api.get('/accounts'),
  get: (accountNumber) => api.get(`/accounts/${accountNumber}`),
  create: (data) => api.post('/accounts', data),
  deposit: (data) => api.post('/accounts/deposit', data),
  withdraw: (data) => api.post('/accounts/withdraw', data),
}

export const transferService = {
  transfer: (data) => api.post('/transfers', data),
}

export const transactionService = {
  history: (accountNumber, page = 0, size = 20) =>
    api.get(`/accounts/${accountNumber}/transactions`, { params: { page, size } }),
}

export const adminService = {
  allAccounts: () => api.get('/admin/accounts'),
  freeze: (accountNumber) => api.patch(`/admin/accounts/${accountNumber}/freeze`),
  unfreeze: (accountNumber) => api.patch(`/admin/accounts/${accountNumber}/unfreeze`),
}
