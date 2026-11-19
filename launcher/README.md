# ToolLauncher

Windows-лаунчер для Minecraft 26.3, 1.21.11 и 1.21.4. Все три сборки ToolVisuals входят в один EXE. Для запуска требуется минимум 2 ГБ ОЗУ.

Лаунчер устанавливает Minecraft, Fabric и подходящую Java из официальных источников. У каждой версии отдельные миры, настройки и моды.

Возможности:

- Каталог модов Modrinth и выбранных официальных проектов GitHub с установкой зависимостей.
- Импорт локальных Fabric-модов, включение, выключение и удаление установленных модов.
- Вход по нику или через Microsoft, переключение аккаунтов и импорт профилей из поддерживаемых лаунчеров.
- Автоматический Discord RPC.
- Выбор Java и памяти для игры: от 1024 МБ до половины физической ОЗУ.
- Выбор сборщика мусора отдельно для каждой версии Minecraft: Авто, G1, ZGC, Shenandoah и Parallel. При смене открывается описание с подтверждением «Я ознакомился, выбираю этот»; отмена сохраняет прежний выбор. Изменения сохраняются вместе с остальными настройками запуска.
- Проверка выбранной Java перед запуском: учитываются её фактическая версия и поддерживаемые сборщики. ZGC на Java 21/22 использует поколенческий режим; Shenandoah на Java 25 и новее предпочитает его при наличии поддержки. Недоступный вариант объясняется в окне выбора. Авто остаётся стандартным режимом.

Для входа через Microsoft нужен ID зарегистрированного приложения с поддержкой личных аккаунтов, device-code входом и доступом к Minecraft API.

Профили находятся в `%LOCALAPPDATA%/ToolLauncher/profiles`. Для переносной установки доступен аргумент `--data-root <папка>`.

Сначала соберите три клиентских JAR по инструкции в основном README. Для сборки лаунчера установите .NET SDK 10 + Rust 1.90+ (Windows x64 GNU/MSVC) и выполните из корня проекта:

```powershell
python launcher/build.py
```

Результат: `build/libs/ToolLauncher-1.3.11-win-x64.exe`. .NET входит в готовый EXE. Разработка без упаковки: `python launcher/build.py --development`.

EXE сохраняет все три клиентских JAR внутри одного файла без внутреннего сжатия сборок .NET: файл больше, зато загрузчику не нужно держать их распакованную копию в приватной памяти процесса. При сворачивании дерево интерфейса скрывается и восстанавливается без пересоздания; анимации прогресса и подсветки приостанавливаются у неактивного окна. Загрузки и наблюдение за игрой продолжаются. Discord RPC ожидает изменения состояния и ответы соединения вместо ежесекундного опроса; при отсутствии Discord повторное обнаружение остаётся ограниченным по частоте.

Фон главной — игровой кадр Minecraft от Xbox México (CC BY 3.0). Источник и лицензия: [launcher/licenses/minecraft-scene.txt](licenses/minecraft-scene.txt). Изображение декодируется под размер области и DPI монитора, сохраняя нужное разрешение при увеличении окна; изменения размера объединяются коротким одноразовым таймером. У свёрнутого главного окна графический контекст переключается в программный режим, а перед восстановлением возвращается стандартное аппаратное ускорение. Исходные JPG и значки не изменяются.

Java4Shell protection: Java is checked on startup and before game launch. Update installs the latest fixed Temurin 21/25 into the launcher data directory; system Java is preserved. Declining enforces and verifies -XX:-UseBASE64Intrinsics before every unpatched/unknown-runtime launch. Conflicting metadata flags and Java option environment variables cannot override this protection in launcher-created child processes. Unsupported or ineffective mitigation blocks launch. Java 21.0.13.1 and 25.0.5.1 are the known fixed baselines for Oracle/Temurin; unknown vendors and early-access versions retain mitigation. Downloads preserve TLS and SHA-256 checks, validate the extracted runtime before replacement and retain the previous managed runtime.
