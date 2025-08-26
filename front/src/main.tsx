import ReactDOM from 'react-dom/client';
import App from './App';
import axios from 'axios';
import './index.css'; // Tailwind 포함

axios.defaults.baseURL = "http://localhost:8080";
axios.defaults.withCredentials = true;

ReactDOM.createRoot(document.getElementById('root')!).render(
  // <React.StrictMode>
    <App />
  // </React.StrictMode>
);
