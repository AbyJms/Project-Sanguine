require('dotenv').config();
const db = require('./db');
async function run() {
  const r = await db.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'request'");
  console.log('Request Table Columns:', r.rows);
  process.exit(0);
}
run();
