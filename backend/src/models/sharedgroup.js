'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class SharedGroup extends Model {
    static associate(models) {
      SharedGroup.belongsTo(models.User, { foreignKey: 'owner_id', as: 'owner' });
      SharedGroup.belongsTo(models.DigitalService, { foreignKey: 'service_id', as: 'service' });
      SharedGroup.belongsTo(models.Subscription, { foreignKey: 'subscription_id', as: 'subscription' });
      SharedGroup.hasMany(models.GroupMember, { foreignKey: 'group_id', as: 'members' });
    }
  }

  SharedGroup.init(
    {
      owner_id: { type: DataTypes.BIGINT, allowNull: false },
      service_id: { type: DataTypes.BIGINT, allowNull: false },
      subscription_id: { type: DataTypes.BIGINT, allowNull: true },
      name: { type: DataTypes.STRING(50), allowNull: false },
      total_cost: { type: DataTypes.DECIMAL(12, 0), allowNull: false },
      billing_cycle: { type: DataTypes.ENUM('BULANAN', 'TRIWULAN', 'TAHUNAN'), allowNull: false },
      due_day: { type: DataTypes.TINYINT, allowNull: false },
      max_members: { type: DataTypes.TINYINT, allowNull: false },
      description: { type: DataTypes.STRING(255), allowNull: true },
    },
    { sequelize, modelName: 'SharedGroup', tableName: 'shared_groups', underscored: true, createdAt: 'created_at', updatedAt: 'updated_at' }
  );

  return SharedGroup;
};
