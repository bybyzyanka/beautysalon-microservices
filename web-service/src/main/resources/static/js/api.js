export async function getData(url) {
    try {
        const response = await fetch(`/api${url}`, { method: "GET" });
        if (response.ok) {
            return await response.json();
        } else {
            const error = await response.json();
            throw new Error(error.message || `Failed to fetch data from ${url}.`);
        }
    } catch (error) {
        console.error("GET Error:", error);
        throw error;
    }
}

export async function postData(url, body) {
    try {
        const response = await fetch(`/api${url}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (response.ok) {
            return await response.json();
        } else {
            const error = await response.json();
            console.error("POST Error:", error);
            throw new Error(error.message || `Failed to post data to ${url}.`);
        }
    } catch (error) {
        console.error("POST Error:", error);
        throw error;
    }
}

export async function putData(url, body) {
    try {
        const response = await fetch(`/api${url}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body),
        });
        if (response.ok) {
            return await response.json();
        } else {
            const error = await response.json();
            throw new Error(error.message || `Failed to update data at ${url}.`);
        }
    } catch (error) {
        console.error("PUT Error:", error);
        throw error;
    }
}

export async function deleteData(url) {
    try {
        const response = await fetch(`/api${url}`, { method: "DELETE" });
        if (response.ok) {
            return true; // Return true to indicate successful deletion
        } else {
            const error = await response.json();
            throw new Error(error.message || `Failed to delete data at ${url}.`);
        }
    } catch (error) {
        console.error("DELETE Error:", error);
        throw error;
    }
}

export async function fetchPageableData(path, searchQuery = "", page = 0, size = 10) {
    try {
        const response = await fetch(`/api${path}?search=${encodeURIComponent(searchQuery)}&page=${page}&size=${size}`);
        if (response.ok) {
            const data = await response.json();

            // Extract data from the nested 'page' object
            return {
                content: data.content || [],
                totalPages: data.page?.totalPages || 0,
                totalElements: data.page?.totalElements || 0,
                number: data.page?.number || 0
            };
        } else {
            const error = await response.json();
            throw new Error(error.message || `Failed to fetch data from ${path}.`);
        }
    } catch (error) {
        console.error(`Error fetching data from ${path}:`, error);
        throw error;
    }
}

