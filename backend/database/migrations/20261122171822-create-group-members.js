'use strict';

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface, Sequelize) {
    await queryInterface.createTable('group_members', {
      id: { type: Sequelize.BIGINT, autoIncrement: true, primaryKey: true },
      group_id: {
        type: Sequelize.BIGINT,
        allowNull: false,
        references: { model: 'shared_groups', key: 'id' },
        onDelete: 'CASCADE', // grup dihapus -> anggotanya ikut terhapus (GRP-5)
      },
      member_name: { type: Sequelize.STRING(50), allowNull: false },
      contact: { type: Sequelize.STRING(100), allowNull: true },
      share_amount: { type: Sequelize.DECIMAL(12, 0), allowNull: false },
      dues_status: { type: Sequelize.ENUM('LUNAS', 'BELUM_LUNAS'), allowNull: false, defaultValue: 'BELUM_LUNAS' },
      paid_at: { type: Sequelize.DATEONLY, allowNull: true },
      note: { type: Sequelize.STRING(255), allowNull: true },
      created_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
      updated_at: { type: Sequelize.DATE, allowNull: false, defaultValue: Sequelize.literal('CURRENT_TIMESTAMP') },
    });

    await queryInterface.addConstraint('group_members', {
      fields: ['share_amount'],
      type: 'check',
      where: { share_amount: { [Sequelize.Op.gt]: 0 } },
      name: 'chk_members_share_amount',
    });
  },

  async down(queryInterface) {
    await queryInterface.dropTable('group_members');
  },
};
