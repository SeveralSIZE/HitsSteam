// js/auth.js

function switchTab(tab) {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const tabs = document.querySelectorAll('.auth-tab');

    if (tab === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        tabs[0].classList.add('active');
        tabs[1].classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        tabs[0].classList.remove('active');
        tabs[1].classList.add('active');
    }

    document.getElementById('login-error').textContent = '';
    document.getElementById('register-error').textContent = '';
}

async function handleLogin() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    const errorEl = document.getElementById('login-error');

    if (!username || !password) {
        errorEl.textContent = 'Заполните все поля';
        return;
    }

    try {
        const data = await apiLogin(username, password);
        setToken(data.token);
        setUsername(username);
        window.location.href = 'dashboard.html';
    } catch (e) {
        errorEl.textContent = e.message;
    }
}

async function handleRegister() {
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const errorEl = document.getElementById('register-error');

    if (!username || !email || !password) {
        errorEl.textContent = 'Заполните все поля';
        return;
    }

    try {
        await apiRegister(username, email, password);
        const data = await apiLogin(username, password);
        setToken(data.token);
        setUsername(username);
        window.location.href = 'dashboard.html';
    } catch (e) {
        errorEl.textContent = e.message;
    }
}

if (getToken()) {
    window.location.href = 'dashboard.html';
}