async function loadRequests() {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
        const res = await fetch("http://localhost:5000/api/hospital/district-requests", {
            headers: { Authorization: "Bearer " + token }
        });

        if (!res.ok) return console.error("Could not fetch requests");
        const requests = await res.json();
        const tbody = document.getElementById("requestTableBody");
        tbody.innerHTML = "";

        if (requests.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No client requests found in your district.</td></tr>';
            return;
        }

        requests.forEach(r => {
            const tr = document.createElement("tr");

            // Define status options and which is selected
            const statuses = ["Pending", "Fulfilled", "Cancelled"];
            const options = statuses.map(s => `
                <option value="${s}" ${r.status === s ? 'selected' : ''}>${s}</option>
            `).join("");

            tr.innerHTML = `
                <td>${r.request_pk}</td>
                <td>${r.acceptor_name}</td>
                <td>${r.blood}</td>
                <td>${r.quantity}</td>
                <td>${r.city_name}</td>
                <td>
                    <select id="status-${r.request_pk}">
                        ${options}
                    </select>
                </td>
                <td>
                    <button class="primary-btn" onclick="updateStatus(${r.request_pk})">Update</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("LOAD ERROR:", err);
    }
}

async function updateStatus(pk) {
    const token = localStorage.getItem("token");
    const status = document.getElementById(`status-${pk}`).value;

    try {
        const res = await fetch(`http://localhost:5000/api/hospital/requests/${pk}/status`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({ status })
        });

        const data = await res.json();
        if (res.ok) {
            alert("Status updated successfully!");
            loadRequests(); // Refresh table
        } else {
            alert(data.msg || "Update failed");
        }
    } catch (err) {
        console.error("UPDATE ERROR:", err);
        alert("An error occurred");
    }
}

loadRequests();
