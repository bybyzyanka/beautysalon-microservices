// API utility functions - simplified without authentication

const BASE_URL = '/api';

// Generic fetch wrapper
async function fetchWithErrorHandling(url, options = {}) {
    try {
        const response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            let errorMessage = `HTTP ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch (e) {
                // If response is not JSON, use status text
                errorMessage = response.statusText || errorMessage;
            }
            throw new Error(errorMessage);
        }

        // Handle empty responses
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    }
}

// GET request
export async function getData(endpoint) {
    return fetchWithErrorHandling(`${BASE_URL}${endpoint}`);
}

// POST request
export async function postData(endpoint, data) {
    return fetchWithErrorHandling(`${BASE_URL}${endpoint}`, {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

// PUT request
export async function putData(endpoint, data) {
    return fetchWithErrorHandling(`${BASE_URL}${endpoint}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    });
}

// DELETE request
export async function deleteData(endpoint) {
    return fetchWithErrorHandling(`${BASE_URL}${endpoint}`, {
        method: 'DELETE'
    });
}

// Fetch pageable data with optional search
export async function fetchPageableData(endpoint, searchQuery = '', page = 0, size = 10) {
    let url = `${endpoint}?page=${page}&size=${size}`;
    if (searchQuery) {
        url += `&search=${encodeURIComponent(searchQuery)}`;
    }
    return getData(url);
}