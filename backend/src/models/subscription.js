'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class Subscription extends Model {
    static associate(models) {
      Subscription.belongsTo(models.User, { foreignKey: 'user_id', as: 'user' });
      Subscription.belongsTo(models.DigitalService, { foreignKey: 'service_id', as: 'service' });
      Subscription.hasMany(models.Payment, { foreignKey: 'subscription_id', as: 'payments' });
      Subscription.hasMany(models.SharedGroup, { foreignKey: 'subscription_id', as: 'groups' });
    }
  }

  Subscription.init(
    {
      user_id: { type: DataTypes.BIGINT, allowNull: false },
      service_id: { type: DataTypes.BIGINT, allowNull: false },
      plan_name: { type: DataTypes.STRING(50), allowNull: false },
      price: { type: DataTypes.DECIMAL(12, 0), allowNull: false },
      billing_cycle: { type: DataTypes.ENUM('BULANAN', 'TRIWULAN', 'TAHUNAN'), allowNull: false },
      start_date: { type: DataTypes.DATEONLY, allowNull: false },
      next_billing_date: { type: DataTypes.DATEONLY, allowNull: false },
      payment_method: { type: DataTypes.STRING(30), allowNull: true },
      status: { type: DataTypes.ENUM('ACTIVE', 'PAUSED', 'CANCELLED'), allowNull: false, defaultValue: 'ACTIVE' },
      notes: { type: DataTypes.STRING(255), allowNull: true },
    },
    { sequelize, modelName: 'Subscription', tableName: 'subscriptions', underscored: true, createdAt: 'created_at', updatedAt: 'updated_at' }
  );

  return Subscription;
};
