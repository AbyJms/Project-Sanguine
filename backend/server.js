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

app.get("/api/me", auth, async (req, res) => {
  try {
    const { role, id } = req.user;
    let query = "SELECT username FROM users WHERE id = $1";
    if (role === 'Client') query = "SELECT u.username, c.name FROM users u JOIN client c ON c.id = u.id WHERE u.id = $1";
    if (role === 'Hospital') query = "SELECT u.username, h.name FROM users u JOIN hospital h ON h.id = u.id WHERE u.id = $1";
    if (role === 'Admin') query = "SELECT u.username, a.name FROM users u JOIN admin a ON a.id = u.id WHERE u.id = $1";
    
    const { rows } = await db.query(query, [id]);
    res.json(rows[0] || req.user);
  } catch (err) {
    res.json(req.user);
  }
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
               ci.city_name, ci.district_name
        FROM users u
        JOIN client c ON c.id = u.id
        LEFT JOIN cities ci ON ci.city_id = c.city_id
        WHERE u.id = $1
      `;
    } else if (role === "Hospital") {
      q = `
        SELECT u.username, h.name, h.gmail, h.mobile_no,
               ci.city_name, ci.district_name
        FROM users u
        JOIN hospital h ON h.id = u.id
        LEFT JOIN cities ci ON ci.city_id = h.city_id
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

// Create Blood Request (Client)
app.post("/api/request", auth, async (req, res) => {
  if (req.user.role !== "Client") return res.status(403).json({ msg: "Forbidden" });
  
  try {
    const { username } = req.user;
    const { quantity, city_id } = req.body;

    // 1. Get user details
    const userRes = await db.query(
      `SELECT c.name, c.mobile_no, c.blood_type FROM client c WHERE c.id = $1`,
      [req.user.id]
    );
    if (userRes.rows.length === 0) return res.status(404).json({ msg: "User data not found" });
    const u = userRes.rows[0];

    // 2. Get district for the SELECTED city
    const cityData = await db.query("SELECT district_id FROM cities WHERE city_id = $1", [city_id]);
    if (cityData.rows.length === 0) return res.status(400).json({ msg: "Invalid city" });
    const district_id = cityData.rows[0].district_id;

    // 3. Insert into request table
    await db.query(
      `INSERT INTO request (acceptor, acceptor_name, mobile_no, blood, quantity, district_id, city_id, status) 
       VALUES ($1, $2, $3, $4, $5, $6, $7, 'Pending')`,
      [username, u.name, u.mobile_no, u.blood_type, quantity, district_id, city_id]
    );

    res.json({ msg: "Request submitted successfully" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Create Blood Request (Hospital)
app.post("/api/hospital/request", auth, async (req, res) => {
  if (req.user.role !== "Hospital") return res.status(403).json({ msg: "Forbidden" });
  
  try {
    const { username } = req.user;
    const { blood, quantity } = req.body;

    // 1. Get hospital details including their assigned city and district
    const hospRes = await db.query(
      `SELECT h.name, h.mobile_no, h.city_id, ci.district_id 
       FROM hospital h 
       JOIN cities ci ON ci.city_id = h.city_id 
       WHERE h.id = $1`, 
      [req.user.id]
    );
    if (hospRes.rows.length === 0) return res.status(404).json({ msg: "Hospital data not found" });
    const h = hospRes.rows[0];

    // 2. Insert into request table using the hospital's local city/district
    await db.query(
      `INSERT INTO request (acceptor, acceptor_name, mobile_no, blood, quantity, district_id, city_id, status) 
       VALUES ($1, $2, $3, $4, $5, $6, $7, 'Pending')`,
      [username, h.name, h.mobile_no, blood, quantity, h.district_id, h.city_id]
    );

    res.json({ msg: "Hospital request submitted" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});




// User Notifications (Pending requests from others in same district)
app.get("/api/notifications", auth, async (req, res) => {
  try {
    const { id, username } = req.user;
    
    // 1. Get current user's district
    const userRes = await db.query(
      `SELECT ci.district_id 
       FROM client c 
       JOIN cities ci ON ci.city_id = c.city_id 
       WHERE c.id = $1`,
      [id]
    );

    if (userRes.rows.length === 0) return res.json([]);
    const districtId = userRes.rows[0].district_id;

    // 2. Get pending requests from others in that district
    // Note: The request table has district_id directly, or we can join via city_id
    const { rows } = await db.query(
      `SELECT r.*, ci.city_name 
       FROM request r
       JOIN cities ci ON ci.city_id = r.city_id
       WHERE r.acceptor != $1 
       AND ci.district_id = $2
       AND r.status = 'Pending'
       ORDER BY r.request_pk DESC`,
      [username, districtId]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// User Request History
app.get("/api/history", auth, async (req, res) => {
  try {
    const { username } = req.user;
    const { rows } = await db.query(
      `SELECT r.*, c.city_name, c.district_name 
       FROM request r
       LEFT JOIN cities c ON c.city_id = r.city_id
       WHERE r.acceptor = $1
       ORDER BY r.request_pk DESC`,
      [username]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Update Profile route (Client)
app.post("/api/profile/update", auth, async (req, res) => {
  if (req.user.role !== "Client") return res.status(403).json({ msg: "Forbidden" });
  
  try {
    const { name, mobile_no, gmail, blood_type, city_id } = req.body;
    const userId = req.user.id;

    await db.query(
      `UPDATE client 
       SET name = $1, mobile_no = $2, gmail = $3, blood_type = $4, city_id = $5 
       WHERE id = $6`,
      [name, mobile_no, gmail, blood_type, city_id, userId]
    );

    res.json({ msg: "Profile updated successfully" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


// GET cities
app.get("/api/cities", async (req, res) => {
  try {
    const { rows } = await db.query("SELECT * FROM cities ORDER BY district_name, city_name");
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// REGISTER Client
app.post("/api/register", async (req, res) => {
  const client = await db.connect();
  try {
    const { name, username, password, blood_type, gmail, mobile_no, city_id } = req.body;
    
    await client.query("BEGIN");

    const userRes = await client.query(
      "INSERT INTO users (username, password, role) VALUES ($1, $2, 'Client') RETURNING id",
      [username, password]
    );

    const userId = userRes.rows[0].id;

    await client.query(
      "INSERT INTO client (id, name, blood_type, gmail, mobile_no, city_id) VALUES ($1, $2, $3, $4, $5, $6)",
      [userId, name, blood_type, gmail, mobile_no, city_id]
    );

    await client.query("COMMIT");
    res.json({ msg: "User registered successfully" });
  } catch (err) {
    await client.query("ROLLBACK");
    res.status(500).json({ error: err.message });
  } finally {
    client.release();
  }
});

// Hospital - View Donors in District
app.get("/api/hospital/donors", auth, async (req, res) => {
  if (req.user.role !== "Hospital") return res.status(403).json({ msg: "Forbidden" });
  try {
    const hospitalId = req.user.id;

    // 1. Get hospital's district
    const hospRes = await db.query(
      `SELECT ci.district_id FROM hospital h JOIN cities ci ON ci.city_id = h.city_id WHERE h.id = $1`,
      [hospitalId]
    );
    if (hospRes.rows.length === 0) return res.json([]);
    const districtId = hospRes.rows[0].district_id;

    // 2. Get all Clients in that district
    const { rows } = await db.query(
      `SELECT c.name, c.blood_type, c.mobile_no, c.gmail, u.username, ci.city_name 
       FROM client c 
       JOIN users u ON u.id = c.id
       JOIN cities ci ON ci.city_id = c.city_id 
       WHERE ci.district_id = $1
       ORDER BY c.name`,
      [districtId]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Hospital - View District Requests (Clients only)
app.get("/api/hospital/district-requests", auth, async (req, res) => {
  if (req.user.role !== "Hospital") return res.status(403).json({ msg: "Forbidden" });
  try {
    const hospitalId = req.user.id;

    // 1. Get hospital's district
    const hospRes = await db.query(
      `SELECT ci.district_id FROM hospital h JOIN cities ci ON ci.city_id = h.city_id WHERE h.id = $1`,
      [hospitalId]
    );
    if (hospRes.rows.length === 0) return res.json([]);
    const districtId = hospRes.rows[0].district_id;

    // 2. Get requests from same district, only from Clients
    const { rows } = await db.query(
      `SELECT r.*, ci.city_name 
       FROM request r
       JOIN users u ON u.username = r.acceptor
       JOIN cities ci ON ci.city_id = r.city_id
       WHERE ci.district_id = $1 AND u.role = 'Client'
       ORDER BY r.request_pk DESC`,
      [districtId]
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Hospital - Update Request Status
app.patch("/api/hospital/requests/:pk/status", auth, async (req, res) => {
  if (req.user.role !== "Hospital") return res.status(403).json({ msg: "Forbidden" });
  try {
    const { pk } = req.params;
    const { status } = req.body;

    if (!['Pending', 'Fulfilled', 'Cancelled'].includes(status)) {
      return res.status(400).json({ msg: "Invalid status" });
    }

    await db.query("UPDATE request SET status = $1 WHERE request_pk = $2", [status, pk]);
    res.json({ msg: "Status updated successfully" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Hospital - Blood Request Stats by Month (District specific)
app.get("/api/hospital/request-stats", auth, async (req, res) => {
  if (req.user.role !== "Hospital") return res.status(403).json({ msg: "Forbidden" });
  try {
    const hospitalId = req.user.id;

    // 1. Get hospital's district 
    const hospRes = await db.query(
      `SELECT ci.district_id, ci.district_name 
       FROM hospital h 
       JOIN cities ci ON ci.city_id = h.city_id 
       WHERE h.id = $1`,
      [hospitalId]
    );

    if (hospRes.rows.length === 0) return res.json({ districtName: "Unknown", stats: [] });
    const { district_id, district_name } = hospRes.rows[0];

    // 2. Aggregate requests by month for that district (Past 5 months including current)
    const { rows } = await db.query(
      `SELECT 
         to_char(g.dt, 'Mon') as month,
         COALESCE(r.count, 0) as count
       FROM 
         generate_series(
           date_trunc('month', CURRENT_DATE) - INTERVAL '4 months', 
           date_trunc('month', CURRENT_DATE), 
           INTERVAL '1 month'
         ) AS g(dt)
       LEFT JOIN (
         SELECT 
           EXTRACT(MONTH FROM "Time") as m,
           EXTRACT(YEAR FROM "Time") as y,
           COUNT(*) as count
         FROM request
         WHERE district_id = $1
         GROUP BY y, m
       ) r ON r.m = EXTRACT(MONTH FROM g.dt) AND r.y = EXTRACT(YEAR FROM g.dt)
       ORDER BY g.dt`,
      [district_id]
    );

    res.json({ districtName: district_name, stats: rows });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});




// Admin - Donor Distribution by District
app.get("/api/admin/donor-distribution", auth, async (req, res) => {
  if (req.user.role !== "Admin") return res.status(403).json({ msg: "Forbidden" });
  try {
    const { rows } = await db.query(
      `SELECT ci.district_name, COUNT(c.id) as donor_count 
       FROM cities ci 
       LEFT JOIN client c ON c.city_id = ci.city_id 
       GROUP BY ci.district_name 
       ORDER BY ci.district_name`
    );
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Admin stats
app.get("/api/admin/stats", auth, async (req, res) => {
  if (req.user.role !== "Admin") return res.status(403).json({ msg: "Forbidden" });
  try {
    const clients = await db.query("SELECT COUNT(*) FROM client");
    const hospitals = await db.query("SELECT COUNT(*) FROM hospital");
    const admins = await db.query("SELECT COUNT(*) FROM admin");
    
    res.json({
      clients: parseInt(clients.rows[0].count),
      hospitals: parseInt(hospitals.rows[0].count),
      admins: parseInt(admins.rows[0].count)
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Admin - View Users by Role
app.get("/api/admin/users/:role", auth, async (req, res) => {
  if (req.user.role !== "Admin") return res.status(403).json({ msg: "Forbidden" });
  try {
    const { role } = req.params;
    let query = "";
    if (role === "Client") {
      query = `SELECT u.id, u.username, c.name, u.role, c.mobile_no, c.gmail, c.blood_type 
               FROM users u JOIN client c ON c.id = u.id`;
    } else if (role === "Hospital") {
      query = `SELECT u.id, u.username, h.name, u.role, h.mobile_no, h.gmail, '' as blood_type 
               FROM users u JOIN hospital h ON h.id = u.id`;
    } else if (role === "Admin") {
      query = `SELECT u.id, u.username, a.name, u.role, '' as mobile_no, '' as gmail, '' as blood_type 
               FROM users u JOIN admin a ON a.id = u.id`;
    } else {

      return res.status(400).json({ msg: "Invalid role" });
    }
    const { rows } = await db.query(query);
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Admin - Delete User
app.delete("/api/admin/users/:id", auth, async (req, res) => {
  if (req.user.role !== "Admin") return res.status(403).json({ msg: "Forbidden" });
  try {
    const { id } = req.params;
    
    // Check if target user exists
    const { rows } = await db.query("SELECT role FROM users WHERE id = $1", [id]);
    if (rows.length === 0) return res.status(404).json({ msg: "User not found" });

    const targetUser = rows[0];

    // RESTRICTION: Only Head Admin can delete another Admin
    if (targetUser.role === 'Admin' && !req.user.is_head_admin) {
        return res.status(403).json({ msg: "Only Head Admin can delete other admins" });
    }

    // Prevent deleting self
    if (parseInt(id) === req.user.id) {
       return res.status(403).json({ msg: "Cannot delete yourself" });
    }

    await db.query("DELETE FROM users WHERE id = $1", [id]);
    res.json({ msg: "User deleted successfully" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Admin - Register Hospital
app.post("/api/register/hospital", auth, async (req, res) => {
  if (req.user.role !== "Admin") return res.status(403).json({ msg: "Forbidden" });
  const client = await db.connect();
  try {
    const { name, username, password, gmail, mobile_no, city_id } = req.body;
    await client.query("BEGIN");

    const userRes = await client.query(
      "INSERT INTO users (username, password, role) VALUES ($1, $2, 'Hospital') RETURNING id",
      [username, password]
    );

    await client.query(
      "INSERT INTO hospital (id, name, gmail, mobile_no, city_id) VALUES ($1, $2, $3, $4, $5)",
      [userRes.rows[0].id, name, gmail, mobile_no, city_id]
    );

    await client.query("COMMIT");
    res.json({ msg: "Hospital registered" });
  } catch (err) {
    await client.query("ROLLBACK");
    res.status(500).json({ error: err.message });
  } finally {
    client.release();
  }
});

// Admin - Register New Admin
app.post("/api/register/admin", auth, async (req, res) => {
  if (req.user.role !== "Admin") return res.status(403).json({ msg: "Forbidden" });
  
  // RESTRICTION: Only Head Admin can register other admins
  if (!req.user.is_head_admin) {
    return res.status(403).json({ msg: "Only Head Admin can register other admins" });
  }

  const client = await db.connect();
  try {
    const { name, username, password } = req.body;
    await client.query("BEGIN");

    const userRes = await client.query(
      "INSERT INTO users (username, password, role) VALUES ($1, $2, 'Admin') RETURNING id",
      [username, password]
    );

    await client.query(
      "INSERT INTO admin (id, name, is_head_admin) VALUES ($1, $2, false)",
      [userRes.rows[0].id, name]
    );

    await client.query("COMMIT");
    res.json({ msg: "Admin registered" });
  } catch (err) {
    await client.query("ROLLBACK");
    res.status(500).json({ error: err.message });
  } finally {
    client.release();
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

    let is_head_admin = false;
    if (user.role === 'Admin') {
      const adminData = await db.query("SELECT is_head_admin FROM admin WHERE id = $1", [user.id]);
      is_head_admin = adminData.rows[0]?.is_head_admin || false;
    }

    const token = jwt.sign(
      {
        username: user.username,
        role: user.role,
        id: user.id,
        is_head_admin
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


if (require.main === module) {
  const PORT = process.env.PORT || 5000;
  app.listen(PORT, () => console.log("Server started on port " + PORT));
}

module.exports = app;