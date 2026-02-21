const express = require("express");
const cors = require("cors");
const jwt = require("jsonwebtoken");
require("dotenv").config();

const db = require("./db");

const app = express();

app.use(cors());
app.use(express.json());

function auth(req, res, next) {
  const h = req.headers.authorization;

  if (!h) return res.status(401).json({ msg: "No token" });

  try {
    const t = h.split(" ")[1];
    req.user = jwt.verify(t, process.env.JWT_SECRET);
    next();
  } catch {
    res.status(401).json({ msg: "Invalid token" });
  }
}


app.get("/api/me", auth, (req, res) => {
  res.json(req.user);
});

//profile route
app.get("/api/profile", auth, async (req, res) => {
  try {
    const [rows] = await db.query(
      "SELECT Username, Name, Blood_Type, Gmail, Mobile_No, district_name, city_name FROM user_table WHERE Username = ?",
      [req.user.username]
    );

    if (rows.length === 0) {
      return res.status(404).json({ msg: "User not found" });
    }

    res.json(rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


// health
app.get("/api/health", (req, res) => {
  res.json({ msg: "Server running" });
});

// DB test
app.get("/api/db-test", async (req, res) => {
  try {
    const [rows] = await db.query("SELECT 1 AS test");
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// LOGIN
app.post("/api/login", async (req, res) => {
  try {
    const { username, password } = req.body;

    const [rows] = await db.query(
      "SELECT * FROM user_table WHERE Username = ?",
      [username]
    );

    if (rows.length === 0) {
      return res.status(401).json({ msg: "Invalid credentials" });
    }

    const user = rows[0];

    // plain compare (since your Java app likely stored plain passwords)
    if (user.Password !== password) {
      return res.status(401).json({ msg: "Invalid credentials" });
    }

    const token = jwt.sign(
      {
        username: user.Username,
        role: user.Role,
      },
      process.env.JWT_SECRET,
      { expiresIn: "7d" }
    );

    res.json({
      token,
      role: user.Role,
      username: user.Username,
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log("Server started on port " + PORT));