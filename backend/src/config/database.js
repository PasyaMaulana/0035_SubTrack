require('dotenv').config({ quiet: true });

const config = {
  username: process.env.DB_USER,
  password: process.env.DB_PASSWORD || null,
  database: process.env.DB_NAME,
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT) || 3306,
  dialect: 'mysql',
  timezone: '+07:00',
  logging: false,
  dialectOptions: { decimalNumbers: true }, // DECIMAL dikembalikan sebagai angka, bukan string
};

module.exports = {
  development: config,
  test: config,
  production: config,
};
