/**
 * API Service for Credit Application System
 * Handles all HTTP requests to the backend with JWT authentication
 */

const API_BASE_URL = 'http://localhost:8080/api';

class ApiService {
    constructor() {
        this.baseUrl = API_BASE_URL;
        this.token = localStorage.getItem('authToken');
    }

    /**
     * Get authorization headers with JWT token
     */
    getHeaders() {
        const headers = {
            'Content-Type': 'application/json',
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        return headers;
    }

    /**
     * Handle API response
     */
    async handleResponse(response) {
        if (!response.ok) {
            const error = await response.json().catch(() => ({
                message: `HTTP ${response.status}: ${response.statusText}`
            }));
            throw new Error(error.detail || error.message || 'Request failed');
        }

        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    /**
     * Make HTTP request
     */
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            ...options,
            headers: {
                ...this.getHeaders(),
                ...options.headers,
            },
        };

        try {
            const response = await fetch(url, config);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // ==================== Authentication ====================

    /**
     * Login user and store JWT token
     */
    async login(username, password) {
        const data = await this.request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password }),
        });

        if (data.token) {
            this.token = data.token;
            localStorage.setItem('authToken', data.token);
            localStorage.setItem('currentUser', JSON.stringify({
                username: data.username,
                email: data.email,
                role: data.role,
            }));
        }

        return data;
    }

    /**
     * Register new user
     */
    async register(userData) {
        return await this.request('/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData),
        });
    }

    /**
     * Logout user and clear stored data
     */
    logout() {
        this.token = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('currentUser');
    }

    /**
     * Get current logged in user
     */
    getCurrentUser() {
        const userStr = localStorage.getItem('currentUser');
        return userStr ? JSON.parse(userStr) : null;
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return !!this.token;
    }

    // ==================== Affiliates ====================

    /**
     * Get all affiliates (ANALYST, ADMIN)
     */
    async getAffiliates() {
        return await this.request('/affiliates');
    }

    /**
     * Get affiliate by ID
     */
    async getAffiliate(id) {
        return await this.request(`/affiliates/${id}`);
    }

    /**
     * Create new affiliate (ADMIN)
     */
    async createAffiliate(affiliateData) {
        return await this.request('/affiliates', {
            method: 'POST',
            body: JSON.stringify(affiliateData),
        });
    }

    /**
     * Update affiliate (ADMIN)
     */
    async updateAffiliate(id, affiliateData) {
        return await this.request(`/affiliates/${id}`, {
            method: 'PUT',
            body: JSON.stringify(affiliateData),
        });
    }

    // ==================== Credit Applications ====================

    /**
     * Get all credit applications (filtered by role)
     */
    async getCreditApplications() {
        return await this.request('/credit-applications');
    }

    /**
     * Get credit application by ID
     */
    async getCreditApplication(id) {
        return await this.request(`/credit-applications/${id}`);
    }

    /**
     * Create new credit application (AFFILIATE)
     */
    async createCreditApplication(applicationData) {
        return await this.request('/credit-applications', {
            method: 'POST',
            body: JSON.stringify(applicationData),
        });
    }

    /**
     * Evaluate credit application (ANALYST)
     */
    async evaluateCreditApplication(id) {
        return await this.request(`/credit-applications/${id}/evaluate`, {
            method: 'POST',
        });
    }

    /**
     * Approve credit application (ADMIN)
     */
    async approveCreditApplication(id) {
        return await this.request(`/credit-applications/${id}/approve`, {
            method: 'POST',
        });
    }

    /**
     * Reject credit application (ADMIN)
     */
    async rejectCreditApplication(id, reason) {
        return await this.request(`/credit-applications/${id}/reject`, {
            method: 'POST',
            body: JSON.stringify({ reason }),
        });
    }
}

// Create singleton instance
const apiService = new ApiService();

// Export for use in other modules
export default apiService;
