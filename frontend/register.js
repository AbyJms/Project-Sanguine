let allCities = [];

async function loadCities() {
  const res = await fetch("http://localhost:5000/api/cities");
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

async function register() {
  const name = document.getElementById("name").value;
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;
  const gmail = document.getElementById("gmail").value;
  const mobile_no = document.getElementById("mobile_no").value;
  const city_id = document.getElementById("city").value;
  const blood_type = document.querySelector('input[name="blood"]:checked')?.value;

  if (!blood_type) return alert("Please select blood type");

  const res = await fetch("http://localhost:5000/api/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, username, password, gmail, mobile_no, city_id, blood_type })
  });

  const data = await res.json();
  if (res.ok) {
    alert("Registration successful! Please login.");
    location.href = "login.html";
  } else {
    alert(data.error || data.msg);
  }
}

loadCities();
