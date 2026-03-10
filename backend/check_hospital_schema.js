require('dotenv').config();
const db = require('./db');
async function run() {
  const r2 = await db.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'hospital'");
  console.log('Hospital Table Columns:', r2.rows);
  process.exit(0);
}
run();
