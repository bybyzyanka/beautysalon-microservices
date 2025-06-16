import { getData, postData, putData, deleteData, fetchPageableData } from "./api.js";
import { renderList, renderPagination, showToast } from "./ui.js";

let currentPage = 0;
let currentFacilityId = null;

document.addEventListener("DOMContentLoaded", async () => {
    await initializeFacilityManagement();
});

async function initializeFacilityManagement() {
    const modal = document.getElementById("edit-facility-modal");
    const closeBtn = document.querySelector(".close-btn");
    const addBtn = document.querySelector(".add-btn");
    const searchBtn = document.querySelector(".search-btn");
    const facilityNameInput = document.getElementById("facility-name");
    const facilityPriceInput = document.getElementById("facility-price");
    const editForm = document.getElementById("edit-facility-form");
    const deleteFacilityBtn = document.getElementById("delete-facility-btn");

    const closeModal = () => modal.classList.add("hidden");
    const openModal = () => modal.classList.remove("hidden");

    closeBtn.addEventListener("click", closeModal);

    addBtn.addEventListener("click", () => {
        currentFacilityId = null;
        facilityNameInput.value = "";
        facilityPriceInput.value = "";
        document.getElementById("modal-title").textContent = "Add Facility";
        deleteFacilityBtn.style.display = "none";
        openModal();
    });

    deleteFacilityBtn.addEventListener("click", async () => {
        if (!currentFacilityId) return;

        const confirmed = confirm("Are you sure you want to delete this facility?");
        if (confirmed) {
            try {
                await deleteData(`/facility/${currentFacilityId}`);
                closeModal();
                await loadFacilities();
            } catch (error) {
                const errorMessage = error.message || "Failed to delete facility.";
                showToast(errorMessage, "error");
            }
        }
    });

    searchBtn.addEventListener("click", async () => {
        currentPage = 0;
        await loadFacilities();
    });

    editForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const updatedFacility = {
            name: facilityNameInput.value.trim(),
            price: parseFloat(facilityPriceInput.value.trim()),
        };

        const method = currentFacilityId ? putData : postData;
        const url = currentFacilityId ? `/facility/${currentFacilityId}` : "/facility";

        try {
            await method(url, updatedFacility);
            closeModal();
            showToast(currentFacilityId ? "Facility updated successfully!" : "Facility added successfully!", "notification");
            await loadFacilities();
        } catch (error) {
            const errorMessage = error.message || "Failed to save facility.";
            showToast(errorMessage, "error");
        }
    });

    document.getElementById("facility-list").addEventListener("click", async (event) => {
        if (event.target.classList.contains("edit-btn")) {
            currentFacilityId = event.target.getAttribute("data-id");

            try {
                const facility = await getData(`/facility/${currentFacilityId}`);
                facilityNameInput.value = facility.name || "";
                facilityPriceInput.value = facility.price || "";
                document.getElementById("modal-title").textContent = "Edit Facility";
                deleteFacilityBtn.style.display = "block";
                openModal();
            } catch (error) {
                const errorMessage = error.message || "Failed to fetch facility data.";
                showToast(errorMessage, "error");
            }
        }
    });

    await loadFacilities();
}

async function loadFacilities() {
    const searchBar = document.querySelector(".search-bar");
    const searchQuery = searchBar.value.trim();

    try {
        const data = await fetchPageableData("/facility", searchQuery, currentPage);
        const totalPages = data.totalPages || 0;
        renderList(data.content, "facility-list", renderFacility);
        renderPagination(totalPages, currentPage, async (page) => {
            currentPage = page;
            await loadFacilities();
        });
    } catch (error) {
        const errorMessage = error.message || "Failed to load facilities.";
        showToast(errorMessage, "error");
    }
}

function renderFacility(facility) {
    const facilityBlock = document.createElement("div");
    facilityBlock.className = "facility-block";

    facilityBlock.innerHTML = `
        <div class="facility-info">
            <h3>${facility.name}</h3>
            <p><strong>Price:</strong> $${facility.price.toFixed(2)}</p>
        </div>
        <div class="facility-actions">
            <button class="edit-btn" data-id="${facility.id}">Edit</button>
        </div>
    `;

    return facilityBlock;
}