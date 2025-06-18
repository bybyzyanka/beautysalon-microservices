import {showToast} from "./ui.js";

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

    // Open the modal
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

    // Add click event to schedule blocks
    scheduleList.addEventListener("click", (event) => {
        const block = event.target.closest(".schedule-block");
        if (block) {
            const scheduleId = block.dataset.id;
            openEditModal(scheduleId);
        }
    });

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

    // Delete schedule
    const deleteScheduleBtn = document.getElementById("delete-schedule-btn");
    if (deleteScheduleBtn) {
        deleteScheduleBtn.addEventListener("click", async () => {
            const scheduleId = document.getElementById("edit-schedule-id").value;
            if (confirm("Are you sure you want to delete this schedule?")) {
                try {
                    const response = await fetch(`/api/schedule/${scheduleId}`, {
                        method: "DELETE",
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

    // Load dropdown options
    loadDropdownOptions();
});

async function loadDropdownOptions() {
    try {
        // Load facilities
        const facilitiesResponse = await fetch("/api/facility?page=0&size=1000");
        if (facilitiesResponse.ok) {
            const facilitiesData = await facilitiesResponse.json();
            const facilities = facilitiesData.content || facilitiesData;

            const facilitySelects = document.querySelectorAll("#facility, #edit-facility");
            facilitySelects.forEach(select => {
                select.innerHTML = '<option value="" disabled selected>Select a facility</option>';
                facilities.forEach(facility => {
                    const option = document.createElement("option");
                    option.value = facility.id;
                    option.textContent = facility.name;
                    select.appendChild(option);
                });
            });
        }

        // Load clients
        const clientsResponse = await fetch("/api/client?page=0&size=1000");
        if (clientsResponse.ok) {
            const clientsData = await clientsResponse.json();
            const clients = clientsData.content || clientsData;

            const clientSelects = document.querySelectorAll("#client, #edit-client");
            clientSelects.forEach(select => {
                select.innerHTML = '<option value="" disabled selected>Select a client</option>';
                clients.forEach(client => {
                    const option = document.createElement("option");
                    option.value = client.id;
                    option.textContent = `${client.name} (${client.email})`;
                    select.appendChild(option);
                });
            });
        }

        // Load masters
        const mastersResponse = await fetch("/api/master?page=0&size=1000");
        if (mastersResponse.ok) {
            const mastersData = await mastersResponse.json();
            const masters = mastersData.content || mastersData;

            const masterSelects = document.querySelectorAll("#master, #edit-master");
            masterSelects.forEach(select => {
                select.innerHTML = '<option value="" disabled selected>Select a master</option>';
                masters.forEach(master => {
                    const option = document.createElement("option");
                    option.value = master.id;
                    option.textContent = `${master.name} (${master.email})`;
                    select.appendChild(option);
                });
            });
        }
    } catch (error) {
        console.error("Error loading dropdown options:", error);
        showToast("Failed to load dropdown options", "error");
    }
}