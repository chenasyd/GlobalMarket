@echo off
cd /d d:\ServerMarketdo
set JAVA_HOME=C:\Program Files\Java\jdk-18
set PATH=%JAVA_HOME%\bin;%PATH%

echo Compiling TextUtil.java...
javac -cp "build/temp;depends/AyCore-1.1.0-BETA.jar;depends/item-nbt-api-plugin-2.12.2.jar;bukkit/libs/NyEconomy.jar;bukkit/libs/PlayerPoints.jar" ^
    -d build/temp ^
    bukkit/src/main/java/com/blank038/servermarket/internal/util/TextUtil.java

if %ERRORLEVEL% EQU 0 (
    echo TextUtil.java compiled successfully!
) else (
    echo TextUtil.java compilation failed!
    exit /b 1
)

echo Compiling MarketData.java...
javac -cp "build/temp;depends/AyCore-1.1.0-BETA.jar;depends/item-nbt-api-plugin-2.12.2.jar;bukkit/libs/NyEconomy.jar;bukkit/libs/PlayerPoints.jar" ^
    -d build/temp ^
    bukkit/src/main/java/com/blank038/servermarket/api/entity/MarketData.java

if %ERRORLEVEL% EQU 0 (
    echo MarketData.java compiled successfully!
) else (
    echo MarketData.java compilation failed!
    exit /b 1
)

echo Recreating JAR...
cd build/temp
jar -cf ../libs/ServerMarket-2.7.3-fixed.jar .
cd ..\..

echo Build completed! New JAR: build\libs\ServerMarket-2.7.3-fixed.jar
