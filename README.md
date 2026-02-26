# GlobalMarket (原 ServerMarket)

## 原插件信息

- **原插件名称**: ServerMarket
- **原作者**: Blank038
- **源地址**: https://github.com/blank038/ServerMarket
- **主页**: https://github.com/blank038

## 修改概览

本次对 ServerMarket 插件进行了全面重构和功能增强，主要包括：
1. 代码包名重构
2. 编译系统迁移（Gradle → Maven）
3. 右键预览功能实现
4. GUI 显示逻辑优化（分类和排序）
5. 平台兼容性改进（支持 Folia）

##  1. 代码包名重构

### 修改内容
将插件包名从 `com.blank038.servermarket` 重构为 `com.upgrade.globalmarket`

### 主要修改
- 所有 Java 源文件的包声明
- `plugin.yml` 中的 main 类路径
- 配置文件引用路径
- 依赖注入和反射调用

---

## 2. 编译系统迁移

### 从 Gradle 迁移到 Maven

### 创建的文件
- `pom.xml` - 根 POM 配置
- `bukkit/pom.xml` - Bukkit 模块配置
- `folia/pom.xml` - Folia 模块配置
- `assembly/pom.xml` - 组装模块配置

### 构建命令
```bash
# 完整构建
mvn clean package -DskipTests

# 仅构建 bukkit 模块
mvn clean package -DskipTests -pl bukkit -am

# 快速重新打包
mvn package -DskipTests
```

### 构建产物
最终 JAR 文件位于：`build/libs/GlobalMarket-2.7.3.jar`

### 移除的 Gradle 文件
- `gradle/` 目录
- `.gradle/` 目录
- `build.gradle`
- `settings.gradle`
- `gradlew` / `gradlew.bat`

---

## 3. 右键预览功能

### 功能描述
实现了 Shulker Box（潜影盒）的右键预览功能，玩家可以右键点击市场中的潜影盒商品查看内部物品。

### 实现文件
- `bukkit/src/main/java/com/upgrade/globalmarket/internal/gui/impl/PreviewGui.java`

### 核心功能
```java
// 创建预览 GUI
public PreviewGui(Player player, ItemStack shulkerBox, int lastPage, 
                  String sourceMarketKey, int currentPage, FilterHandler filter)

// 打开预览界面
public void open()
```

### 特性
- 实时显示潜影盒内部物品
- 保持原物品的 NBT 数据和属性
- 支持点击返回主市场界面
- 自动恢复分页和过滤状态

---

## 4. GUI 显示逻辑优化

### 4.1 分类按钮 Lore 优化

#### 修改文件
`bukkit/src/main/java/com/upgrade/globalmarket/internal/gui/impl/MarketGui.java`

#### 修改内容
为分类按钮添加完整的分类列表显示，包括：
- "全部"（all）
- 自定义分类（如"道具"）

#### 显示效果
```
选择分类:
  全部
✓ 道具      ← 当前选中
```



### 4.2 排序按钮 Lore 优化

#### 显示效果
```
选择排序:
  默认
✓ 价格高到低  ← 当前选中
  价格低到高
```

## 配置文件说明

### 分类配置 (types.yml)
```yaml
default:
  all: "全部"
  none: "无分类"

types:
  道具:
    - "STONE"
    - "道具"
```

### 排序配置 (sorts.yml)
```yaml
default: "默认"
price_high: "价格高到低"
price_low: "价格低到高"
```

### 市场配置 (market/example.yml)
```yaml
types:
  - "all"
  - "道具"
```


---

## 相关文档

- [README.md](README.md) - 项目说明
- [README_EN.md](README_EN.md) - English README
- [PATCHES.md](PATCHES.md) - 补丁说明
- [MAVEN_BUILD.md](MAVEN_BUILD.md) - Maven 构建说明
- [SORT_MODIFICATION.md](SORT_MODIFICATION.md) - 排序修改说明

---


## 自述文件 & README

* zh_CN [简体中文](README.md)
* en [English](README_EN.md)

## 插件依赖

+ AyCore [Download](https://cdn.mc9y.com/files/AyCore/AyCore-1.2.1-BETA.jar)
+ NBTAPI [Download](https://www.spigotmc.org/resources/nbt-api.7939/)

## 插件功能

* 高度自定义 Gui 界面
* 支持 MySQL, YAML 格式存储
* 自定义商品原信息是否显示
* 自定义商品所在槽位
* 个人仓库及仓库面板自定义
* 支持版本: 1.8-1.20
* 支持多种货币(Vault, PlayerPoints, NyEconomy)
* 支持商品分类、商品搜索、商品黑名单
* 支持指定商品/分类价格范围

## 补丁

[补丁列表](PATCHES.md)

## 贡献者 & Contributors

<a href="https://github.com/blank038/ServerMarket/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=blank038/ServerMarket&anon=1" alt="Contributors"/>
</a>

### 翻译

[语言文件位置](https://github.com/blank038/ServerMarket/tree/master/bukkit/src/main/resources/language)

## 外部链接

- 原插件: https://github.com/blank038/ServerMarket
- 原作者主页: https://github.com/blank038

---
## 开源协议 & License

[GPL v3.0](https://opensource.org/license/gpl-3-0/)
