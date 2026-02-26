# 市场GUI默认排序修改

## 原作者信息

- **作者**: Blank038
- **源地址**: https://github.com/blank038/ServerMarket
- **主页**: https://github.com/blank038

## 修改内容

已修改市场GUI中物品的默认排序方式，现在默认按上架时间从早到晚排序。

## 修改的文件

- `bukkit/src/main/java/com/blank038/servermarket/api/handler/sort/impl/DefaultSortHandlerImpl.java`

## 修改详情

### 修改前
```java
@Override
public int compare(SaleCache o1, SaleCache o2) {
    return 0;
}
```

### 修改后
```java
@Override
public int compare(SaleCache o1, SaleCache o2) {
    // 按上架时间从早到晚排序
    long timeDiff = o1.getPostTime() - o2.getPostTime();
    if (timeDiff == 0) {
        return 0;
    }
    return timeDiff < 0 ? -1 : 1;
}
```

## 排序逻辑

- **排序依据**: 物品的上架时间 (`postTime`)
- **排序方向**: 从早到晚（升序）
- **结果**: 
  - 上架时间早的物品显示在靠前的槽位和页面
  - 上架时间晚的物品显示在靠后的槽位和页面
  - 第一个页面的第一个槽位显示最早上架的物品
  - 最后一个页面的最后一个槽位显示最晚上架的物品

## 其他排序方式

插件仍然支持其他排序方式，玩家可以通过点击"切换排序方式"按钮切换：

1. **default** - 默认（按上架时间从早到晚）← 已修改
2. **price_high** - 价格从高到低
3. **price_low** - 价格从低到高

## 构建命令

```bash
mvn clean package -DskipTests
```

构建完成后，新的 JAR 文件位于：`build/libs/ServerMarket-2.7.3.jar`
