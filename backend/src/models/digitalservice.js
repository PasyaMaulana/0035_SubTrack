'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class DigitalService extends Model {
    static associate(models) {
      DigitalService.belongsTo(models.User, { foreignKey: 'created_by', as: 'creator' });
      DigitalService.hasMany(models.Subscription, { foreignKey: 'service_id', as: 'subscriptions' });
      DigitalService.hasMany(models.SharedGroup, { foreignKey: 'service_id', as: 'groups' });
    }
  }

  DigitalService.init(
    {
      created_by: { type: DataTypes.BIGINT, allowNull: false },
      name: { type: DataTypes.STRING(50), allowNull: false, unique: true },
      category: {
        type: DataTypes.ENUM('STREAMING_VIDEO', 'MUSIK', 'CLOUD_STORAGE', 'PRODUKTIVITAS', 'GAMING', 'LAINNYA'),
        allowNull: false,
      },
      description: { type: DataTypes.STRING(255), allowNull: true },
      website_url: { type: DataTypes.STRING(255), allowNull: true },
      is_active: { type: DataTypes.BOOLEAN, allowNull: false, defaultValue: true },
    },
    { sequelize, modelName: 'DigitalService', tableName: 'digital_services', underscored: true, createdAt: 'created_at', updatedAt: 'updated_at' }
  );

  return DigitalService;
};
