#!/bin/bash

# 小说网站前端部署脚本
# 使用方法: ./deploy-nginx.sh

set -e

echo "🚀 开始部署小说网站前端..."

# 配置变量（请根据实际情况修改）
PROJECT_NAME="novel-frontend"
BUILD_DIR="dist"
DEPLOY_DIR="/var/www/novel-frontend"
NGINX_CONFIG_DIR="/etc/nginx/sites-available"
NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"
BACKUP_DIR="/var/backups/nginx-configs"

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "❌ 请使用root权限运行此脚本"
    exit 1
fi

# 1. 构建项目
echo "📦 构建Vue.js项目..."
if [ ! -f "package.json" ]; then
    echo "❌ 未找到package.json文件，请在项目根目录运行此脚本"
    exit 1
fi

npm run build

if [ ! -d "$BUILD_DIR" ]; then
    echo "❌ 构建失败，未找到dist目录"
    exit 1
fi

echo "✅ 项目构建完成"

# 2. 备份现有配置
echo "💾 备份现有配置..."
mkdir -p "$BACKUP_DIR"
if [ -f "$NGINX_CONFIG_DIR/$PROJECT_NAME" ]; then
    cp "$NGINX_CONFIG_DIR/$PROJECT_NAME" "$BACKUP_DIR/$PROJECT_NAME.$(date +%Y%m%d_%H%M%S).bak"
    echo "✅ 已备份现有nginx配置"
fi

# 3. 创建部署目录
echo "📁 创建部署目录..."
mkdir -p "$DEPLOY_DIR"

# 4. 备份现有文件
if [ -d "$DEPLOY_DIR" ] && [ "$(ls -A $DEPLOY_DIR)" ]; then
    echo "💾 备份现有网站文件..."
    tar -czf "$BACKUP_DIR/website-$(date +%Y%m%d_%H%M%S).tar.gz" -C "$DEPLOY_DIR" .
    echo "✅ 已备份现有网站文件"
fi

# 5. 部署新文件
echo "🚚 部署新文件..."
rm -rf "$DEPLOY_DIR"/*
cp -r "$BUILD_DIR"/* "$DEPLOY_DIR"/

# 设置正确的权限
chown -R www-data:www-data "$DEPLOY_DIR"
chmod -R 755 "$DEPLOY_DIR"

echo "✅ 文件部署完成"

# 6. 配置nginx
echo "⚙️  配置nginx..."

# 复制nginx配置文件
if [ -f "nginx-production.conf" ]; then
    cp nginx-production.conf "$NGINX_CONFIG_DIR/$PROJECT_NAME"
    echo "✅ 已复制nginx配置文件"
else
    echo "⚠️  未找到nginx-production.conf，请手动配置nginx"
fi

# 启用站点
if [ -f "$NGINX_CONFIG_DIR/$PROJECT_NAME" ]; then
    ln -sf "$NGINX_CONFIG_DIR/$PROJECT_NAME" "$NGINX_ENABLED_DIR/$PROJECT_NAME"
    echo "✅ 已启用nginx站点配置"
fi

# 7. 测试nginx配置
echo "🔍 测试nginx配置..."
if nginx -t; then
    echo "✅ nginx配置测试通过"
else
    echo "❌ nginx配置测试失败，请检查配置文件"
    exit 1
fi

# 8. 重载nginx
echo "🔄 重载nginx..."
systemctl reload nginx

if systemctl is-active --quiet nginx; then
    echo "✅ nginx重载成功"
else
    echo "❌ nginx重载失败"
    exit 1
fi

# 9. 显示部署信息
echo ""
echo "🎉 部署完成！"
echo "📍 部署路径: $DEPLOY_DIR"
echo "⚙️  nginx配置: $NGINX_CONFIG_DIR/$PROJECT_NAME"
echo "🌐 请访问你的域名查看网站"
echo ""
echo "📝 部署后检查清单:"
echo "   1. 检查网站是否正常访问"
echo "   2. 检查API接口是否正常"
echo "   3. 检查图片资源是否正常显示"
echo "   4. 检查Vue Router路由是否正常"
echo ""
echo "🔧 如需回滚，备份文件位于: $BACKUP_DIR"

# 10. 可选：显示nginx状态
echo ""
echo "📊 nginx状态:"
systemctl status nginx --no-pager -l

echo ""
echo "✨ 部署脚本执行完成！"