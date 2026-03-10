async function loadDonors() {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
        const res = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/hospital/donors", {
            headers: { Authorization: "Bearer " + token }
        });

        if (!res.ok) return console.error("Could not fetch donors");
        const donors = await res.json();
        const tbody = document.getElementById("donorTableBody");
        tbody.innerHTML = "";

        if (donors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No donors found in your district.</td></tr>';
            return;
        }

        donors.forEach(d => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${d.name}</td>
                <td>${d.blood_type}</td>
                <td>${d.mobile_no}</td>
                <td>${d.gmail}</td>
                <td>${d.city_name}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error("LOAD ERROR:", err);
    }
}

loadDonors();
