// js/dashboard.js

if (!getToken()) window.location.href = 'index.html';

document.getElementById('header-username').textContent = getUsername();
document.getElementById('amount').addEventListener('input', updateTotal);

let selectedCardId = null;

function updateTotal() {
    const amount = document.getElementById('amount').value || 0;
    document.getElementById('total-display').textContent =
        Number(amount).toLocaleString('ru-RU') + ' ₽';
}

function setAmount(val) {
    document.getElementById('amount').value = val;
    updateTotal();
    document.querySelectorAll('.preset').forEach(b => b.classList.remove('active'));
    event.target.classList.add('active');
}

function handleLogout() {
    clearAuth();
    window.location.href = 'index.html';
}

function setPayButtonState() {
    const btn = document.getElementById('pay-btn');
    if (selectedCardId) {
        btn.textContent = 'Оплатить';
        btn.disabled = false;
    } else {
        btn.textContent = 'Оплатить';
        btn.disabled = true;
    }
}

function renderCardList(cards) {
    selectedCardId = null;
    setPayButtonState();

    const section = document.getElementById('card-section');
    const cardItems = cards.map(card => `
        <div class="card-list-item" onclick="showCardDetail(${card.id}, '${card.cardNumber}', '${card.cardholderName}', '${card.expiry}')">
            <div class="card-list-item__info">
                <span class="card-list-item__number">**** **** **** ${card.cardNumber.slice(-4)}</span>
                <span class="card-list-item__name">${card.cardholderName}</span>
            </div>
            <svg width="36" height="24" viewBox="0 0 40 26" fill="none">
                <rect width="40" height="26" rx="4" fill="#1a1f71"/>
                <circle cx="15" cy="13" r="8" fill="#eb001b" opacity="0.9"/>
                <circle cx="25" cy="13" r="8" fill="#f79e1b" opacity="0.9"/>
            </svg>
        </div>
    `).join('');

    section.innerHTML = `
        <div class="card-list">
            ${cardItems}
            <div class="card-list-add" onclick="renderAddCardForm()">
                + Добавить новую карту
            </div>
        </div>
    `;
}

function showCardDetail(id, number, name, expiry) {
    selectedCardId = id;
    setPayButtonState();

    document.getElementById('card-section').innerHTML = `
        <div class="card-detail">
            <button class="card-detail__back" onclick="reloadCards()">✕</button>
            <div class="card-detail__row">
                <span class="card-detail__label">Номер карты</span>
                <span class="card-detail__value">${number}</span>
            </div>
            <div class="card-detail__row">
                <span class="card-detail__label">Владелец</span>
                <span class="card-detail__value">${name}</span>
            </div>
            <div class="card-detail__row">
                <span class="card-detail__label">Срок действия</span>
                <span class="card-detail__value">${expiry}</span>
            </div>
        </div>
    `;
}

function renderAddCardForm() {
    selectedCardId = null;
    setPayButtonState();

    document.getElementById('card-section').innerHTML = `
        <div class="card-form">
            <input type="text" id="card-holder" placeholder="Имя владельца (как на карте)">
            <input type="text" id="card-number" placeholder="Номер карты" maxlength="19"
                oninput="formatCardNumber(this)">
            <div class="card-row">
                <input type="text" id="card-expiry" placeholder="ММ/ГГГГ" maxlength="7"
                    oninput="formatExpiry(this)">
                <input type="password" id="card-cvv" placeholder="CVV" maxlength="3">
            </div>
            <button class="btn-primary" onclick="handleAddCard()">Добавить карту</button>
            <button class="btn-cancel" onclick="reloadCards()">Отмена</button>
        </div>
    `;
}

function formatCardNumber(input) {
    let val = input.value.replace(/\D/g, '').slice(0, 16);
    input.value = val.replace(/(.{4})/g, '$1 ').trim();
}

function formatExpiry(input) {
    let val = input.value.replace(/\D/g, '').slice(0, 6);
    if (val.length >= 3) val = val.slice(0, 2) + '/' + val.slice(2);
    input.value = val;
}

async function reloadCards() {
    selectedCardId = null;
    setPayButtonState();
    await loadCardSection();
}

async function handleAddCard() {
    const errorEl = document.getElementById('pay-error');
    errorEl.textContent = '';

    const holder = document.getElementById('card-holder')?.value.trim();
    const number = document.getElementById('card-number')?.value.replace(/\s/g, '');
    const expiry = document.getElementById('card-expiry')?.value.trim();
    const cvv = document.getElementById('card-cvv')?.value.trim();

    if (!holder || !number || !expiry || !cvv) {
        errorEl.textContent = 'Заполните все поля карты';
        return;
    }

    try {
        await apiAddCard(holder, number, expiry, cvv, 0);
        await reloadCards();
    } catch (e) {
        errorEl.textContent = e.message;
    }
}

async function handlePay() {
    const errorEl = document.getElementById('pay-error');
    const successEl = document.getElementById('pay-success');
    const amount = parseFloat(document.getElementById('amount').value);
    const steamLogin = document.getElementById('steam-login').value.trim();

    errorEl.textContent = '';
    successEl.textContent = '';

    if (!steamLogin) { errorEl.textContent = 'Введите логин Steam'; return; }
    if (!amount || amount <= 0) { errorEl.textContent = 'Введите сумму'; return; }
    if (!selectedCardId) { errorEl.textContent = 'Выберите карту'; return; }

    try {
        await apiTopUp(selectedCardId, amount);
        successEl.textContent = `Баланс Steam пополнен на ${amount.toLocaleString('ru-RU')} ₽`;
    } catch (e) {
        errorEl.textContent = e.message;
    }
}

async function loadCardSection() {
    try {
        const cards = await apiGetCards();
        if (cards.length > 0) {
            renderCardList(cards);
        } else {
            renderAddCardForm();
        }
    } catch (e) {
        renderAddCardForm();
    }
}

function switchDashTab(tab) {
    document.getElementById('tab-topup').style.display = tab === 'topup' ? 'flex' : 'none';
    document.getElementById('tab-transfer').style.display = tab === 'transfer' ? 'block' : 'none';
    document.querySelectorAll('.dashboard-tab').forEach((b, i) => {
        b.classList.toggle('active', (tab === 'topup' && i === 0) || (tab === 'transfer' && i === 1));
    });
}

let searchTimeout = null;

async function handleSearch() {
    const q = document.getElementById('search-input').value.trim();
    const results = document.getElementById('search-results');

    clearTimeout(searchTimeout);

    if (!q) { results.innerHTML = ''; return; }

    searchTimeout = setTimeout(async () => {
        try {
            const users = await apiSearchUsers(q);
            if (users.length === 0) {
                results.innerHTML = '<div class="search-empty">Игроки не найдены</div>';
                return;
            }
            results.innerHTML = users.map(u => `
                <div class="search-result-item">
                    <div class="search-result-item__info">
                        <span class="search-result-item__username">${u.username}</span>
                        <span class="search-result-item__email">${u.email}</span>
                    </div>
                    <button class="btn-transfer" onclick="alert('Функция перевода в разработке')">
                        Перевести
                    </button>
                </div>
            `).join('');
        } catch (e) {
            results.innerHTML = '<div class="search-empty">Ошибка поиска</div>';
        }
    }, 400);
}

loadCardSection();
updateTotal();