using System.Windows;
using System.Windows.Controls;
using ToolLauncher.Minecraft;
using ToolLauncher.Minecraft.GarbageCollection;
using ToolLauncher.Presentation.GarbageCollection;

namespace ToolLauncher.Presentation;

public partial class MainWindow
{
    private GarbageCollectionSettings pendingGarbageCollection = new();
    private bool updatingGarbageCollection;
    private void ConnectGarbageCollectionEvents()
    {
        CollectorProfilePicker.ItemsSource = GameTarget.All;
        foreach (var information in GarbageCollectorInfo.All) CollectorPicker.Items.Add(new ComboBoxItem { Tag = information.Collector });
        CollectorProfilePicker.SelectionChanged += (_, _) => RefreshCollectorSelection();
        CollectorPicker.SelectionChanged += CollectorChanged;
    }
    private void PopulateGarbageCollection()
    {
        pendingGarbageCollection = services.Settings.Current.GarbageCollection;
        CollectorProfilePicker.SelectedItem = GameTarget.All.FirstOrDefault(target => target.Minecraft == services.Settings.Current.SelectedVersion) ?? GameTarget.All[0];
        RefreshCollectorSelection();
    }
    private void RefreshCollectorSelection()
    {
        if (CollectorProfilePicker.SelectedItem is not GameTarget target) return;
        updatingGarbageCollection = true;
        try
        {
            foreach (ComboBoxItem item in CollectorPicker.Items)
            {
                var information = GarbageCollectorInfo.Find((GarbageCollector)item.Tag);
                item.Content = information.Collector == GarbageCollector.Auto ? LauncherLanguage.Get("gcAuto") : information.Name;
                if (information.Collector == pendingGarbageCollection.For(target)) CollectorPicker.SelectedItem = item;
            }
        }
        finally { updatingGarbageCollection = false; }
    }
    private void CollectorChanged(object sender, SelectionChangedEventArgs e)
    {
        if (initializing || populatingSettings || updatingGarbageCollection || busy || CollectorProfilePicker.SelectedItem is not GameTarget target || CollectorPicker.SelectedItem is not ComboBoxItem item) return;
        var candidate = (GarbageCollector)item.Tag;
        if (candidate != pendingGarbageCollection.For(target)) ChooseCollector(target, candidate);
    }
    private void CollectorDetailsClick(object sender, RoutedEventArgs e)
    {
        if (!busy && CollectorProfilePicker.SelectedItem is GameTarget target) ChooseCollector(target, pendingGarbageCollection.For(target));
    }
    private void ChooseCollector(GameTarget target, GarbageCollector candidate)
    {
        var dialog = new GarbageCollectorDialog(services, target, ReadPreferences(), candidate) { Owner = this };
        if (dialog.ShowDialog() == true) pendingGarbageCollection = pendingGarbageCollection.Select(target, candidate);
        RefreshCollectorSelection();
        UpdatePreferencesState();
    }
}
