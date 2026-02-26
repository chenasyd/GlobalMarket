# Maven 构建说明

## 原作者信息

- **作者**: Blank038
- **源地址**: https://github.com/blank038/ServerMarket
- **主页**: https://github.com/blank038

## 项目已成功迁移到 Maven 构建系统

### 构建命令

```bash
# 完整构建
mvn clean package -DskipTests

# 仅构建 bukkit 模块
mvn clean package -DskipTests -pl bukkit -am

# 快速重新打包（不重新编译）
mvn package -DskipTests
```

### 构建产物

构建成功后，最终的 JAR 文件位于：`build/libs/ServerMarket-2.7.3.jar`

### 项目结构

- `pom.xml` - 根 POM，定义所有模块
- `bukkit/pom.xml` - Bukkit 模块 POM
- `folia/pom.xml` - Folia 模块 POM（当前未包含在构建中）
- `assembly/pom.xml` - 组装模块，将所有模块打包成最终 JAR

### 依赖

主要依赖包括：
- Spigot API 1.16.5-R0.1-SNAPSHOT
- AyCore 1.4.5-BETA
- NBT-API 2.12.2
- Vault API 1.7
- PlayerPoints 2.1.20（本地安装）
- NyEconomy 1.0.0（本地安装）

### 本地依赖安装

如果需要重新安装本地依赖：

```bash
mvn install:install-file -Dfile=bukkit/libs/PlayerPoints.jar -DgroupId=org.black_ixx -DartifactId=PlayerPoints -Dversion=2.1.20 -Dpackaging=jar
mvn install:install-file -Dfile=bukkit/libs/NyEconomy.jar -DgroupId=com.mc9y -DartifactId=NyEconomy -Dversion=1.0.0 -Dpackaging=jar
```

### 注意事项

1. Folia 模块当前已从构建中排除，因为它需要 Paper API 和 Java 21
2. Netty 库被包含在最终的 JAR 中（shade 插件）
3. 警告信息可以忽略，这些是由于 shade 插件合并多个 JAR 时的正常提示

### 代码修改摘要

本次构建包含以下修改：

1. **TextUtil.java** - 添加了 null 检查以修复 NullPointerException
2. **MarketData.java** - 修复了 economyType 可能为 null 的问题
3. **资源文件** - 添加了 market/example.yml 和 market/exampleLegacy.yml
4. **预览功能** - ShulkerPreviewGui（PreviewGui）已编译并包含在 JAR 中
