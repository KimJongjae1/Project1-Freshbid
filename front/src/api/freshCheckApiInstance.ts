import axios from 'axios';

const FRESH_CHECK_SERVER_URL = import.meta.env.VITE_FRESH_CHECK_SERVER_URL;
const protocol = window.location.protocol === "https:" ? "https:" : "http:";

const freshCheckApiInstance = axios.create({
    baseURL: `${protocol}//${FRESH_CHECK_SERVER_URL}`,
    headers: {
        "Content-Type": "multipart/form-data",
    }
});

export default freshCheckApiInstance;