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

// Login Handler
forms.login?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        // Real API call to backend
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.detail || 'Login failed');
        }

        const data = await response.json();

        // Store authentication data
        state.token = data.token;
        state.username = data.username;
        state.role = data.role;

        localStorage.setItem('authToken', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('userRole', data.role);

        showNotification(`Welcome ${data.username}! (${data.role})`, 'success');
        updateView();  // Show the app container and hide login
    } catch (error) {
        console.error('Login error:', error);
        showNotification(error.message || 'Login failed. Please check your credentials.', 'error');
    }
});

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
    const salaryInput = document.getElementById('monthlySalary') || document.getElementById('salary');
    const salary = salaryInput ? salaryInput.value : 0;

    const payload = {
        document: docNumber,
        name: name,
        salary: Number(salary),
        affiliationDate: new Date().toISOString().split('T')[0]
    };

    console.log('Sending Affiliate Payload:', payload);

    try {
        const response = await fetch('http://localhost:8080/api/v1/affiliates', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.detail || 'Failed to create affiliate');
        }

        showNotification('Affiliate created successfully!', 'success');
        forms.affiliate.reset();
    } catch (error) {
        console.error('Create affiliate error:', error);
        showNotification(error.message || 'Failed to create affiliate', 'error');
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
        // Real API call to get pending credit applications
        const response = await fetch('http://localhost:8080/api/v1/credit-applications/pending', {
            headers: getHeaders()
        });

        if (!response.ok) {
            if (response.status === 401) {
                showNotification('Session expired. Please login again.', 'error');
                logout();
                return;
            }
            throw new Error('Failed to load applications');
        }

        const applications = await response.json();
        renderApplications(applications); // Changed from displayApplications to renderApplications
        showNotification(`Loaded ${applications.length} applications`, 'success');
    } catch (error) {
        console.error('Load error:', error);
        showNotification(error.message || 'Failed to load applications', 'error');

        // Display empty state
        if (elements.applicationsBody) {
            elements.applicationsBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                        <p style="margin: 0; font-size: 1.1rem;">⚠️ Failed to load applications</p>
                        <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem;">${error.message}</p>
                    </td>
                </tr>
            `;
        }
    }
};

const evaluateApplication = async (id) => {
    try {
        const response = await fetch(`http://localhost:8080/api/evaluations/${id}`, {
            method: 'POST',
            headers: getHeaders()
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.detail || 'Evaluation failed');
        }

        const result = await response.json();
        showNotification(`Evaluation complete: ${result.riskLevel} - ${result.approved ? 'APPROVED' : 'REJECTED'}`,
            result.approved ? 'success' : 'error');

        loadApplications(); // Refresh list
    } catch (error) {
        console.error('Evaluation error:', error);
        showNotification(error.message || 'Evaluation failed', 'error');
    }
};

const renderApplications = (applications) => {
    if (!elements.applicationsBody) return;

    if (!applications || applications.length === 0) {
        elements.applicationsBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                    <p style="margin: 0; font-size: 1.1rem;">📋 No pending applications</p>
                    <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem;">New applications will appear here</p>
                </td>
            </tr>
        `;
        return;
    }

    elements.applicationsBody.innerHTML = applications
        .map((app) => `
            <tr>
                <td>${app.id || 'N/A'}</td>
                <td>${app.affiliate?.name || app.affiliate?.document || 'N/A'}</td>
                <td>${new Date(app.applicationDate).toLocaleDateString()}</td>
                <td>$${(app.requestedAmount || 0).toLocaleString()}</td>
                <td>${app.termMonths || 'N/A'} months</td>
                <td><span class="status status-${(app.status || 'unknown').toLowerCase()}">${app.status || 'N/A'}</span></td>
                <td>
                    <button class="btn-action" onclick="evaluateApplication(${app.id})">Evaluate</button>
                </td>
            </tr>
        `)
        .join('');
};

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    updateView();

    // Removed duplicate login listener

    forms.affiliate.addEventListener('submit', createAffiliate);
    forms.credit.addEventListener('submit', createCreditApplication);

    elements.refreshBtn.addEventListener('click', loadApplications);
    elements.logoutBtn.addEventListener('click', logout);
});

// Expose function to global scope for HTML onclick
window.evaluateApplication = evaluateApplication;
