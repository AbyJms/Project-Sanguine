let allCities = [];

async function loadData() {
  const token = localStorage.getItem("token");
  if (!token) return;

  try {
    // 1. Load Cities/Districts
    const cityRes = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/cities");
    allCities = await cityRes.json();

    const dSel = document.getElementById("eDistrict");
    const dists = [...new Set(allCities.map(c => c.district_name))];
    dists.forEach(d => {
      const opt = document.createElement("option");
      opt.value = d; opt.textContent = d;
      dSel.appendChild(opt);
    });

    // 2. Load Profile Data
    const r = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/profile", {
      headers: { Authorization: "Bearer " + token }
    });
    if (!r.ok) return;
    const u = await r.json();

    document.getElementById("eName").value = u.name || "";
    document.getElementById("eMobile").value = u.mobile_no || "";
    document.getElementById("eEmail").value = u.gmail || "";
    document.getElementById("eBlood").value = u.blood_type || "O+";

    // Set District and City dropdowns
    if (u.district_name) {
      dSel.value = u.district_name;
      updateCities(u.district_name, u.city_id);
    }

  } catch (e) {
    console.error("LOAD ERROR:", e);
  }
}

document.getElementById("eDistrict").addEventListener("change", (e) => {
  updateCities(e.target.value);
});

function updateCities(districtName, selectedCityId = null) {
  const cSel = document.getElementById("eCity");
  cSel.innerHTML = '<option value="">Select City</option>';
  allCities.filter(c => c.district_name === districtName).forEach(c => {
    const opt = document.createElement("option");
    opt.value = c.city_id; opt.textContent = c.city_name;
    if (selectedCityId && c.city_id == selectedCityId) opt.selected = true;
    cSel.appendChild(opt);
  });
}

async function saveChanges() {
  const name = document.getElementById("eName").value;
  const mobile_no = document.getElementById("eMobile").value;
  const gmail = document.getElementById("eEmail").value;
  const blood_type = document.getElementById("eBlood").value;
  const city_id = document.getElementById("eCity").value;

  const token = localStorage.getItem("token");
  const res = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/profile/update", {
    method: "POST",
    headers: { 
      "Content-Type": "application/json",
      "Authorization": "Bearer " + token
    },
    body: JSON.stringify({ name, mobile_no, gmail, blood_type, city_id })
  });

  if (res.ok) {
    alert("Profile updated!");
    location.href = "profile.html";
  } else {
    const data = await res.json();
    alert(data.msg || "Update failed");
  }
}

loadData();
