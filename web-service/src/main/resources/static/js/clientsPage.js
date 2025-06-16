import { getData, postData, putData, deleteData, fetchPageableData } from "./api.js";
import { renderList, renderPagination, showToast } from "./ui.js";

let currentPage = 0;
let currentClientId = null;

document.addEventListener("DOMContentLoaded", async () => {
    await initializeClientManagement();
});

async function initializeClientManagement() {
    const modal = document.getElementById("edit-client-modal");
    const closeBtn = document.querySelector(".close-btn");
    const addBtn = document.querySelector(".add-btn");
    const searchBtn = document.querySelector(".search-btn");
    const clientNameInput = document.getElementById("client-name");
    const clientPhoneInput = document.getElementById("client-phone");
    const clientEmailInput = document.getElementById("client-email");
    const editForm = document.getElementById("edit-client-form");
    const deleteClientBtn = document.getElementById("delete-client-btn");

    const closeModal = () => modal.classList.add("hidden");
    const openModal = () => modal.classList.remove("hidden");

    closeBtn.addEventListener("click", closeModal);

    addBtn.addEventListener("click", () => {
        currentClientId = null;
        clientNameInput.value = "";
        clientPhoneInput.value = "";
        clientEmailInput.value = "";
        document.getElementById("modal-title").textContent = "Add Client";
        deleteClientBtn.style.display = "none";
        openModal();
    });

    deleteClientBtn.addEventListener("click", async () => {
        if (!currentClientId) return;

        const confirmed = confirm("Are you sure you want to delete this client?");
        if (confirmed) {
            try {
                await deleteData(`/client/${currentClientId}`);
                closeModal();
                await loadClients();
            } catch (error) {
                const errorMessage = error.message || "Failed to delete client.";
                showToast(errorMessage, "error");
            }
        }
    });

    searchBtn.addEventListener("click", async () => {
        currentPage = 0;
        await loadClients();
    });

    editForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const updatedClient = {
            name: clientNameInput.value.trim(),
            phone: clientPhoneInput.value.trim(),
            email: clientEmailInput.value.trim(),
        };

        const method = currentClientId ? putData : postData;
        const url = currentClientId ? `/client/${currentClientId}` : "/client";

        try {
            await method(url, updatedClient);
            closeModal();
            showToast(currentClientId ? "Client updated successfully!" : "Client added successfully!", "notification");
            await loadClients();
        } catch (error) {
            const errorMessage = error.message || "Failed to save client.";
            showToast(errorMessage, "error");
        }
    });

    document.getElementById("client-list").addEventListener("click", async (event) => {
        if (event.target.classList.contains("edit-btn")) {
            currentClientId = event.target.getAttribute("data-id");

            try {
                const client = await getData(`/client/${currentClientId}`);
                clientNameInput.value = client.name || "";
                clientPhoneInput.value = client.phone || "";
                clientEmailInput.value = client.email || "";
                document.getElementById("modal-title").textContent = "Edit Client";
                deleteClientBtn.style.display = "block";
                openModal();
            } catch (error) {
                const errorMessage = error.message || "Failed to fetch client data.";
                showToast(errorMessage, "error");
            }
        }
    });

    await loadClients();
}

async function loadClients() {
    const searchBar = document.querySelector(".search-bar");
    const searchQuery = searchBar.value.trim();

    try {
        const data = await fetchPageableData("/client", searchQuery, currentPage);
        const totalPages = data.totalPages || 0;
        renderList(data.content, "client-list", renderClient);
        renderPagination(totalPages, currentPage, async (page) => {
            currentPage = page;
            await loadClients();
        });
    } catch (error) {
        const errorMessage = error.message || "Failed to load clients.";
        showToast(errorMessage, "error");
    }
}

function renderClient(client) {
    const clientBlock = document.createElement("div");
    clientBlock.className = "client-block";

    clientBlock.innerHTML = `
        <div class="client-info">
            <h3>${client.name}</h3>
            <p><strong>Phone:</strong> ${client.phone}</p>
            <p><strong>Email:</strong> ${client.email}</p>
        </div>
        <div class="client-actions">
            <button class="edit-btn" data-id="${client.id}">Edit</button>
        </div>
    `;

    return clientBlock;
}