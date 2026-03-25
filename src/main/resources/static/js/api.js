const API_URL = 'http://localhost:8080/api/v1';

let currentLang = localStorage.getItem('lang') || 'en';

/**
 * Get stored tokens
 */
function getTokens() {
    return {
        accessToken: localStorage.getItem('accessToken'),
        refreshToken: localStorage.getItem('refreshToken')
    };
}

/**
 * Save tokens
 */
function saveTokens(access, refresh) {
    localStorage.setItem('accessToken', access);
    localStorage.setItem('refreshToken', refresh);
}

/**
 * Clear tokens
 */
function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
}

/**
 * Global API Fetch Wrapper with automatic Token refresh
 */
async function apiFetch(endpoint, options = {}) {
    const { accessToken } = getTokens();

    const headers = {
        'Content-Type': 'application/json',
        'Accept-Language': currentLang,
        ...options.headers
    };

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    try {
        let response = await fetch(`${API_URL}${endpoint}`, {
            ...options,
            headers
        });

        // Handle 401 Unauthorized (Expired Token)
        if (response.status === 401 && accessToken) {
            const { refreshToken } = getTokens();
            if (!refreshToken) {
                forceLogout();
                return null;
            }

            // Attempt to refresh
            const refreshRes = await fetch(`${API_URL}/auth/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken })
            });

            if (refreshRes.ok) {
                const data = await refreshRes.json();
                saveTokens(data.data.accessToken, data.data.refreshToken);
                // Retry original request
                headers['Authorization'] = `Bearer ${data.data.accessToken}`;
                response = await fetch(`${API_URL}${endpoint}`, { ...options, headers });
            } else {
                forceLogout();
                return null;
            }
        }

        const data = await response.json();
        
        if (!response.ok || (data && data.success === false)) {
            // Check for validation errors
            let errorMsg = data.message || 'An error occurred';
            if (data.errors) {
                errorMsg = Object.values(data.errors).join(', ');
            }
            throw new Error(errorMsg);
        }

        return data.data;

    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    }
}

function forceLogout() {
    clearTokens();
    window.location.reload();
}

/**
 * Toast Notifications
 */
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    container.appendChild(toast);
    
    // Animate in
    setTimeout(() => { toast.classList.add('show'); }, 10);
    
    // Animate out
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => { toast.remove(); }, 300);
    }, 4000);
}
