export function renderList(items, containerId, renderItem) {
    const container = document.getElementById(containerId);
    container.innerHTML = ""; // Clear existing items

    // if (items.length === 0) {
    //     container.innerHTML = "<p>No items found</p>";
    //     return;
    // }

    items.forEach(item => {
        const element = renderItem(item);
        container.appendChild(element);
    });
}

export function renderPagination(totalPages, currentPage, onPageChange) {
    const paginationContainer = document.getElementById("pagination");
    paginationContainer.innerHTML = ""; // Clear existing pagination buttons

    for (let i = 0; i < totalPages; i++) {
        const button = document.createElement("button");
        button.textContent = i + 1;
        button.disabled = i === currentPage;
        button.addEventListener("click", () => onPageChange(i));
        paginationContainer.appendChild(button);
    }

    if (totalPages === 0) {
        const button = document.createElement("button");
        button.textContent = "1";
        button.disabled = true;
        paginationContainer.appendChild(button);
    }
}

export function showToast(message, type = "success") {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.className = "toast";
    toast.classList.add(type, "visible");

    setTimeout(() => {
        toast.classList.remove("visible");
    }, 3000);
}