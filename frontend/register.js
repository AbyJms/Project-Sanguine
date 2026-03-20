console.log("JS LOADED");
let allCities = [];

document.addEventListener("DOMContentLoaded", () => {

  async function loadCities() {
    const res = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/cities");
    allCities = await res.json();

    const districtSelect = document.getElementById("district");
    const districts = [...new Set(allCities.map(c => c.district_name))];
    
    districts.forEach(d => {
      const opt = document.createElement("option");
      opt.value = d;
      opt.textContent = d;
      districtSelect.appendChild(opt);
    });
  }

  document.getElementById("district").addEventListener("change", (e) => {
    const districtName = e.target.value;
    const citySelect = document.getElementById("city");
    citySelect.innerHTML = '<option value="">Select City</option>';

    const filtered = allCities.filter(c => c.district_name === districtName);
    filtered.forEach(c => {
      const opt = document.createElement("option");
      opt.value = c.city_id;
      opt.textContent = c.city_name;
      citySelect.appendChild(opt);
    });
  });

  loadCities();
});