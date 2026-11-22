'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class GroupMember extends Model {
    static associate(models) {
      GroupMember.belongsTo(models.SharedGroup, { foreignKey: 'group_id', as: 'group' });
    }
  }

  GroupMember.init(
    {
      group_id: { type: DataTypes.BIGINT, allowNull: false },
      member_name: { type: DataTypes.STRING(50), allowNull: false },
      contact: { type: DataTypes.STRING(100), allowNull: true },
      share_amount: { type: DataTypes.DECIMAL(12, 0), allowNull: false },
      dues_status: { type: DataTypes.ENUM('LUNAS', 'BELUM_LUNAS'), allowNull: false, defaultValue: 'BELUM_LUNAS' },
      paid_at: { type: DataTypes.DATEONLY, allowNull: true },
      note: { type: DataTypes.STRING(255), allowNull: true },
    },
    { sequelize, modelName: 'GroupMember', tableName: 'group_members', underscored: true, createdAt: 'created_at', updatedAt: 'updated_at' }
  );

  return GroupMember;
};
