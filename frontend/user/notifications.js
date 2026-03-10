(async () => {
  const token = localStorage.getItem("token");
  if (!token) return;

  try {
    const res = await fetch("http://localhost:5000/api/notifications", {
      headers: { Authorization: "Bearer " + token }
    });

    if (!res.ok) {
      if (res.status === 401) {
        // Handle session expired
        return;
      }
      return console.error("Could not fetch notifications");
    }

    const notifications = await res.json();
    const tbody = document.getElementById("notifBody");
    tbody.innerHTML = "";

    if (notifications.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5">No pending requests in your district.</td></tr>';
      return;
    }

    notifications.forEach(n => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${n.acceptor_name}</td>
        <td>${n.mobile_no}</td>
        <td>${n.blood}</td>
        <td>${n.city_name}</td>
        <td>${n.quantity}</td>
      `;
      tbody.appendChild(tr);
    });

  } catch (err) {
    console.error("NOTIFICATION LOAD ERROR:", err);
  }
})();
