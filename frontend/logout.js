function logout() {
  localStorage.removeItem("token");
  location.href = "login.html";
}