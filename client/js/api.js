// js/api.js

const API = 'http://localhost:8080';

function getToken() {
    return localStorage.getItem('token');
}

function setToken(token) {
    localStorage.setItem('token', token);
}

function getUsername() {
    return localStorage.getItem('username');
}

function setUsername(username) {
    localStorage.setItem('username', username);
}

function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
}

async function apiSearchUsers(q) {
    return apiRequest('GET', '/users/search?q=' + encodeURIComponent(q));
}

async function apiRequest(method, path, body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API + path, opts);

    const text = await res.text();
    const data = text ? JSON.parse(text) : {};

    if (!res.ok) throw new Error(data.error || 'Ошибка сервера');
    return data;
}

async function apiRegister(username, email, password) {
    return apiRequest('POST', '/auth/register', { username, email, password });
}

async function apiLogin(username, password) {
    return apiRequest('POST', '/auth/login', { username, password });
}

async function apiAddCard(cardholderName, cardNumber, expiry, cvv, amount) {
    return apiRequest('POST', '/cards/add', { cardholderName, cardNumber, expiry, cvv, amount });
}

async function apiGetCards() {
    return apiRequest('GET', '/cards');
}

async function apiTopUp(cardId, amount) {
    return apiRequest('POST', '/transactions', { cardId, amount });
}