# ToolVisuals

Клиентский мод Fabric: настраиваемый HUD, визуальные эффекты, плащи, косметика и внутриигровое меню. Открытие меню — правый Shift; клавишу можно изменить в управлении Minecraft.

| Minecraft | Java | Клиентский JAR |
| --- | --- | --- |
| 26.3 | 25 | ToolVisuals-1.4+fabric-26.3.jar |
| 1.21.11 | 21 | ToolVisuals-1.4+fabric-1.21.11.jar |
| 1.21.4 | 21 | ToolVisuals-1.4+fabric-1.21.4.jar |

Установите Fabric Loader 0.19.5 или новее и один JAR под свою версию Minecraft. Fabric API и зависимости уже включены в готовые клиентские сборки.

## Сборка

Нужны JDK 25, JDK 21, Python 3.10 или новее и доступ к интернету. Gradle скачивается через Wrapper. Зависимости скачиваются с официальных адресов из закреплённых манифестов с проверкой размера и SHA-512.

```shell
python build-tools/dependencies.py
./gradlew build
```

На Windows используйте `py -3 build-tools/dependencies.py` и `gradlew.bat build`. Если команда `python` недоступна, передайте Gradle путь к Python через `-PpythonExecutable=путь`.

Три клиентских JAR появятся в `build/libs`. Для отдельной версии: `./gradlew :minecraft1214:build` или `./gradlew :minecraft12111:build`. Команда `./gradlew release` дополнительно соберёт серверный плагин `ToolVisualsPlugin-1.4.jar` для Paper/Folia. Он нужен для серверной синхронизации поддерживаемых социальных функций.

Общий код находится в `src/main`, адаптеры — в `versions/common/main` и `versions/<minecraft>/main`, исходники серверного плагина — в `server/paper/src/main`. Исходник Windows-помощника MusicHUD находится в `native/music/windows`; его можно пересобрать командой `python native/music/windows/build.py` на Windows 10/11.

## Лицензии и связь

Собственный код ToolVisuals распространяется по [LGPL-3.0-only](LICENSE); сопутствующий текст GPLv3 находится в [COPYING](COPYING). Сторонние компоненты, производные шейдеры, шрифт и исключения перечислены в [NOTICE](NOTICE); их лицензии сохраняются отдельно.

Предложения и обновления: [t.me/toolvisuals](https://t.me/toolvisuals). Разработка: [t.me/hplugin](https://t.me/hplugin).
