const express = require('express');
const { sequelize } = require('../models');

const router = express.Router();

// GET /api/v1/health -> cek server dan koneksi database
router.get('/', async (req, res) => {
  try {
    await sequelize.authenticate();
    res.json({ success: true, message: 'Server dan database berjalan', data: { database: sequelize.getDatabaseName() } });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Database tidak terhubung: ' + err.message });
  }
});

module.exports = router;