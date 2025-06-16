import { getData, postData, putData, deleteData, fetchPageableData } from "./api.js";
import { renderList, renderPagination, showToast } from "./ui.js";

let currentPage = 0;
let currentMasterId = null;

document.addEventListener("DOMContentLoaded", async () => {
    await initializeMasterManagement();
});

async function initializeMasterManagement() {
    const modal = document.getElementById("edit-master-modal");
    const closeBtn = document.querySelector(".close-btn");
    const addBtn = document.querySelector(".add-btn");
    const searchBtn = document.querySelector(".search-btn");
    const masterNameInput = document.getElementById("master-name");
    const masterPhoneInput = document.getElementById("master-phone");
    const masterEmailInput = document.getElementById("master-email");
    const masterPasswordInput = document.getElementById("master-password");
    const editForm = document.getElementById("edit-master-form");
    const deleteMasterBtn = document.getElementById("delete-master-btn");
    const facilitiesContainer = document.getElementById("master-facilities");

    async function loadFacilities() {
        try {
            const response = await getData("/facility?page=0&size=1000");
            const facilities = response.content || response;

            if (!Array.isArray(facilities)) {
                throw new Error("Facilities data is not an array");
            }

            facilitiesContainer.innerHTML = "";

            facilities.forEach(facility => {
                const checkboxContainer = document.createElement("div");
                checkboxContainer.className = "checkbox-container";

                const checkbox = document.createElement("input");
                checkbox.type = "checkbox";
                checkbox.id = `facility-${facility.id}`;
                checkbox.value = facility.id;
                checkbox.name = "facilities";
                checkbox.setAttribute("data-name", facility.name);

                const label = document.createElement("label");
                label.htmlFor = `facility-${facility.id}`;
                label.textContent = facility.name;

                checkboxContainer.appendChild(checkbox);
                checkboxContainer.appendChild(label);
                facilitiesContainer.appendChild(checkboxContainer);
            });
        } catch (error) {
            console.error("Failed to load facilities:", error);
            showToast("Failed to load facilities", "error");
        }
    }

    await loadFacilities();

    const closeModal = () => modal.classList.add("hidden");
    const openModal = () => modal.classList.remove("hidden");

    const clearForm = () => {
        masterNameInput.value = "";
        masterPhoneInput.value = "";
        masterEmailInput.value = "";
        masterPasswordInput.value = "";
        const checkboxes = facilitiesContainer.querySelectorAll("input[type='checkbox']");
        checkboxes.forEach(checkbox => {
            checkbox.checked = false;
        });
    };

    closeBtn.addEventListener("click", closeModal);

    addBtn.addEventListener("click", () => {
        currentMasterId = null;
        clearForm();
        document.getElementById("modal-title").textContent = "Add Master";
        deleteMasterBtn.style.display = "none";
        openModal();
    });

    deleteMasterBtn.addEventListener("click", async () => {
        if (!currentMasterId) return;

        const confirmed = confirm("Are you sure you want to delete this master?");
        if (confirmed) {
            try {
                await deleteData(`/master/${currentMasterId}`);
                closeModal();
                showToast("Master deleted successfully!", "notification");
                await loadMasters();
            } catch (error) {
                const errorMessage = error.message || "Failed to delete master.";
                showToast(errorMessage, "error");
            }
        }
    });

    searchBtn.addEventListener("click", async () => {
        currentPage = 0;
        await loadMasters();
    });

    editForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const selectedFacilities = Array.from(facilitiesContainer.querySelectorAll("input[type='checkbox']:checked"))
            .map(checkbox => ({
                id: parseInt(checkbox.value),
                name: checkbox.getAttribute("data-name"),
            }));

        const updatedMaster = {
            name: masterNameInput.value.trim(),
            phone: masterPhoneInput.value.trim(),
            email: masterEmailInput.value.trim(),
            password: masterPasswordInput.value.trim(),
            facilities: selectedFacilities,
        };

        const method = currentMasterId ? putData : postData;
        const url = currentMasterId ? `/master/${currentMasterId}` : `/master`;

        try {
            await method(url, updatedMaster);
            closeModal();
            showToast(currentMasterId ? "Master updated successfully!" : "Master added successfully!", "notification");
            await loadMasters();
        } catch (error) {
            const errorMessage = error.message || "Failed to save master.";
            showToast(errorMessage, "error");
        }
    });

    document.getElementById("master-list").addEventListener("click", async (event) => {
        if (event.target.classList.contains("edit-btn")) {
            currentMasterId = event.target.getAttribute("data-id");

            try {
                const master = await getData(`/master/${currentMasterId}`);
                masterNameInput.value = master.name || "";
                masterPhoneInput.value = master.phone || "";
                masterEmailInput.value = master.email || "";
                masterPasswordInput.value = "";

                const checkboxes = facilitiesContainer.querySelectorAll("input[type='checkbox']");
                checkboxes.forEach(checkbox => {
                    checkbox.checked = false;
                });

                if (master.facilities && Array.isArray(master.facilities)) {
                    master.facilities.forEach(facility => {
                        const checkbox = facilitiesContainer.querySelector(`input[value="${facility.id}"]`);
                        if (checkbox) {
                            checkbox.checked = true;
                        }
                    });
                }

                document.getElementById("modal-title").textContent = "Edit Master";
                deleteMasterBtn.style.display = "block";
                openModal();
            } catch (error) {
                const errorMessage = error.message || "Failed to fetch master data.";
                showToast(errorMessage, "error");
            }
        }
    });

    await loadMasters();
}

async function loadMasters() {
    const searchBar = document.querySelector(".search-bar");
    const searchQuery = searchBar.value.trim();

    try {
        const data = await fetchPageableData("/master", searchQuery, currentPage);
        const totalPages = data.totalPages || 0;
        renderList(data.content, "master-list", renderMaster);
        renderPagination(totalPages, currentPage, async (page) => {
            currentPage = page;
            await loadMasters();
        });
    } catch (error) {
        const errorMessage = error.message || "Failed to load masters.";
        showToast(errorMessage, "error");
    }
}

function renderMaster(master) {
    const masterBlock = document.createElement("div");
    masterBlock.className = "master-block";

    const facilitiesText = master.facilities && master.facilities.length > 0
        ? master.facilities.map(f => f.name).join(", ")
        : "No facilities assigned";

    masterBlock.innerHTML = `
        <div class="master-info">
            <h3>${master.name}</h3>
            <p><strong>Phone:</strong> ${master.phone}</p>
            <p><strong>Email:</strong> ${master.email}</p>
            <p><strong>Facilities:</strong> ${facilitiesText}</p>
        </div>
        <div class="master-actions">
            <button class="edit-btn" data-id="${master.id}">Edit</button>
        </div>
    `;

    return masterBlock;
}