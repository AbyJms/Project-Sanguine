require("dotenv").config();
const db = require("./db");
async function test() {
  const rows = await db.query("SELECT * FROM request LIMIT 5");
  console.log("Sample requests:", rows.rows);
  process.exit(0);
}
test();
