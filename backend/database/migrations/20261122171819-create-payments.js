'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('payments', {
      id: { type: Sequelize.BIGINT, autoIncrement: true, primaryKey: true },
      subscription_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'subscriptions', key: 'id' },
        onDelete: 'CASCADE', // langganan dihapus -> riwayat pembayarannya ikut terhapus (SUB-5)
      },
      amount: { type: Sequelize.DECIMAL(12, 0), allowNull: false },
      paid_at: { type: Sequelize.DATEONLY, allowNull: false },
      method: { type: Sequelize.STRING(30), allowNull: true },
      proof_file_name: { type: Sequelize.STRING(100), allowNull: true },
      note: { type: Sequelize.STRING(255), allowNull: true },
      created_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
      updated_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
    });

    await queryInterface.addConstraint('payments', {
      fields: ['amount'],
      type: 'check',
      where: { amount: { [Sequelize.Op.gt]: 0 } },
      name: 'chk_payments_amount',
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable('payments');
  },
};
