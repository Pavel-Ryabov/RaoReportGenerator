package ru.kamikadze_zm.raoreportgenerator.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kamikadze_zm.raoreportgenerator.Button22Excel;
import ru.kamikadze_zm.raoreportgenerator.Button22Movie;
import ru.kamikadze_zm.raoreportgenerator.ExcelException;
import ru.kamikadze_zm.raoreportgenerator.MainApp;
import ru.kamikadze_zm.raoreportgenerator.playreports.PlayReportMovie;
import ru.kamikadze_zm.raoreportgenerator.playreports.PlayReportsParser;

public class MainController implements Initializable {

    private static final Logger LOG = LogManager.getLogger(MainController.class);

    private Stage mainStage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void process(ActionEvent event) {
        File stpGrid = showFileChooser("Выберите сетку СТП", false);

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(getInputDir());
        directoryChooser.setTitle("Выберите папку с плэй репортами");
        File playReportsDir = directoryChooser.showDialog(mainStage);
        if (playReportsDir == null || !playReportsDir.isDirectory()) {
            MainApp.showErrorAndExit("Папка c плэй репортами не выбрана");
        }

        String savePath;
        File saveFile = showFileChooser("Выберите путь сохранения", true);
        if (saveFile == null) {
            savePath = MainApp.SETTINGS.getRaoPath();
        } else {
            savePath = saveFile.getAbsolutePath();
        }

        List<Button22Movie> button22Movies;
        try {
            button22Movies = Button22Excel.parseSTP(stpGrid);
        } catch (ExcelException e) {
            MainApp.showErrorAndExit(e.getMessage());
            return;
        }

        List<String> errors = new ArrayList<>();
        PlayReportsParser prp = new PlayReportsParser(playReportsDir);
        if (!prp.getErrors().isEmpty()) {
            errors.addAll(prp.getErrors());
        }
        if (!prp.getIgnoredMovies().isEmpty()) {
            errors.add("Из-за слишком многих выходов были пропущены следующие файлы: " + prp.getIgnoredMovies());
        }

        List<PlayReportMovie> playReports = prp.getMovies();
        try {
            Button22Excel.combine(button22Movies, playReports, savePath);
        } catch (ExcelException e) {
            MainApp.showErrorAndExit(e.getMessage());
            return;
        }
        finish(errors);
    }

    @FXML
    private void openSettings(ActionEvent event) {
        Stage settings = new Stage();
        settings.setResizable(false);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            settings.setTitle("Настройки");
            settings.setScene(scene);
            settings.initModality(Modality.WINDOW_MODAL);
            settings.initOwner(mainStage);
            settings.show();
        } catch (IOException e) {
            LOG.warn("Load Settings.fxml exception: ", e);
            MainApp.showMessage("Ошибка", "Не удалось загрузить окно настроек", Alert.AlertType.ERROR);
        }
    }

    private void finish(List<String> errors) {
        MainApp.showMessage("Выполнение завершено", "Выполнение завершено", Alert.AlertType.INFORMATION);
        errors.stream().forEach(e -> showError(e));
        MainApp.exit(0);
    }

    private void showError(String message) {
        MainApp.showMessage("Ошибка", message, Alert.AlertType.ERROR);
    }

    private File showFileChooser(String title, boolean save) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialDirectory(getInputDir());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        if (save) {
            return fileChooser.showSaveDialog(mainStage);
        }
        return fileChooser.showOpenDialog(mainStage);
    }

    private File getInputDir() {
        String inputDir = MainApp.SETTINGS.getInputDir();
        File f = new File(inputDir);
        if (!f.exists()) {
            f = new File("");
        }
        return f;
    }

    public void setStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
}
