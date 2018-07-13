package ru.kamikadze_zm.raoreportgenerator;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import ru.kamikadze_zm.raoreportgenerator.controller.MainController;
import ru.kamikadze_zm.raoreportgenerator.settings.Settings;

public class MainApp extends Application {

    public static final Settings SETTINGS = Settings.load();

    private static HostServices hostServices;

    @Override
    public void start(Stage stage) throws Exception {
        hostServices = getHostServices();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();

        loader.<MainController>getController().setStage(stage);

        Scene scene = new Scene(root);
        stage.setTitle("Генератор данных для РАО отчета");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void showErrorAndExit(String message) {
        showMessage("Ошибка", message, AlertType.ERROR);
        exit(1);
    }

    public static void showMessage(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    public static void exit(int status) {
        System.exit(status);
    }

    public static HostServices hostServices() {
        return hostServices;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
