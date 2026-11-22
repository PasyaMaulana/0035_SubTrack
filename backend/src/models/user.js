'use strict';
const { Model } = require('sequelize');

module.exports = (sequelize, DataTypes) => {
  class User extends Model {
    static associate(models) {
      User.hasMany(models.DigitalService, { foreignKey: 'created_by', as: 'createdServices' });
      User.hasMany(models.Subscription, { foreignKey: 'user_id', as: 'subscriptions' });
      User.hasMany(models.SharedGroup, { foreignKey: 'owner_id', as: 'ownedGroups' });
    }

    // Data user yang aman dikirim ke aplikasi (tanpa password dan token)
    toJSON() {
      const { password_hash, refresh_token_hash, refresh_expires_at, ...safe } = this.get();
      return safe;
    }
  }

  User.init(
    {
      name: { type: DataTypes.STRING(50), allowNull: false },
      email: { type: DataTypes.STRING(100), allowNull: false, unique: true },
      password_hash: { type: DataTypes.STRING(255), allowNull: false },
      role: { type: DataTypes.ENUM('ADMIN', 'USER'), allowNull: false, defaultValue: 'USER' },
      refresh_token_hash: { type: DataTypes.STRING(255), allowNull: true },
      refresh_expires_at: { type: DataTypes.DATE, allowNull: true },
    },
    { sequelize, modelName: 'User', tableName: 'users', underscored: true, createdAt: 'created_at', updatedAt: 'updated_at' }
  );

  return User;
};
