(async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
        const res = await fetch("http://localhost:5000/api/history", {
            headers: { Authorization: "Bearer " + token }
        });

        if (!res.ok) return;

        const history = await res.json();
        const tbody = document.getElementById("historyBody");
        tbody.innerHTML = "";

        history.forEach(r => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${r.acceptor_name}</td>
                <td>${r.mobile_no}</td>
                <td>${r.blood}</td>
                <td>${r.district_name || '-'}</td>
                <td>${r.city_name || '-'}</td>
                <td>${r.quantity}</td>
                <td>${r.status}</td>
            `;
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.error("HISTORY LOAD ERROR:", e);
    }
})();
