let allCities = [];

async function loadRequestPage() {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
        // 1. Load User Profile (to get blood type)
        const profRes = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/profile", {
            headers: { Authorization: "Bearer " + token }
        });
        if (profRes.ok) {
            const u = await profRes.json();
            document.getElementById("uBlood").textContent = u.blood_type || "Unknown";
        }

        // 2. Load Cities/Districts
        const cityRes = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/cities");
        allCities = await cityRes.json();

        const dSel = document.getElementById("rDistrict");
        const dists = [...new Set(allCities.map(c => c.district_name))];
        dists.forEach(d => {
            const opt = document.createElement("option");
            opt.value = d; opt.textContent = d;
            dSel.appendChild(opt);
        });

        dSel.addEventListener("change", (e) => {
            updateCities(e.target.value);
        });

    } catch (e) {
        console.error("LOAD ERROR:", e);
    }
}

function updateCities(districtName) {
    const cSel = document.getElementById("rCity");
    cSel.innerHTML = '<option value="">Select City</option>';
    allCities.filter(c => c.district_name === districtName).forEach(c => {
        const opt = document.createElement("option");
        opt.value = c.city_id; opt.textContent = c.city_name;
        cSel.appendChild(opt);
    });
}

async function submitRequest() {
    const city_id = document.getElementById("rCity").value;
    const quantity = document.getElementById("rQuantity").value;
    const token = localStorage.getItem("token");

    if (!city_id || !quantity) {
        return alert("Please fill all fields");
    }

    try {
        const res = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/request", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({ city_id, quantity })
        });

        const data = await res.json();
        if (res.ok) {
            alert("Blood request submitted!");
            location.href = "history.html";
        } else {
            alert(data.msg || "Submission failed");
        }
    } catch (err) {
        console.error(err);
        alert("An error occurred");
    }
}

loadRequestPage();
