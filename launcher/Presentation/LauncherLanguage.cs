using System.Windows;

namespace ToolLauncher.Presentation;

public static class LauncherLanguage
{
    private static readonly Dictionary<string, (string En, string Ru)> Texts = new()
    {
        ["home"] = ("Overview", "Главная"), ["mods"] = ("Discover mods", "Каталог модов"), ["installed"] = ("My mods", "Мои моды"), ["settings"] = ("Settings", "Настройки"),
        ["community"] = ("Community ↗", "Сообщество ↗"), ["source"] = ("Source code ↗", "Исходный код ↗"), ["tagline"] = ("Make Minecraft yours.", "Твой Minecraft. Твои правила."),
        ["heroTitle"] = ("A new way\nto feel the game.", "Взгляни на игру\nпо-новому."), ["heroBody"] = ("Your visuals, your mods, your next adventure.\nOne place to bring it all together.", "Твои визуалы, твои моды, твои приключения.\nВсё, что нужно для игры, в одном месте."),
        ["choose"] = ("CHOOSE YOUR MINECRAFT", "ВЫБЕРИ СВОЙ MINECRAFT"), ["versionHint"] = ("Each version has its own worlds, settings and mods.", "У каждой версии свои миры, настройки и моды."),
        ["ready"] = ("Ready when you are", "Можно начинать"), ["selectFirst"] = ("Choose a version to get started", "Выбери версию для начала"), ["play"] = ("Launch Minecraft   →", "Запустить Minecraft   →"), ["cancel"] = ("Cancel", "Отменить"),
        ["visualsTitle"] = ("ToolVisuals, built in", "ToolVisuals уже внутри"), ["visualsBody"] = ("Your complete visual client, with Fabric and performance mods included.", "Визуальный клиент, Fabric и моды оптимизации уже в комплекте."),
        ["modsTitle"] = ("A library without limits", "Моды под твой стиль"), ["modsBody"] = ("Browse Modrinth, CurseForge and official GitHub releases. Required dependencies follow along.", "Modrinth, CurseForge и официальные релизы GitHub. Обязательные зависимости устанавливаются вместе с модом."),
        ["search"] = ("Search mods…", "Поиск модов…"), ["searchButton"] = ("Search", "Найти"), ["install"] = ("Install", "Установить"), ["website"] = ("Website ↗", "На сайт ↗"), ["import"] = ("Import JAR", "Импорт JAR"),
        ["openProfile"] = ("Open profile", "Папка профиля"), ["openLogs"] = ("Open logs", "Папка логов"), ["refresh"] = ("Refresh", "Обновить"), ["enable"] = ("Enable", "Включить"), ["disable"] = ("Disable", "Выключить"), ["remove"] = ("Remove", "Убрать"),
        ["emptyMods"] = ("No mods found. Try another search or source.", "Моды не найдены. Измени запрос или источник."), ["emptyInstalled"] = ("Launch this profile once to install ToolVisuals, or add your own mods.", "Запусти профиль, чтобы установить ToolVisuals, или добавь свои моды."),
        ["account"] = ("Your account", "Твой аккаунт"), ["nickname"] = ("Local nickname", "Локальный ник"), ["nicknameHint"] = ("For singleplayer and servers that allow local accounts.", "Для одиночной игры и серверов с локальными аккаунтами."),
        ["signIn"] = ("Sign in with Microsoft", "Войти через Microsoft"), ["signOut"] = ("Use local account", "Локальный аккаунт"), ["msClientId"] = ("Microsoft application ID", "ID приложения Microsoft"), ["msHint"] = ("Your own public-client application, with personal accounts and Minecraft API access.", "Своё приложение с поддержкой личных аккаунтов и доступом к Minecraft API."),
        ["memory"] = ("Memory allocation", "Оперативная память"), ["java"] = ("Java runtime", "Среда Java"), ["autoJava"] = ("Install Java automatically when needed", "Устанавливать Java, если нужной версии нет"), ["javaHint"] = ("Leave paths empty for automatic detection. Java 25 is used for 26.3; Java 21 for 1.21.x.", "Оставь пути пустыми для автопоиска. Для 26.3 нужна Java 25, для 1.21.x — Java 21."),
        ["cfKey"] = ("CurseForge API key", "API-ключ CurseForge"), ["cfHint"] = ("Optional. Kept encrypted on this PC. Modrinth works without a key.", "Необязательно. Хранится зашифрованным на этом ПК. Modrinth работает без ключа."), ["rpcHint"] = ("Always enabled. Uses the ToolVisuals application and follows your selected Minecraft version.", "Всегда включён. Общее приложение ToolVisuals, активность зависит от выбранной версии Minecraft."),
        ["save"] = ("Save settings", "Сохранить настройки"), ["saved"] = ("Settings saved", "Настройки сохранены"), ["error"] = ("Could not complete the operation", "Не удалось завершить действие"), ["done"] = ("Done", "Готово"), ["busy"] = ("Preparing your session…", "Готовим всё для игры…"),
        ["metadata"] = ("Checking Minecraft and Fabric", "Проверяем Minecraft и Fabric"), ["libraries"] = ("Downloading libraries", "Загружаем библиотеки"), ["assets"] = ("Downloading game resources", "Загружаем ресурсы игры"), ["minecraft"] = ("Downloading Minecraft", "Загружаем Minecraft"), ["visuals"] = ("Installing ToolVisuals", "Устанавливаем ToolVisuals"), ["javaDownload"] = ("Installing Java", "Устанавливаем Java"), ["modDownload"] = ("Resolving and downloading mods", "Проверяем и загружаем моды"),
        ["auth"] = ("Signing in…", "Входим в аккаунт…"), ["running"] = ("Minecraft is running", "Minecraft запущен"), ["exitError"] = ("Minecraft closed with an error. Details are in the launcher log.", "Minecraft закрылся с ошибкой. Подробности — в логе лаунчера."), ["previous"] = ("← Previous", "← Назад"), ["next"] = ("Next →", "Далее →"), ["browse"] = ("Browse…", "Обзор…"),
        ["protection"] = ("Verified downloads · separate profiles", "Проверка загрузок · отдельные профили"), ["installedHint"] = ("Disabling keeps the file. Removing moves it to the profile backup folder.", "Выключение сохраняет файл. Удалённые моды перемещаются в резервную папку профиля."), ["chooseAccount"] = ("Manage account", "Настроить аккаунт"), ["clearVersion"] = ("Clear selection", "Снять выбор"), ["sourceHint"] = ("Fabric mods compatible with your selected Minecraft version.", "Fabric-моды, совместимые с выбранной версией Minecraft.")
    };
    public static string Current { get; private set; } = "en";
    public static string Get(string key) => Texts.TryGetValue(key, out var text) ? Current == "ru" ? text.Ru : text.En : key;
    public static void Apply(string language)
    {
        Current = language;
        foreach (var item in Texts) Application.Current.Resources["Text." + item.Key] = language == "ru" ? item.Value.Ru : item.Value.En;
    }
}
