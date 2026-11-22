'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class Payment extends Model {
    static associate(models) {
      Payment.belongsTo(models.Subscription, { foreignKey: 'subscription_id', as: 'subscription' });
    }
  }

  Payment.init(
    {
      subscription_id: { type: DataTypes.BIGINT, allowNull: false },
      amount: { type: DataTypes.DECIMAL(12, 0), allowNull: false },
      paid_at: { type: DataTypes.DATEONLY, allowNull: false },
      method: { type: DataTypes.STRING(30), allowNull: true },
      proof_file_name: { type: DataTypes.STRING(100), allowNull: true },
      note: { type: DataTypes.STRING(255), allowNull: true },
    },
    { sequelize, modelName: 'Payment', tableName: 'payments', underscored: true, createdAt: 'created_at', updatedAt: 'updated_at' }
  );

  return Payment;
};
