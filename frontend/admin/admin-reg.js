async function registerHospital() {
  const username = document.getElementById("username").value;
  const name = document.getElementById("name").value;
  const mobile_no = document.getElementById("mobile_no").value;
  const city_id = document.getElementById("city").value;
  const gmail = document.getElementById("gmail").value;
  const password = document.getElementById("password").value;

  const t = localStorage.getItem("token");
  const res = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/register/hospital", {
    method: "POST",
    headers: { 
      "Content-Type": "application/json",
      "Authorization": "Bearer " + t
    },
    body: JSON.stringify({ username, name, mobile_no, city_id, gmail, password })
  });

  if (res.ok) {
    alert("Hospital Registered!");
    location.href = "admin-dashboard.html";
  } else {
    const data = await res.json();
    alert(data.error || data.msg);
  }
}

async function registerAdmin() {
  const name = document.getElementById("name").value;
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const t = localStorage.getItem("token");
  const res = await fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/register/admin", {
    method: "POST",
    headers: { 
      "Content-Type": "application/json",
      "Authorization": "Bearer " + t
    },
    body: JSON.stringify({ name, username, password })
  });

  if (res.ok) {
    alert("Admin Registered!");
    location.href = "admin-dashboard.html";
  } else {
    const data = await res.json();
    alert(data.error || data.msg);
  }
}

// Logic for city dropdown if it exists on page
if (document.getElementById("district")) {
  let allCities = [];
  fetch((window.location.hostname === "localhost" ? "http://localhost:5000" : "") + "/api/cities")
    .then(r => r.json())
    .then(data => {
      allCities = data;
      const dSel = document.getElementById("district");
      const dists = [...new Set(data.map(c => c.district_name))];
      dists.forEach(d => {
        const opt = document.createElement("option");
        opt.value = d; opt.textContent = d;
        dSel.appendChild(opt);
      });
    });

  document.getElementById("district").addEventListener("change", (e) => {
    const cSel = document.getElementById("city");
    cSel.innerHTML = '<option value="">Select City</option>';
    allCities.filter(c => c.district_name === e.target.value).forEach(c => {
      const opt = document.createElement("option");
      opt.value = c.city_id; opt.textContent = c.city_name;
      cSel.appendChild(opt);
    });
  });
}
