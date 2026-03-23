import axios from 'axios';

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: 10000,
});

request.interceptors.request.use(
  (config) => {
    // 后续可以补充从 localStorage 获取 Token 并携带在 Header 中
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

request.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    // 基础错误处理
    return Promise.reject(error);
  }
);

export default request;
