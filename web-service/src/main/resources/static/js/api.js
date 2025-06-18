// API utility functions with improved error handling

const BASE_URL = '/api';

// Generic fetch wrapper with better error handling
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

                // Handle different error response formats
                if (errorData.error) {
                    errorMessage = errorData.error;
                } else if (errorData.message) {
                    errorMessage = errorData.message;
                } else if (errorData.details) {
                    errorMessage = errorData.details;
                } else if (typeof errorData === 'string') {
                    errorMessage = errorData;
                } else {
                    errorMessage = `${response.status} ${response.statusText}`;
                }
            } catch (parseError) {
                // If response is not JSON, try to get text
                try {
                    const errorText = await response.text();
                    if (errorText) {
                        errorMessage = errorText;
                    } else {
                        errorMessage = `${response.status} ${response.statusText}`;
                    }
                } catch (textError) {
                    errorMessage = `${response.status} ${response.statusText}`;
                }
            }

            const error = new Error(errorMessage);
            error.status = response.status;
            throw error;
        }

        // Handle empty responses
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const text = await response.text();
            return text ? JSON.parse(text) : null;
        }
        return null;
    } catch (error) {
        console.error('API Error:', error);

        // Handle network errors
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            error.message = 'Network error: Unable to connect to server';
        }

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