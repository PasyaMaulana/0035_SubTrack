'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('digital_services', {
      id: { type: Sequelize.BIGINT, autoIncrement: true, primaryKey: true },
      created_by: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'users', key: 'id' },
      },
      name: { type: Sequelize.STRING(50), allowNull: false, unique: true },
      category: {
        type: Sequelize.ENUM('STREAMING_VIDEO', 'MUSIK', 'CLOUD_STORAGE', 'PRODUKTIVITAS', 'GAMING', 'LAINNYA'),
        allowNull: false,
      },
      description: { type: Sequelize.STRING(255), allowNull: true },
      website_url: { type: Sequelize.STRING(255), allowNull: true },
      is_active: { type: Sequelize.BOOLEAN, allowNull: false, defaultValue: true },
      created_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
      updated_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable('digital_services');
  },
};
