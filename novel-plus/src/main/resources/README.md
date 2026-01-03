# 配置文件说明

## ShardingSphere 配置

### 快速开始

1. 复制示例配置文件：
```bash
cp shardingsphere.yaml.example shardingsphere.yaml
```

2. 修改 `shardingsphere.yaml` 中的数据库连接信息：
   - `jdbcUrl`: 数据库连接地址
   - `username`: 数据库用户名
   - `password`: 数据库密码

3. 确保 `shardingsphere.yaml` 已添加到 `.gitignore`，避免提交敏感信息

### 注意事项

⚠️ **重要**: `shardingsphere.yaml` 包含敏感信息（数据库密码等），请勿提交到版本控制系统！

该文件已在 `.gitignore` 中配置忽略：
```
/src/main/resources/shardingsphere.yaml
```

### 配置文件说明

- `shardingsphere.yaml.example` - 示例配置文件（可提交到版本控制）
- `shardingsphere.yaml` - 实际配置文件（包含敏感信息，不应提交）

### 更多信息

详细的分库分表配置说明请参考：[ShardingSphere分库分表实现文档](../../doc/ShardingSphere分库分表实现文档.md)
