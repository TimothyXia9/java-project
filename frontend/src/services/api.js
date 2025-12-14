import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use(
  (config) => {
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

export const authService = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data)
};

export const userService = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.put('/users/profile', data),
  getRecommendedCalories: () => api.get('/users/recommended-calories')
};

export const mealService = {
  createMeal: (data) => api.post('/meals', data),
  getMealsByDate: (date) => api.get(`/meals/date/${date}`),
  getMealsByDateRange: (startDate, endDate) =>
    api.get(`/meals/range?startDate=${startDate}&endDate=${endDate}`),
  getMeal: (id) => api.get(`/meals/${id}`),
  deleteMeal: (id) => api.delete(`/meals/${id}`)
};

export const foodService = {
  getAllFoods: () => api.get('/foods'),
  getFood: (id) => api.get(`/foods/${id}`),
  searchFoods: (name) => api.get(`/foods/search?name=${name}`),
  getFoodByBarcode: (barcode) => api.get(`/foods/barcode/${barcode}`),
  createFood: (data) => api.post('/foods', data)
};

export const imageService = {
  analyzeImage: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const token = localStorage.getItem('token');
    return api.post('/image/analyze', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token ? `Bearer ${token}` : ''
      }
    });
  }
};

export const barcodeService = {
  getFoodByBarcode: (barcode) => api.get(`/barcode/${barcode}`)
};

export default api;
