#!/bin/bash

echo "开始部署前端项目..."

# 停止并删除旧容器
docker-compose down

# 删除旧镜像（可选）
docker image prune -f

# 构建并启动新容器
docker-compose up -d --build

echo "部署完成！"
echo "访问地址: http://182.92.215.16"

# 查看容器状态
docker-compose ps