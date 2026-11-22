require('dotenv').config({ quiet: true });
const app = require('./app');

const PORT = process.env.PORT || 3000;

// 0.0.0.0 supaya server bisa diakses dari HP di jaringan Wi-Fi yang sama, bukan hanya dari laptop
app.listen(PORT, '0.0.0.0', () => {
  console.log(`SubTrack API berjalan di http://localhost:${PORT}`);
});