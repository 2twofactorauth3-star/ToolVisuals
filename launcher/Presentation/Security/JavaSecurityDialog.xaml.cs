using System.Windows;

namespace ToolLauncher.Presentation.Security;

public partial class JavaSecurityDialog : Window
{
    public JavaSecurityDialog(string runtimes)
    {
        InitializeComponent();
        Title = LauncherLanguage.Get("javaSecurityTitle");
        RuntimeText.Text = runtimes;
        Loaded += (_, _) =>
        {
            MaxHeight = Math.Max(300, Math.Min(650, SystemParameters.WorkArea.Height - 48));
            MaxWidth = Math.Max(320, SystemParameters.WorkArea.Width - 48);
            UpdateButton.Focus();
        };
    }
    private void UpdateClick(object sender, RoutedEventArgs e) => DialogResult = true;
    private void DeclineClick(object sender, RoutedEventArgs e) => DialogResult = false;
}
