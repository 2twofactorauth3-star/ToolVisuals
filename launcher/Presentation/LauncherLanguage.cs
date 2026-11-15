using System.Windows;

namespace ToolLauncher.Presentation;

public static class LauncherLanguage
{
    private static readonly Dictionary<string, (string En, string Ru)> Texts = new()
    {
        ["home"] = ("Home", "Главная"), ["mods"] = ("Discover mods", "Каталог модов"), ["installed"] = ("My mods", "Мои моды"), ["settings"] = ("Settings", "Настройки"),
        ["community"] = ("Community ↗", "Сообщество ↗"), ["source"] = ("Source code ↗", "Исходный код ↗"), ["choose"] = ("Version", "Версия"), ["ready"] = ("Ready when you are", "Можно начинать"), ["selectFirst"] = ("Choose a version to get started", "Выбери версию для начала"), ["play"] = ("Play", "Играть"), ["cancel"] = ("Cancel", "Отменить"),
        ["search"] = ("Search mods…", "Поиск модов…"), ["searchButton"] = ("Search", "Найти"), ["install"] = ("Install", "Установить"), ["website"] = ("Website ↗", "На сайт ↗"), ["import"] = ("Import JAR", "Импорт JAR"),
        ["openProfile"] = ("Open profile", "Папка профиля"), ["openLogs"] = ("Open logs", "Папка логов"), ["refresh"] = ("Refresh", "Обновить"), ["enable"] = ("Enable", "Включить"), ["disable"] = ("Disable", "Выключить"), ["remove"] = ("Remove", "Убрать"),
        ["emptyMods"] = ("No mods found. Try a different name.", "Моды не найдены. Попробуй другое название."), ["emptyInstalled"] = ("Launch this profile once to install ToolVisuals, or add your own mods.", "Запусти профиль, чтобы установить ToolVisuals, или добавь свои моды."),
        ["account"] = ("Your account", "Твой аккаунт"), ["nickname"] = ("Local nickname", "Локальный ник"), ["nicknameHint"] = ("For singleplayer and servers that allow local accounts.", "Для одиночной игры и серверов с локальными аккаунтами."),
        ["signIn"] = ("Sign in with Microsoft", "Войти через Microsoft"), ["signOut"] = ("Use local account", "Локальный аккаунт"), ["msClientId"] = ("Microsoft application ID", "ID приложения Microsoft"), ["msHint"] = ("Your own public-client application, with personal accounts and Minecraft API access.", "Своё приложение с поддержкой личных аккаунтов и доступом к Minecraft API."),
        ["memory"] = ("Memory allocation", "Оперативная память"), ["java"] = ("Java runtime", "Среда Java"), ["autoJava"] = ("Install Java automatically when needed", "Устанавливать Java, если нужной версии нет"), ["javaHint"] = ("Leave paths empty for automatic detection. Java 25 is used for 26.3; Java 21 for 1.21.x.", "Оставь пути пустыми для автопоиска. Для 26.3 нужна Java 25, для 1.21.x — Java 21."),
        ["memoryBudget"] = ("Installed: {0} GB · Maximum for the game: {1} GB", "Установлено: {0} ГБ · Максимум для игры: {1} ГБ"),
        ["savedAccounts"] = ("Saved accounts", "Сохранённые аккаунты"), ["accountLocal"] = ("Nickname", "Ник"), ["removeAccount"] = ("Remove this saved account", "Удалить сохранённый аккаунт"),
        ["accountSignInRequired"] = ("Sign in with Microsoft to use this account.", "Чтобы использовать этот аккаунт, войди через Microsoft."),
        ["importTitle"] = ("Bring your accounts", "Твои аккаунты уже здесь"), ["importSubtitle"] = ("Welcome to ToolLauncher", "Добро пожаловать в ToolLauncher"),
        ["importScanning"] = ("Looking for account profiles in installed launchers…", "Ищем профили аккаунтов в установленных лаунчерах…"),
        ["importNotFound"] = ("No account profiles found automatically.", "Автоматически найти аккаунты не удалось."),
        ["importFound"] = ("Found {1} accounts in: {0}", "Найдено аккаунтов: {1}. Источники: {0}"),
        ["importUnreadable"] = ("Could not read {0} files. Details are in the launcher log.", "Не удалось прочитать файлов: {0}. Подробности — в логе лаунчера."),
        ["importEmpty"] = ("Choose a portable launcher's folder, or continue with a new nickname.", "Укажи папку портативного лаунчера или продолжи с новым ником."),
        ["startingAccount"] = ("Starting account", "Стартовый аккаунт"),
        ["importDisclosure"] = ("Choose which account names to import. Microsoft accounts need a new sign-in. Your other launchers stay unchanged.", "Выбери, какие аккаунты перенести. Для Microsoft понадобится войти заново. Данные других лаунчеров останутся на месте."),
        ["importFolder"] = ("Choose launcher folder", "Указать папку лаунчера"), ["importSkip"] = ("Skip", "Пропустить"), ["importAccounts"] = ("Import accounts", "Импорт аккаунтов"),
        ["catalogUnavailable"] = ("The mod catalog is temporarily unavailable. Try again shortly.", "Каталог модов временно недоступен. Попробуй чуть позже."),
        ["save"] = ("Save settings", "Сохранить настройки"), ["saved"] = ("Settings saved", "Настройки сохранены"), ["error"] = ("Could not complete the operation", "Не удалось завершить действие"), ["done"] = ("Done", "Готово"), ["busy"] = ("Preparing your session…", "Готовим всё для игры…"),
        ["metadata"] = ("Checking Minecraft and Fabric", "Проверяем Minecraft и Fabric"), ["libraries"] = ("Downloading libraries", "Загружаем библиотеки"), ["assets"] = ("Downloading game resources", "Загружаем ресурсы игры"), ["minecraft"] = ("Downloading Minecraft", "Загружаем Minecraft"), ["visuals"] = ("Installing ToolVisuals", "Устанавливаем ToolVisuals"), ["javaDownload"] = ("Installing Java", "Устанавливаем Java"), ["modDownload"] = ("Resolving and downloading mods", "Проверяем и загружаем моды"),
        ["auth"] = ("Signing in…", "Входим в аккаунт…"), ["running"] = ("Minecraft is running", "Minecraft запущен"), ["exitError"] = ("Minecraft closed with an error. Details are in the launcher log.", "Minecraft закрылся с ошибкой. Подробности — в логе лаунчера."), ["previous"] = ("← Previous", "← Назад"), ["next"] = ("Next →", "Далее →"), ["browse"] = ("Browse…", "Обзор…"),
        ["installedHint"] = ("Disabling keeps the file. Removing moves it to the profile backup folder.", "Выключение сохраняет файл. Удалённые моды перемещаются в резервную папку профиля."), ["chooseAccount"] = ("Manage account", "Настроить аккаунт"), ["clearVersion"] = ("Clear selection", "Снять выбор"),
        ["launchGame"] = ("Launch game", "Запуск игры"),
        ["launchReady"] = ("Ready to launch", "Готов к запуску"),
        ["launchBusy"] = ("Working…", "Выполняется действие…"),
        ["launchChoose"] = ("Choose a version", "Выбери версию"),
        ["launchSignIn"] = ("Sign-in required", "Нужен вход"),
        ["launchVersion"] = ("Version", "Версия"),
        ["pickVersion"] = ("Choose version", "Выбрать версию"),
        ["clientBuild"] = ("Client {0}", "Клиент {0}"),
        ["runningAction"] = ("Running", "Запущено"),
        ["busyAction"] = ("Please wait", "Подождите"),
        ["accounts"] = ("Accounts", "Аккаунты"),
        ["launchAccount"] = ("Account", "Аккаунт"),
        ["launchSettingsLink"] = ("Configure launch →", "Настроить запуск →"),
        ["sessionSettings"] = ("Launch settings", "Параметры запуска"),
        ["gameSettings"] = ("Game", "Игра"),
        ["profileHint"] = ("Separate worlds and mods", "Отдельные миры и моды"),
        ["appearance"] = ("Interface", "Интерфейс"),
        ["afterLaunch"] = ("After launching Minecraft", "После запуска Minecraft"),
        ["minimizeOnLaunch"] = ("Minimize the launcher after the game starts", "Сворачивать лаунчер после старта игры"),
        ["gameOptionsHint"] = ("Graphics and gameplay options are configured in Minecraft.", "Графика и игровые параметры настраиваются в Minecraft."),
        ["interfaceAnimations"] = ("Animate control highlights", "Анимировать подсветку элементов"),
        ["animationsHint"] = ("Short hover transitions. Follows the Windows animation preference.", "Короткие переходы при наведении. Учитывается настройка анимаций Windows."),
        ["compactInterface"] = ("Compact spacing", "Компактные отступы"),
        ["compactHint"] = ("Reduce spacing while keeping text readable. Applied when saved.", "Меньше отступов с сохранением читаемости текста. Применяется после сохранения."),
        ["resetMemory"] = ("Reset to 4 GB within the hardware limit", "Вернуть 4 ГБ с учётом предела памяти"),
        ["unsaved"] = ("Unsaved changes", "Есть несохранённые правки"),
        ["preferencesSaved"] = ("All changes saved", "Изменения сохранены"),
        ["discard"] = ("Discard changes", "Отменить правки"),
        ["discardOnClose"] = ("Close without saving your settings changes?", "Закрыть лаунчер без сохранения изменений настроек?"),
        ["invalidMicrosoftId"] = ("Enter a valid Microsoft application ID.", "Укажи корректный ID приложения Microsoft."),
        ["readingMods"] = ("Reading profile mods…", "Читаем моды профиля…"),
        ["homeModsCount"] = ("{0} enabled · {1} disabled", "{0} включено · {1} отключено"),
        ["homeModsEmpty"] = ("Add mods for this version", "Добавь моды для этой версии"),
        ["homeModsError"] = ("Could not read mods · Retry", "Не удалось прочитать · Повторить"),
        ["homeProfileHint"] = ("Separate worlds, mods and settings", "Отдельные миры, моды и настройки"),
        ["gigabytes"] = ("GB", "ГБ"),
        ["minimize"] = ("Minimize", "Свернуть"),
        ["maximize"] = ("Maximize or restore", "Развернуть или восстановить"),
        ["close"] = ("Close", "Закрыть"),
    };

    public static string Current { get; private set; } = "en";

    public static string Get(string key) => Texts.TryGetValue(key, out var text) ? Current == "ru" ? text.Ru : text.En : key;

    public static void Apply(string language)
    {
        Current = language;
        foreach (var item in Texts) Application.Current.Resources["Text." + item.Key] = language == "ru" ? item.Value.Ru : item.Value.En;
    }
}
