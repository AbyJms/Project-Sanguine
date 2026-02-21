(async () => {
  const t = localStorage.getItem("token");

  if (!t) {
    location.href = "login.html";
    return;
  }

  try {
    const r = await fetch("http://localhost:5000/api/me", {
      headers: { Authorization: "Bearer " + t }
    });

    if (!r.ok) {
      localStorage.removeItem("token");
      location.href = "login.html";
      return;
    }

    const u = await r.json();

    // store user globally
    window.currentUser = u;

    // fill username if element exists
    const el = document.getElementById("loggedUser");
    if (el) el.textContent = u.username;

  } catch {
    location.href = "login.html";
  }
})();