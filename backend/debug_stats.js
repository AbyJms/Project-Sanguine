require('dotenv').config();
const db = require('./db');
async function test() {
  try {
    const hospitalId = 1; // Try with ID 1 or first available
    const hospRes = await db.query(
      `SELECT ci.district_id, ci.district_name 
       FROM hospital h 
       JOIN cities ci ON ci.city_id = h.city_id 
       LIMIT 1`
    );

    if (hospRes.rows.length === 0) {
        console.log("No hospitals found");
        process.exit(0);
    }
    const { district_id, district_name } = hospRes.rows[0];
    console.log("Testing for District:", district_name, "ID:", district_id);

    const { rows } = await db.query(
      `SELECT 
         to_char(g.dt, 'Mon') as month,
         EXTRACT(MONTH FROM g.dt) as month_num,
         COALESCE(r.count, 0) as count,
         g.dt as debug_dt
       FROM 
         generate_series(
           date_trunc('month', CURRENT_DATE) - INTERVAL '4 months', 
           date_trunc('month', CURRENT_DATE), 
           INTERVAL '1 month'
         ) AS g(dt)
       LEFT JOIN (
         SELECT 
           date_trunc('month', "Time") as month_truncated,
           COUNT(*) as count
         FROM request
         WHERE district_id = $1
         GROUP BY month_truncated
       ) r ON r.month_truncated = g.dt
       ORDER BY g.dt`,
      [district_id]
    );

    console.log("Result rows:", rows);
    process.exit(0);
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}
test();
