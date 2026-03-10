require("dotenv").config();
const db = require("./db");

async function test() {
  try {
    const cities = await db.query("SELECT * FROM cities");
    console.log("Cities:", cities.rows);
    
    const districts = await db.query("SELECT * FROM districts");
    console.log("Districts:", districts.rows);

    const users = await db.query("SELECT id, username, role FROM users");
    console.log("Users:", users.rows);

    process.exit(0);
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}

test();
