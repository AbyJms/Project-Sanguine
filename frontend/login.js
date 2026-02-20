async function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const res = await fetch("http://localhost:5000/api/login", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({ username, password })
  });

  const data = await res.json();

  if (!res.ok) {
    alert(data.msg || "Login failed");
    return;
  }

  localStorage.setItem("token", data.token);

  if (data.role === "Admin") location.href = "home.html";
  else if (data.role === "Client") location.href = "home.html";
  else if (data.role === "Hospital") location.href = "home.html";
}