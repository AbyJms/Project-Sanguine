async function submitHospitalRequest() {
    const blood = document.getElementById("hBlood").value;
    const quantity = document.getElementById("hQuantity").value;
    const token = localStorage.getItem("token");

    if (!quantity || !blood) {
        return alert("Please fill all fields");
    }

    try {
        const res = await fetch("http://localhost:5000/api/hospital/request", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify({ blood, quantity })
        });

        const data = await res.json();
        if (res.ok) {
            alert("Hospital request submitted!");
            location.href = "home.html";
        } else {
            alert(data.msg || "Submission failed");
        }
    } catch (err) {
        console.error(err);
        alert("An error occurred");
    }
}
