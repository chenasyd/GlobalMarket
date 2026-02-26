# GlobalMarket (原 ServerMarket) 修改总结

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

---

##  1. 代码包名重构

### 修改内容
将插件包名从 `com.blank038.servermarket` 重构为 `com.upgrade.globalmarket`

### 主要修改
- 所有 Java 源文件的包声明
- `plugin.yml` 中的 main 类路径
- 配置文件引用路径
- 依赖注入和反射调用

### 修改文件示例
- `bukkit/src/main/resources/plugin.yml`
  - `main: com.upgrade.globalmarket.internal.plugin.GlobalMarket`
  - `name: GlobalMarket`（移除了插件名中的空格）

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

#### 代码实现
```java
if ("type".equals(section.getString("action"))) {
    List<String> typeLore = new ArrayList<>();
    typeLore.add("&f选择分类:");
    // 添加 "all" 类型
    String allDisplayName = DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault("all", "all");
    if ("all".equals(this.currentType)) {
        typeLore.add("&a✓ " + allDisplayName);
    } else {
        typeLore.add("&f  " + allDisplayName);
    }
    // 添加自定义类型
    for (Map.Entry<String, List<String>> entry : DataContainer.SALE_TYPES.entrySet()) {
        String typeKey = entry.getKey();
        String displayName = DataContainer.SALE_TYPE_DISPLAY_NAME.getOrDefault(typeKey, typeKey);
        if (typeKey.equals(this.currentType)) {
            typeLore.add("&a✓ " + displayName);
        } else {
            typeLore.add("&f  " + displayName);
        }
    }
    itemMeta.setLore(typeLore.stream().map(TextUtil::formatHexColor).collect(Collectors.toList()));
}
```

### 4.2 排序按钮 Lore 优化

#### 显示效果
```
选择排序:
  默认
✓ 价格高到低  ← 当前选中
  价格低到高
```

#### 代码实现
```java
else if ("sort".equals(section.getString("action"))) {
    List<String> sortLore = new ArrayList<>();
    sortLore.add("&f选择排序:");
    for (Map.Entry<String, String> entry : DataContainer.SORT_TYPE_DISPLAY_NAME.entrySet()) {
        String sortKey = entry.getKey();
        String displayName = entry.getValue();
        if (sortKey.equals(this.currentSort)) {
            sortLore.add("&a✓ " + displayName);
        } else {
            sortLore.add("&f  " + displayName);
        }
    }
    itemMeta.setLore(sortLore.stream().map(TextUtil::formatHexColor).collect(Collectors.toList()));
}
```

### 配色方案
- 选中项：`&a✓` 绿色打勾
- 未选中：`&f  ` 白色空格前缀

---

## 5. 平台兼容性改进

### Folia 平台支持

#### 修改文件
`bukkit/src/main/java/com/upgrade/globalmarket/internal/platform/PlatformHandler.java`

#### 核心逻辑
```java
public static void initPlatform() {
    if (CoreUtil.isFolia()) {
        try {
            Class<? extends IPlatformApi> classes = 
                (Class<? extends IPlatformApi>) Class.forName(
                    "com.upgrade.globalmarket.internal.platform.folia.FoliaPlatformApi");
            setPlatform(classes.newInstance());
            return;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            GlobalMarket.getInstance().getLogger().log(Level.WARNING, 
                "Folia platform API not available, falling back to Bukkit platform");
        }
    }
    setPlatform(new BukkitPlatformApi());
}
```

#### 特性
- 自动检测运行平台（Bukkit/Folia）
- 动态加载对应平台 API
- 优雅的降级机制（Folia 不可用时使用 Bukkit）

---

## 6. 编译错误修复

### 6.1 添加静态方法

#### GlobalMarket.java
```java
public static IStorageHandler getStorageHandler() {
    return storageHandler;
}

public static void setStorageHandler(IStorageHandler handler) {
    GlobalMarket.storageHandler = handler;
}
```

#### I18n.java
```java
public static void init(String language) {
    new I18n(language);
}
```

### 6.2 修复插件名称限制

#### plugin.yml
```yaml
# 修改前
name: "Global Market"  # ❌ 包含空格

# 修改后
name: "GlobalMarket"   # ✅ 移除空格
```

**原因**: Spigot/Paper 不允许插件名称包含空格（0x20 字符）

---

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

## 功能总结

| 功能 | 状态 | 说明 |
|------|------|------|
| 包名重构 | ✅ 完成 | 重构为 `com.upgrade.globalmarket` |
| Maven 构建 | ✅ 完成 | 移除 Gradle，使用 Maven |
| 右键预览 | ✅ 完成 | Shulker Box 预览功能 |
| 分类 Lore | ✅ 完成 | 显示所有分类和选中状态 |
| 排序 Lore | ✅ 完成 | 显示所有排序方式和选中状态 |
| Folia 支持 | ✅ 完成 | 平台自动检测和降级 |
| 编译修复 | ✅ 完成 | 修复所有编译错误 |

---

## 相关文档

- [README.md](README.md) - 项目说明
- [README_EN.md](README_EN.md) - English README
- [PATCHES.md](PATCHES.md) - 补丁说明
- [MAVEN_BUILD.md](MAVEN_BUILD.md) - Maven 构建说明
- [SORT_MODIFICATION.md](SORT_MODIFICATION.md) - 排序修改说明

---

## 外部链接

- 原插件: https://github.com/blank038/ServerMarket
- 原作者主页: https://github.com/blank038
- 开源协议: GPL v3.0

---

**最后更新**: 2026-02-26
**当前版本**: GlobalMarket 2.7.3
