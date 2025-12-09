// State
const state = {
    token: localStorage.getItem('authToken'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('userRole')
};

// DOM Elements
const views = {
    auth: document.getElementById('auth-container'),
    app: document.getElementById('app-container')
};

const forms = {
    login: document.getElementById('login-form'),
    affiliate: document.getElementById('affiliate-form'),
    credit: document.getElementById('credit-form')
};

const elements = {
    notifications: document.getElementById('notification-area'),
    applicationsBody: document.getElementById('applications-body'),
    refreshBtn: document.getElementById('refresh-btn'),
    logoutBtn: document.getElementById('logout-btn'),
    userDisplay: document.getElementById('user-display')
};

// Utilities
const showNotification = (message, type = 'info') => {
    const notif = document.createElement('div');
    notif.className = `notification notification-${type}`;
    notif.textContent = message;
    notif.style.borderLeftColor = type === 'error' ? 'var(--error-color)' :
        type === 'success' ? 'var(--success-color)' : 'var(--accent-color)';

    elements.notifications.appendChild(notif);

    setTimeout(() => {
        notif.style.opacity = '0';
        setTimeout(() => notif.remove(), 300);
    }, 5000);
};

const getHeaders = () => {
    const headers = { 'Content-Type': 'application/json' };
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    return headers;
};

const handleApiError = (error) => {
    console.error('API Error:', error);
    if (error.status === 401 || error.status === 403) {
        logout();
        showNotification('Session expired. Please login again.', 'error');
    } else {
        showNotification(error.message || 'An unexpected error occurred', 'error');
    }
};

// Auth Functions
const updateView = () => {
    if (state.token) {
        views.auth.classList.add('hidden');
        views.app.classList.remove('hidden');
        elements.userDisplay.textContent = `Logged in as: ${state.username || 'User'}`;
        loadApplications();
    } else {
        views.auth.classList.remove('hidden');
        views.app.classList.add('hidden');
    }
};

const login = async (username, password) => {
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) throw { status: response.status, message: 'Invalid credentials' };

        const data = await response.json();

        state.token = data.token;
        state.username = username;

        // Decode token payload for role (simple decode)
        const payload = JSON.parse(atob(data.token.split('.')[1]));
        state.role = payload.role;

        localStorage.setItem('authToken', state.token);
        localStorage.setItem('username', state.username);
        localStorage.setItem('userRole', state.role);

        updateView();
        showNotification('Login successful', 'success');

    } catch (error) {
        handleApiError(error);
    }
};

const logout = () => {
    state.token = null;
    state.username = null;
    state.role = null;
    localStorage.clear();
    updateView();
};

// API Interactions
const createAffiliate = async (e) => {
    e.preventDefault();
    const docNumber = document.getElementById('docNumber').value;
    const name = document.getElementById('name').value;
    const salary = document.getElementById('salary').value;

    try {
        const response = await fetch('/api/v1/affiliates', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({
                documentNumber: docNumber,
                name: name,
                monthlySalary: parseFloat(salary)
            })
        });

        if (!response.ok) throw { status: response.status, message: 'Failed to create affiliate' };

        showNotification('Affiliate created successfully', 'success');
        forms.affiliate.reset();
    } catch (error) {
        handleApiError(error);
    }
};

const createCreditApplication = async (e) => {
    e.preventDefault();
    const affiliateId = document.getElementById('affiliateId').value;
    const amount = document.getElementById('amount').value;
    const term = document.getElementById('term').value;

    try {
        const response = await fetch('/api/v1/credit-applications', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({
                affiliateId: parseInt(affiliateId),
                requestedAmount: parseFloat(amount),
                term: parseInt(term)
            })
        });

        if (!response.ok) throw { status: response.status, message: 'Failed to submit application' };

        showNotification('Credit application submitted', 'success');
        forms.credit.reset();
        loadApplications();
    } catch (error) {
        handleApiError(error);
    }
};

const loadApplications = async () => {
    try {
        const response = await fetch('/api/v1/credit-applications/pending', {
            headers: getHeaders()
        });

        if (!response.ok) throw { status: response.status, message: 'Failed to fetch applications' };

        const applications = await response.json();
        renderApplications(applications);
    } catch (error) {
        handleApiError(error);
        elements.applicationsBody.innerHTML = '<tr><td colspan="7" class="text-center">Error loading data</td></tr>';
    }
};

const evaluateApplication = async (id, affiliateId) => {
    try {
        const response = await fetch('/api/evaluations', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({
                creditApplicationId: id,
                affiliateId: affiliateId
            })
        });

        if (!response.ok) throw { status: response.status, message: 'Evaluation failed' };

        const result = await response.json();
        showNotification(`Evaluation complete: ${result.riskLevel} - ${result.approved ? 'APPROVED' : 'REJECTED'}`,
            result.approved ? 'success' : 'error');

        loadApplications(); // Refresh list to show updated status (if strictly pending filter, it disappears)
    } catch (error) {
        handleApiError(error);
    }
};

const renderApplications = (applications) => {
    elements.applicationsBody.innerHTML = '';

    if (applications.length === 0) {
        elements.applicationsBody.innerHTML = '<tr><td colspan="7" class="text-center">No pending applications found</td></tr>';
        return;
    }

    applications.forEach(app => {
        const tr = document.createElement('tr');

        const date = new Date(app.applicationDate).toLocaleDateString();

        tr.innerHTML = `
            <td>${app.id}</td>
            <td>${app.affiliateId}</td>
            <td>${date}</td>
            <td>$${app.requestedAmount.toLocaleString()}</td>
            <td>${app.term} mo</td>
            <td><span class="status-badge status-${app.status.toLowerCase()}">${app.status}</span></td>
            <td>
                <button class="btn btn-primary btn-sm evaluate-btn" data-id="${app.id}" data-affiliate="${app.affiliateId}">
                    Evaluate
                </button>
            </td>
        `;
        elements.applicationsBody.appendChild(tr);
    });

    // Add listeners to new buttons
    document.querySelectorAll('.evaluate-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            evaluateApplication(btn.dataset.id, btn.dataset.affiliate);
        });
    });
};

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    updateView();

    forms.login.addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        login(username, password);
    });

    forms.affiliate.addEventListener('submit', createAffiliate);
    forms.credit.addEventListener('submit', createCreditApplication);

    elements.refreshBtn.addEventListener('click', loadApplications);
    elements.logoutBtn.addEventListener('click', logout);
});
