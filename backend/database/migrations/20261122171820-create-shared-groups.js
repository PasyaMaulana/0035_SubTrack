'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('shared_groups', {
      id: { type: Sequelize.BIGINT, autoIncrement: true, primaryKey: true },
      owner_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'users', key: 'id' },
        onDelete: 'CASCADE',
      },
      service_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'digital_services', key: 'id' },
        onDelete: 'RESTRICT',
      },
      subscription_id: {
        type: Sequelize.BIGINT,
        allowNull: true,
        references: { model: 'subscriptions', key: 'id' },
        onDelete: 'SET NULL', // langganan dihapus -> grup tetap ada, kaitannya dikosongkan (SUB-5)
      },
      name: { type: Sequelize.STRING(50), allowNull: false },
      total_cost: { type: Sequelize.DECIMAL(12, 0), allowNull: false },
      billing_cycle: { type: Sequelize.ENUM('BULANAN', 'TRIWULAN', 'TAHUNAN'), allowNull: false },
      due_day: { type: Sequelize.TINYINT, allowNull: false },
      max_members: { type: Sequelize.TINYINT, allowNull: false },
      description: { type: Sequelize.STRING(255), allowNull: true },
      created_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
      updated_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
    });

    const { Op } = Sequelize;
    await queryInterface.addConstraint('shared_groups', {
      fields: ['total_cost'], type: 'check', name: 'chk_groups_total_cost',
      where: { total_cost: { [Op.gt]: 0 } },
    });
    await queryInterface.addConstraint('shared_groups', {
      fields: ['due_day'], type: 'check', name: 'chk_groups_due_day',
      where: { due_day: { [Op.between]: [1, 28] } },
    });
    await queryInterface.addConstraint('shared_groups', {
      fields: ['max_members'], type: 'check', name: 'chk_groups_max_members',
      where: { max_members: { [Op.between]: [2, 10] } },
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable('shared_groups');
  },
};
