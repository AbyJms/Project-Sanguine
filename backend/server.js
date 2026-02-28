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

// profile route
app.get("/api/profile", auth, async (req, res) => {
  try {
    const role = req.user.role;
    const id = req.user.id;

    let q = "";

    if (role === "Client") {
      q = `
        SELECT u.username, c.name, c.blood_type, c.gmail, c.mobile_no,
               ci.city_name, d.district_name
        FROM users u
        JOIN client c ON c.id = u.id
        LEFT JOIN cities ci ON ci.city_id = c.city_id
        LEFT JOIN districts d ON d.district_id = ci.district_id
        WHERE u.id = $1
      `;
    } else if (role === "Hospital") {
      q = `
        SELECT u.username, h.name, h.gmail, h.mobile_no,
               ci.city_name, d.district_name
        FROM users u
        JOIN hospital h ON h.id = u.id
        LEFT JOIN cities ci ON ci.city_id = h.city_id
        LEFT JOIN districts d ON d.district_id = ci.district_id
        WHERE u.id = $1
      `;
    } else if (role === "Admin") {
      q = `
        SELECT u.username, a.name, a.is_head_admin
        FROM users u
        JOIN admin a ON a.id = u.id
        WHERE u.id = $1
      `;
    }

    const { rows } = await db.query(q, [id]);

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
    const { rows } = await db.query("SELECT 1 AS test");
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// LOGIN
app.post("/api/login", async (req, res) => {
  try {
    const { username, password } = req.body;

    const { rows } = await db.query(
      "SELECT id, username, password, role FROM users WHERE username = $1",
      [username]
    );

    if (rows.length === 0) {
      return res.status(401).json({ msg: "Invalid credentials" });
    }

    const user = rows[0];

    if (user.password !== password) {
      return res.status(401).json({ msg: "Invalid credentials" });
    }

    const token = jwt.sign(
      {
        username: user.username,
        role: user.role,
        id: user.id,
      },
      process.env.JWT_SECRET,
      { expiresIn: "7d" }
    );

    res.json({
      token,
      role: user.role,
      username: user.username,
    });
  } catch (err) {
    console.error("LOGIN ERROR:", err);
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log("Server started on port " + PORT));