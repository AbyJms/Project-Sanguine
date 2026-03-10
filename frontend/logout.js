function logout() {
  localStorage.removeItem("token");
  // Determine if we need to go up a directory
  const path = window.location.pathname;
  if (path.includes("/admin/") || path.includes("/hospital/") || path.includes("/user/")) {
    location.href = "../login.html";
  } else {
    location.href = "login.html";
  }
}