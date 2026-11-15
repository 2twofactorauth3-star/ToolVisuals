using System.Windows;
using System.Windows.Controls;
using ToolLauncher.Accounts;
using ToolLauncher.Storage;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private void ConnectPreferenceEvents()
    {
        foreach (var input in new[] { NicknameInput, MicrosoftIdInput, Java21Input, Java25Input })
            input.TextChanged += (_, _) => UpdatePreferencesState();
        foreach (var check in new[] { AutoJavaCheck, AnimationsCheck, CompactModeCheck, MinimizeOnLaunchCheck })
        {
            check.Checked += (_, _) => UpdatePreferencesState();
            check.Unchecked += (_, _) => UpdatePreferencesState();
        }
        MemorySlider.ValueChanged += (_, _) => UpdatePreferencesState();
    }

    private LauncherSettings ReadPreferences() => services.Settings.Current with
    {
        Nickname = NicknameInput.Text.Trim(),
        MemoryMiB = (int)MemorySlider.Value,
        Java21 = Java21Input.Text.Trim(),
        Java25 = Java25Input.Text.Trim(),
        AutomaticJava = AutoJavaCheck.IsChecked == true,
        MicrosoftClientId = MicrosoftIdInput.Text.Trim(),
        InterfaceAnimations = AnimationsCheck.IsChecked == true,
        CompactInterface = CompactModeCheck.IsChecked == true,
        MinimizeOnLaunch = MinimizeOnLaunchCheck.IsChecked == true
    };

    private bool PreferencesChanged() => !initializing && !populatingSettings && ReadPreferences() != services.Settings.Current;

    private void UpdatePreferencesState()
    {
        if (initializing || populatingSettings) return;
        bool changed = PreferencesChanged();
        SaveSettingsButton.IsEnabled = SaveAccountSettingsButton.IsEnabled = changed && !busy;
        DiscardSettingsButton.IsEnabled = changed && !busy;
        SettingsStateText.Text = LauncherLanguage.Get(changed ? "unsaved" : "preferencesSaved");
    }

    private void PopulateAccountSettings()
    {
        populatingSettings = true;
        try
        {
            NicknameInput.Text = services.Settings.Current.Nickname;
        }
        finally { populatingSettings = false; }
        UpdatePreferencesState();
    }

    private void PopulateSettings()
    {
        populatingSettings = true;
        try
        {
            var settings = services.Settings.Current;
            NicknameInput.Text = settings.Nickname;
            MicrosoftIdInput.Text = settings.MicrosoftClientId;
            MemorySlider.Value = settings.MemoryMiB;
            Java21Input.Text = settings.Java21;
            Java25Input.Text = settings.Java25;
            AutoJavaCheck.IsChecked = settings.AutomaticJava;
            AnimationsCheck.IsChecked = settings.InterfaceAnimations;
            CompactModeCheck.IsChecked = settings.CompactInterface;
            MinimizeOnLaunchCheck.IsChecked = settings.MinimizeOnLaunch;
        }
        finally { populatingSettings = false; }
        UpdatePreferencesState();
    }

    private void SavePreferences()
    {
        var next = ReadPreferences();
        GameAccount.Offline(next.Nickname);
        if (next.MicrosoftClientId.Length > 0 && !Guid.TryParse(next.MicrosoftClientId, out _))
            throw new InvalidOperationException(LauncherLanguage.Get("invalidMicrosoftId"));
        services.AccountLibrary.SavePreferences(next);
        services.Presence?.Refresh();
        UpdateChrome();
    }

    private void DiscardSettingsClick(object sender, RoutedEventArgs e) { if (!busy) { PopulateSettings(); UpdateChrome(); } }
    private void RecommendedMemoryClick(object sender, RoutedEventArgs e) => MemorySlider.Value = services.Settings.Memory.Clamp(4096);

    private void SettingsCategoryClick(object sender, RoutedEventArgs e)
    {
        string category = (string)((Button)sender).Tag;
        GameSettingsPanel.Visibility = category == "game" ? Visibility.Visible : Visibility.Collapsed;
        AppearanceSettingsPanel.Visibility = category == "appearance" ? Visibility.Visible : Visibility.Collapsed;
        JavaSettingsPanel.Visibility = category == "java" ? Visibility.Visible : Visibility.Collapsed;
        foreach (var button in new[] { GameSettingsTab, AppearanceSettingsTab, JavaSettingsTab })
        {
            bool selected = (string)button.Tag == category;
            button.SetResourceReference(BackgroundProperty, selected ? "NavSelected" : "Surface");
            button.SetResourceReference(ForegroundProperty, selected ? "SelectedForeground" : "Muted");
        }
    }
}
