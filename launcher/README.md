# ToolLauncher

Windows-лаунчер для Minecraft 26.3, 1.21.11 и 1.21.4. Три клиентских JAR ToolVisuals входят в один EXE. У каждой версии отдельные миры, настройки и моды в `%LOCALAPPDATA%/ToolLauncher/profiles`. Для переносной установки доступен аргумент `--data-root <папка>`.

Лаунчер устанавливает Minecraft, Fabric, библиотеки и ресурсы из официальных источников. Java 21 и 25 определяются автоматически; при необходимости загружается Eclipse Temurin с проверкой SHA-256. Можно выбрать свой `java.exe` и объём памяти. Настройки меняются по кнопке сохранения.

Каталог Modrinth фильтруется по Fabric и выбранной версии. Обязательные зависимости загружаются вместе с модом. CurseForge использует собственный API-ключ, указанный в настройках; если автор ограничил прямую загрузку, доступен переход на сайт и импорт JAR. Источник GitHub показывает выбранные официальные репозитории и устанавливает подходящие релизные файлы с опубликованным SHA-256. Есть импорт локальных Fabric-модов, выключение и удаление с сохранением резервных файлов. Встроенными компонентами ToolVisuals управляет сам клиент. Изменять файлы запущенного профиля нельзя.

Вход по нику доступен для одиночной игры и серверов с локальными аккаунтами. Для Microsoft предусмотрен device-code вход через браузер и обновление сессии. В настройках нужен ID собственного приложения Microsoft с разрешёнными личными аккаунтами, public client/device-code входом и доступом к Minecraft API. Лаунчер не использует чужие application ID. Refresh-токен и ключ CurseForge зашифрованы Windows DPAPI и не переносятся на другой аккаунт Windows.

Discord RPC всегда включён и использует общее приложение ToolVisuals. Его Application ID закреплён в коде: изменить его или выключить RPC через интерфейс либо конфигурацию нельзя. Активность показывает `ToolLauncher <Minecraft>` при выборе версии и состояние настроек, когда выбор снят. Во время игры активностью управляет сам ToolVisuals; после завершения лаунчер восстанавливает свою. Кнопка ведёт на [t.me/toolvisuals](https://t.me/toolvisuals).

Сначала соберите три клиентских JAR по инструкции в основном README. Для сборки лаунчера установите .NET SDK 10 и выполните из корня проекта:

```powershell
python launcher/build.py
```

Результат: `build/libs/ToolLauncher-1.0.0-win-x64.exe`. .NET входит в готовый EXE. Исходники и необходимые ресурсы публикуются; игровые файлы, собранные JAR, настройки, ключи, логи и кэши в Git не входят. Разработка без упаковки: `python launcher/build.py --development`.

Сетевые контракты: [Modrinth API](https://docs.modrinth.com/api/), [CurseForge API](https://docs.curseforge.com/rest-api/), [Fabric Meta](https://meta.fabricmc.net/), [Temurin API](https://api.adoptium.net/), [Microsoft device authorization](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-device-code), [Discord RPC](https://docs.discord.com/developers/topics/rpc).
