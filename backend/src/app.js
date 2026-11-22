const express = require('express');
const healthRoutes = require('./routes/health.routes');

const app = express();

app.use(express.json()); // supaya body request JSON bisa dibaca di req.body

app.use('/api/v1/health', healthRoutes);

// Route yang tidak ada -> 404 dengan format response SubTrack
app.use((req, res) => {
  res.status(404).json({ success: false, message: 'Endpoint tidak ditemukan' });
});

module.exports = app;