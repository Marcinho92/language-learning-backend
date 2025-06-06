import axios from 'axios';

const api = axios.create({
    baseURL: 'https://language-learning-backend-850755743835.europe-central2.run.app'
});
 
export default api; 