(async () => {
  const t = localStorage.getItem("token");
  if (!t) return;

  const r = await fetch("http://localhost:5000/api/profile", {
    headers: { Authorization: "Bearer " + t }
  });

  if (!r.ok) return;

  const u = await r.json();

  // fill profile page if elements exist
  const map = {
    pUsername: u.Username,
    pName: u.Name,
    pBlood: u.Blood_Type,
    pEmail: u.Gmail,
    pMobile: u.Mobile_No,
    pDistrict: u.district_name,
    pCity: u.city_name
  };

  for (const id in map) {
    const el = document.getElementById(id);
    if (el) el.textContent = map[id] || "";
  }
})();