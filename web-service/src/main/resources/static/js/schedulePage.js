import {showToast} from "./ui.js";

// Get user role from global variable or element on page
let userRole = null;

export const generateTimeline = (timeline) => {
    timeline.innerHTML = "";
    for (let hour = 0; hour < 24; hour++) {
        const timeBlock = document.createElement("div");
        timeBlock.className = "time-block";
        timeBlock.textContent = `${hour.toString().padStart(2, '0')}:00`;
        timeline.appendChild(timeBlock);
    }
};

export const parseLocalDateTime = (isoString) => {
    const parts = isoString.split(/[-T:.]/);
    return new Date(
        parts[0], // year
        parts[1] - 1, // month (0-based)
        parts[2], // day
        parts[3], // hours
        parts[4], // minutes
        parts[5] // seconds
    );
};

export const fetchSchedule = async (selectedDate, scheduleList) => {
    const response = await fetch(`/api/schedule`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(selectedDate)
    });

    if (response.ok) {
        const schedules = await response.json();
        scheduleList.innerHTML = "";

        const minuteHeight = 1; // 1px per minute
        const blockWidth = 150; // Fixed width for each block

        // Sort schedules by start time
        schedules.sort((a, b) => parseLocalDateTime(a.date) - parseLocalDateTime(b.date));

        // Create an array to track active time slots
        const timeSlots = [];

        schedules.forEach(schedule => {
            const block = document.createElement("div");
            block.className = "schedule-block";
            block.dataset.id = schedule.id;

            const blockHeight = Math.max(schedule.duration * minuteHeight, 30);
            const startTime = parseLocalDateTime(schedule.date);
            const startMinutes = (startTime.getHours() * 60) + startTime.getMinutes();
            const endMinutes = startMinutes + schedule.duration;

            let column = 0;
            while (timeSlotOccupied(timeSlots, column, startMinutes, endMinutes)) {
                column++;
            }

            timeSlots.push({
                column: column,
                startMinutes: startMinutes,
                endMinutes: endMinutes
            });

            const leftOffset = column * blockWidth;
            const topOffset = startMinutes * minuteHeight;

            block.style.height = `${blockHeight}px`;
            block.style.top = `${topOffset}px`;
            block.style.left = `${leftOffset}px`;

            const displayTime = `${String(startTime.getHours()).padStart(2, '0')}:${String(startTime.getMinutes()).padStart(2, '0')}`;

            const content = document.createElement("div");
            content.className = "schedule-block-content";

            content.innerHTML = `
        <h3>${schedule.facility?.name || "Unknown Facility"}</h3>
        <p><strong>Time:</strong> ${displayTime}</p>
        <p><strong>Duration:</strong> ${schedule.duration} minutes</p>
        <p><strong>Client:</strong> ${schedule.client?.name || "Unknown Client"} (${schedule.client?.email || "N/A"}, ${schedule.client?.phone || "N/A"})</p>
        <p><strong>Master:</strong> ${schedule.master?.name || "Unknown Master"} (${schedule.master?.email || "N/A"}, ${schedule.master?.phone || "N/A"})</p>
    `;

            block.appendChild(content);
            scheduleList.appendChild(block);
        });
    } else {
        console.error("Failed to fetch schedules");
    }
};

// Helper function to check if a time slot is already occupied in a specific column
function timeSlotOccupied(timeSlots, column, startMinutes, endMinutes) {
    return timeSlots.some(slot =>
        slot.column === column &&
        // Check for overlap: if the new event starts before another ends and ends after another starts
        !(endMinutes <= slot.startMinutes || startMinutes >= slot.endMinutes)
    );
}

document.addEventListener("DOMContentLoaded", () => {
    // Get user role from body or other element
    const roleElement = document.body.getAttribute('data-role') ||
        document.querySelector('[data-role]')?.getAttribute('data-role') ||
        'ROLE_MASTER'; // fallback
    userRole = roleElement;

    const scheduleModal = document.getElementById("schedule-modal");
    const openModalBtn = document.querySelector(".add-btn");
    const closeModalBtn = document.getElementById("close-schedule-modal");
    const scheduleForm = document.getElementById("schedule-form");

    const editScheduleModal = document.getElementById("edit-schedule-modal");
    const closeEditModalBtn = document.getElementById("close-edit-schedule-modal");
    const editScheduleForm = document.getElementById("edit-schedule-form");

    const dayPicker = document.getElementById("day-picker");
    const scheduleList = document.getElementById("schedule-list");
    const timeline = document.querySelector(".timeline");

    // Disable editing features for non-admin users
    if (userRole !== "ROLE_ADMIN") {
        if (openModalBtn) {
            openModalBtn.style.display = "none"; // Hide the "Add" button
        }
    }

    dayPicker.addEventListener("change", () => {
        const selectedDate = new Date(dayPicker.value);
        if (!isNaN(selectedDate)) {
            fetchSchedule(selectedDate.toISOString(), scheduleList);
        }
    });

    const today = new Date();
    dayPicker.value = today.toISOString().split("T")[0];
    generateTimeline(timeline);
    fetchSchedule(today.toISOString(), scheduleList);

    // Open the modal - check if button exists
    if (openModalBtn) {
        openModalBtn.addEventListener("click", () => {
            scheduleModal.classList.remove("hidden");
        });
    }

    // Close the modal
    if (closeModalBtn) {
        closeModalBtn.addEventListener("click", () => {
            scheduleModal.classList.add("hidden");
        });
    }

    // Submit the form
    if (scheduleForm) {
        scheduleForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            const formData = new FormData(scheduleForm);
            const scheduledFacility = {
                facility: { id: parseInt(formData.get("facility")) },
                client: { id: parseInt(formData.get("client")) },
                master: { id: parseInt(formData.get("master")) },
                date: formData.get("date"),
                duration: parseInt(formData.get("duration")),
            };

            try {
                const response = await fetch("/api/schedule/schedule-facility", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(scheduledFacility),
                });

                if (response.ok) {
                    showToast("Facility scheduled successfully!", "notification");
                    scheduleModal.classList.add("hidden");
                    scheduleForm.reset();
                    const selectedDate = new Date(dayPicker.value);
                    fetchSchedule(selectedDate.toISOString(), scheduleList);
                } else {
                    const errorData = await response.json();
                    const errorMessage = errorData.error || "Failed to schedule facility.";
                    showToast(errorMessage, "error");
                }
            } catch (error) {
                console.error("Error scheduling facility:", error);
                showToast("Network error occurred", "error");
            }
        });
    }

    // Close the edit modal
    if (closeEditModalBtn) {
        closeEditModalBtn.addEventListener("click", () => {
            editScheduleModal.classList.add("hidden");
        });
    }

    // Add click event to schedule blocks - only for admins
    if (userRole === "ROLE_ADMIN") {
        scheduleList.addEventListener("click", (event) => {
            const block = event.target.closest(".schedule-block");
            if (block) {
                const scheduleId = block.dataset.id;
                openEditModal(scheduleId);
            }
        });
    }

    // Open the edit modal and populate fields
    const openEditModal = async (scheduleId) => {
        try {
            const response = await fetch(`/api/schedule/${scheduleId}`);
            if (response.ok) {
                const schedule = await response.json();

                document.getElementById("edit-facility").value = schedule.facility.id;
                document.getElementById("edit-client").value = schedule.client.id;
                document.getElementById("edit-master").value = schedule.master.id;
                document.getElementById("edit-date").value = schedule.date;
                document.getElementById("edit-duration").value = schedule.duration;

                // Add hidden field with ID
                let hiddenIdField = document.getElementById("edit-schedule-id");
                if (!hiddenIdField) {
                    hiddenIdField = document.createElement("input");
                    hiddenIdField.type = "hidden";
                    hiddenIdField.id = "edit-schedule-id";
                    hiddenIdField.name = "id";
                    editScheduleForm.appendChild(hiddenIdField);
                }
                hiddenIdField.value = scheduleId;

                editScheduleModal.classList.remove("hidden");
            } else {
                console.error("Failed to fetch schedule details");
                showToast("Failed to load schedule details", "error");
            }
        } catch (error) {
            console.error("Error fetching schedule details:", error);
            showToast("Network error occurred", "error");
        }
    };

    // Submit the edit form
    if (editScheduleForm) {
        editScheduleForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            const formData = new FormData(editScheduleForm);
            const scheduleId = formData.get("id");
            const updatedSchedule = {
                id: parseInt(scheduleId),
                facility: { id: parseInt(formData.get("facility")) },
                client: { id: parseInt(formData.get("client")) },
                master: { id: parseInt(formData.get("master")) },
                date: formData.get("date"),
                duration: parseInt(formData.get("duration")),
            };

            try {
                const response = await fetch(`/api/schedule/edit/${scheduleId}`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(updatedSchedule),
                });

                if (response.ok) {
                    showToast("Schedule updated successfully!", "notification");
                    editScheduleModal.classList.add("hidden");
                    const selectedDate = new Date(dayPicker.value);
                    fetchSchedule(selectedDate.toISOString(), scheduleList);
                } else {
                    const errorData = await response.json();
                    const errorMessage = errorData.error || "Failed to update schedule.";
                    showToast(errorMessage, "error");
                }
            } catch (error) {
                console.error("Error updating schedule:", error);
                showToast("Network error occurred", "error");
            }
        });
    }

    // Delete schedule functionality
    const deleteScheduleBtn = document.getElementById("delete-schedule-btn");
    if (deleteScheduleBtn) {
        deleteScheduleBtn.addEventListener("click", async () => {
            const scheduleId = document.getElementById("edit-schedule-id")?.value;
            if (!scheduleId) {
                showToast("No schedule selected for deletion", "error");
                return;
            }

            const confirmed = confirm("Are you sure you want to delete this scheduled facility?");
            if (confirmed) {
                try {
                    const response = await fetch(`/api/schedule/delete/${scheduleId}`, {
                        method: "DELETE"
                    });

                    if (response.ok) {
                        showToast("Schedule deleted successfully!", "notification");
                        editScheduleModal.classList.add("hidden");
                        const selectedDate = new Date(dayPicker.value);
                        fetchSchedule(selectedDate.toISOString(), scheduleList);
                    } else {
                        const errorData = await response.json();
                        const errorMessage = errorData.error || "Failed to delete schedule.";
                        showToast(errorMessage, "error");
                    }
                } catch (error) {
                    console.error("Error deleting schedule:", error);
                    showToast("Network error occurred", "error");
                }
            }
        });
    }
});

document.addEventListener("DOMContentLoaded", async () => {
    const clientSelectAdd = document.getElementById("client");
    const masterSelectAdd = document.getElementById("master");
    const facilitySelectAdd = document.getElementById("facility");

    const clientSelectEdit = document.getElementById("edit-client");
    const masterSelectEdit = document.getElementById("edit-master");
    const facilitySelectEdit = document.getElementById("edit-facility");

    // Helper function to populate dropdowns
    const populateDropdown = async (url, selectAdd, selectEdit, labelFormatter) => {
        try {
            const response = await fetch(`/api${url}`);
            if (response.ok) {
                const data = await response.json();
                if (data.content && Array.isArray(data.content)) {
                    data.content.forEach(item => {
                        if (selectAdd) {
                            const optionAdd = document.createElement("option");
                            optionAdd.value = item.id;
                            optionAdd.textContent = labelFormatter(item);
                            selectAdd.appendChild(optionAdd);
                        }

                        if (selectEdit) {
                            const optionEdit = document.createElement("option");
                            optionEdit.value = item.id;
                            optionEdit.textContent = labelFormatter(item);
                            selectEdit.appendChild(optionEdit);
                        }
                    });
                }
            } else {
                console.error(`Failed to fetch data from ${url}`);
            }
        } catch (error) {
            console.error(`Error fetching data from ${url}:`, error);
        }
    };

    // Populate clients
    await populateDropdown(
        "/client?size=1000",
        clientSelectAdd,
        clientSelectEdit,
        client => `${client.name} (${client.email})`
    );

    // Populate masters
    await populateDropdown(
        "/master?size=1000",
        masterSelectAdd,
        masterSelectEdit,
        master => `${master.name} (${master.email})`
    );

    // Populate facilities
    await populateDropdown(
        "/facility?size=1000",
        facilitySelectAdd,
        facilitySelectEdit,
        facility => facility.name
    );
});