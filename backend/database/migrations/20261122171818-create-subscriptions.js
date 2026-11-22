'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('subscriptions', {
      id: { type: Sequelize.BIGINT, autoIncrement: true, primaryKey: true },
      user_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'users', key: 'id' },
        onDelete: 'CASCADE', // user dihapus -> langganannya ikut terhapus
      },
      service_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'digital_services', key: 'id' },
        onDelete: 'RESTRICT', // layanan yang masih dipakai tidak boleh dihapus (CAT-4)
      },
      plan_name: { type: Sequelize.STRING(50), allowNull: false },
      price: { type: Sequelize.DECIMAL(12, 0), allowNull: false },
      billing_cycle: { type: Sequelize.ENUM('BULANAN', 'TRIWULAN', 'TAHUNAN'), allowNull: false },
      start_date: { type: Sequelize.DATEONLY, allowNull: false },
      next_billing_date: { type: Sequelize.DATEONLY, allowNull: false },
      payment_method: { type: Sequelize.STRING(30), allowNull: true },
      status: { type: Sequelize.ENUM('ACTIVE', 'PAUSED', 'CANCELLED'), allowNull: false, defaultValue: 'ACTIVE' },
      notes: { type: Sequelize.STRING(255), allowNull: true },
      created_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
      updated_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
    });

    await queryInterface.addConstraint('subscriptions', {
      fields: ['price'],
      type: 'check',
      where: { price: { [Sequelize.Op.gt]: 0 } },
      name: 'chk_subscriptions_price',
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable('subscriptions');
  },
};
