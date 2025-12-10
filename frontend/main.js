// State
const state = {
    token: localStorage.getItem('authToken'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('userRole'),
    affiliates: [] // Store affiliates for dropdown
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
    affiliatesBody: document.getElementById('affiliates-body'),
    affiliateSelect: document.getElementById('affiliateId'),
    affiliateResult: document.getElementById('affiliate-result'),
    refreshBtn: document.getElementById('refresh-btn'),
    refreshAffiliatesBtn: document.getElementById('refresh-affiliates-btn'),
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

const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0
    }).format(amount);
};

// Auth Functions
const updateView = () => {
    if (state.token) {
        views.auth.classList.add('hidden');
        views.app.classList.remove('hidden');
        elements.userDisplay.textContent = `Logged in as: ${state.username || 'User'}`;
        loadAffiliates();
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

        state.token = data.token;
        state.username = data.username;
        state.role = data.role;

        localStorage.setItem('authToken', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('userRole', data.role);

        showNotification(`Welcome ${data.username}! (${data.role})`, 'success');
        updateView();
    } catch (error) {
        console.error('Login error:', error);
        showNotification(error.message || 'Login failed. Please check your credentials.', 'error');
    }
});

const logout = () => {
    state.token = null;
    state.username = null;
    state.role = null;
    state.affiliates = [];
    localStorage.clear();
    updateView();
};

// ==================== AFFILIATES ====================

const loadAffiliates = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/v1/affiliates', {
            headers: getHeaders()
        });

        if (!response.ok) {
            if (response.status === 401) {
                logout();
                return;
            }
            throw new Error('Failed to load affiliates');
        }

        const affiliates = await response.json();
        state.affiliates = affiliates;
        renderAffiliates(affiliates);
        populateAffiliateDropdown(affiliates);

    } catch (error) {
        console.error('Load affiliates error:', error);
        showNotification(error.message || 'Failed to load affiliates', 'error');
        renderAffiliatesEmpty('Failed to load affiliates');
    }
};

const renderAffiliates = (affiliates) => {
    if (!elements.affiliatesBody) return;

    if (!affiliates || affiliates.length === 0) {
        renderAffiliatesEmpty('No affiliates registered yet');
        return;
    }

    elements.affiliatesBody.innerHTML = affiliates
        .map((aff) => `
            <tr>
                <td><strong>${aff.id}</strong></td>
                <td>${aff.document}</td>
                <td>${aff.name}</td>
                <td>${formatCurrency(aff.salary)}</td>
                <td>${aff.affiliationDate}</td>
                <td>${aff.monthsSinceAffiliation || 'N/A'}</td>
                <td><span class="status status-${aff.status?.toLowerCase() || 'active'}">${aff.status || 'ACTIVE'}</span></td>
            </tr>
        `)
        .join('');
};

const renderAffiliatesEmpty = (message) => {
    if (!elements.affiliatesBody) return;
    elements.affiliatesBody.innerHTML = `
        <tr>
            <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                <p style="margin: 0; font-size: 1.1rem;">📭 ${message}</p>
                <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem;">Create a new affiliate using the form below</p>
            </td>
        </tr>
    `;
};

const populateAffiliateDropdown = (affiliates) => {
    if (!elements.affiliateSelect) return;

    // Keep the default option
    elements.affiliateSelect.innerHTML = '<option value="">-- Select an affiliate --</option>';

    affiliates.forEach(aff => {
        const option = document.createElement('option');
        option.value = aff.id;
        option.textContent = `ID: ${aff.id} - ${aff.name} (${aff.document})`;
        elements.affiliateSelect.appendChild(option);
    });
};

const createAffiliate = async (e) => {
    e.preventDefault();
    const docNumber = document.getElementById('docNumber').value;
    const name = document.getElementById('name').value;
    const salaryInput = document.getElementById('salary');
    const salary = salaryInput ? salaryInput.value : 0;

    const payload = {
        document: docNumber,
        name: name,
        salary: Number(salary),
        affiliationDate: new Date(Date.now() - 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] // 12 months ago
    };

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

        const newAffiliate = await response.json();

        // Show success with ID prominently
        showNotification(`✅ Affiliate "${newAffiliate.name}" created with ID: ${newAffiliate.id}`, 'success');

        // Show result box with ID
        if (elements.affiliateResult) {
            elements.affiliateResult.classList.remove('hidden');
            elements.affiliateResult.innerHTML = `
                <div style="background: var(--success-color); color: white; padding: 1rem; border-radius: 8px; margin-top: 1rem;">
                    <strong>✅ Created Successfully!</strong><br>
                    <span style="font-size: 1.5rem;">ID: ${newAffiliate.id}</span><br>
                    <small>Name: ${newAffiliate.name} | Document: ${newAffiliate.document}</small>
                </div>
            `;
        }

        forms.affiliate.reset();
        loadAffiliates(); // Refresh the list and dropdown

    } catch (error) {
        console.error('Create affiliate error:', error);
        showNotification(error.message || 'Failed to create affiliate', 'error');
    }
};

// ==================== CREDIT APPLICATIONS ====================

const createCreditApplication = async (e) => {
    e.preventDefault();
    const affiliateId = document.getElementById('affiliateId').value;
    const amount = document.getElementById('amount').value;
    const term = document.getElementById('term').value;

    if (!affiliateId) {
        showNotification('Please select an affiliate', 'error');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/v1/credit-applications', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({
                affiliateId: parseInt(affiliateId),
                requestedAmount: parseFloat(amount),
                termMonths: parseInt(term)
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.detail || 'Failed to submit application');
        }

        const result = await response.json();
        showNotification(`Credit application #${result.id} submitted successfully!`, 'success');
        forms.credit.reset();
        loadApplications();
    } catch (error) {
        console.error('Create application error:', error);
        showNotification(error.message || 'Failed to submit application', 'error');
    }
};

const loadApplications = async () => {
    try {
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
        renderApplications(applications);

    } catch (error) {
        console.error('Load error:', error);
        showNotification(error.message || 'Failed to load applications', 'error');
        renderApplicationsEmpty('Failed to load applications');
    }
};

const evaluateApplication = async (id) => {
    try {
        showNotification(`Evaluating application #${id}...`, 'info');

        const response = await fetch(`http://localhost:8080/api/evaluations/${id}`, {
            method: 'POST',
            headers: getHeaders()
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.detail || 'Evaluation failed');
        }

        const result = await response.json();

        const statusEmoji = result.approved ? '✅' : '❌';
        const statusText = result.approved ? 'APPROVED' : 'REJECTED';

        showNotification(
            `${statusEmoji} Application #${id}: ${statusText} | Risk: ${result.riskLevel} (Score: ${result.riskScore})`,
            result.approved ? 'success' : 'error'
        );

        loadApplications();
    } catch (error) {
        console.error('Evaluation error:', error);
        showNotification(error.message || 'Evaluation failed', 'error');
    }
};

const renderApplications = (applications) => {
    if (!elements.applicationsBody) return;

    if (!applications || applications.length === 0) {
        renderApplicationsEmpty('No pending applications');
        return;
    }

    elements.applicationsBody.innerHTML = applications
        .map((app) => `
            <tr>
                <td>${app.id}</td>
                <td>
                    <strong>${app.affiliate?.name || 'N/A'}</strong><br>
                    <small style="color: var(--text-secondary);">${app.affiliate?.document || ''}</small>
                </td>
                <td>${new Date(app.applicationDate).toLocaleDateString()}</td>
                <td>${formatCurrency(app.requestedAmount)}</td>
                <td>${app.termMonths} months</td>
                <td><span class="status status-${(app.status || 'unknown').toLowerCase()}">${app.status || 'N/A'}</span></td>
                <td>
                    ${app.status === 'PENDING'
                ? `<button class="btn-action" onclick="evaluateApplication(${app.id})">🔍 Evaluate</button>`
                : `<span style="color: var(--text-secondary);">—</span>`
            }
                </td>
            </tr>
        `)
        .join('');
};

const renderApplicationsEmpty = (message) => {
    if (!elements.applicationsBody) return;
    elements.applicationsBody.innerHTML = `
        <tr>
            <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                <p style="margin: 0; font-size: 1.1rem;">📋 ${message}</p>
                <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem;">New applications will appear here</p>
            </td>
        </tr>
    `;
};

// ==================== INITIALIZATION ====================

document.addEventListener('DOMContentLoaded', () => {
    updateView();

    if (forms.affiliate) {
        forms.affiliate.addEventListener('submit', createAffiliate);
    }
    if (forms.credit) {
        forms.credit.addEventListener('submit', createCreditApplication);
    }
    if (elements.refreshBtn) {
        elements.refreshBtn.addEventListener('click', loadApplications);
    }
    if (elements.refreshAffiliatesBtn) {
        elements.refreshAffiliatesBtn.addEventListener('click', loadAffiliates);
    }
    if (elements.logoutBtn) {
        elements.logoutBtn.addEventListener('click', logout);
    }
});

// Expose function to global scope for HTML onclick
window.evaluateApplication = evaluateApplication;
