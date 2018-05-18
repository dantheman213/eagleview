package eagleview;

import eagleview.controllers.ConfigurationController;
import eagleview.controllers.ScreensaverController;
import eagleview.data.Config;
import eagleview.data.Utilities;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App extends Application {
    public static Config config;

    public static boolean isFullscreenMode = false;
    public static boolean isConfigurationMode = false;
    public static boolean isDialogSelectorPreviewMode = false;
    public static String previewWindowHandle = null;
    public static String[] arguments = null;

    public static void main( String[] args ) throws Exception {
        arguments = args;
        Utilities util = new Utilities();
        config = new Config();

        checkAppDirectories();

        Application.launch (args);
    }

    @Override
    public void start (Stage primaryStage) throws Exception {
        if(arguments.length > 0) {
            switch(arguments[0].toLowerCase().substring(0, 2)) {
                case "/s":
                    isFullscreenMode = true;
                    break;
                case "/c":
                    // Windows screensaver manager seems to pass /c:123456 (hwnd?) instead of just /c
                    isConfigurationMode = true;
                    break;
                case "/p":
                    if(arguments.length > 1) {
                        isDialogSelectorPreviewMode = true;
                        previewWindowHandle = arguments[1];
                    } else {
                        Alert alert = Utilities.showSimpleAlert("Preview Window Handle Required!");
                        alert.showAndWait();
                        exitApplication();
                    }
                    break;
            }
        } else {
            Alert alert = Utilities.showSimpleAlert("Install screensaver to use! Or read manual to activate manually.");
            alert.showAndWait();
            exitApplication();
        }

        primaryStage.setOnCloseRequest(e -> {
            exitApplication();
        });

        if(isFullscreenMode) {
            new ScreensaverController(primaryStage);
        } else if(isConfigurationMode) {
            new ConfigurationController().render(primaryStage);
        } else if(isDialogSelectorPreviewMode) {
            exitApplication();
        } else {
            exitApplication();
        }
    }

    private static void checkAppDirectories() throws Exception {
        File f = new File(Config.settings.videoCollectionDir);
        if (!f.exists()) {
            Files.createDirectories(Paths.get(Config.settings.videoCollectionDir));
        }

        f = new File(Config.settings.binDir);
        if(!f.exists()) {
            Files.createDirectories(Paths.get(Config.settings.binDir));
        }
    }

    public static void exitApplication() {
        Platform.exit();
        System.exit(0);
    }
}
