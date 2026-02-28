(async () => {
  const t = localStorage.getItem("token");
  if (!t) return;

  const r = await fetch("http://localhost:5000/api/profile", {
    headers: { Authorization: "Bearer " + t }
  });

  if (!r.ok) return;

  const u = await r.json();

  const map = {
    pUsername: u.username,
    pName: u.name,
    pBlood: u.blood_type,
    pEmail: u.gmail,
    pMobile: u.mobile_no,
    pDistrict: u.district_name,
    pCity: u.city_name
  };

  for (const id in map) {
    const el = document.getElementById(id);
    if (el) el.textContent = map[id] ?? "";
  }
})();