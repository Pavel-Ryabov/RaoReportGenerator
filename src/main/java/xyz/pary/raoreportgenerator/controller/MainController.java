package xyz.pary.raoreportgenerator.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.pary.raoreportgenerator.MainApp;
import xyz.pary.raoreportgenerator.kinopoisk.TempUtil;

public class MainController implements Initializable {

    private static final Logger LOG = LogManager.getLogger(MainController.class);

    private Stage mainStage;

    @FXML
    private CheckBox cbMoviesInfo;
    @FXML
    private CheckBox cbRestore;
    @FXML
    private CheckBox cbPlayreports;
    @FXML
    private CheckBox cbCombine;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (TempUtil.checkTemp()) {
            cbRestore.setVisible(true);
        }
    }

    @FXML
    private void process(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Process.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            mainStage.setTitle("Обработка");
            mainStage.setScene(scene);
            mainStage.setResizable(true);
            mainStage.show();
            loader.<ProcessController>getController()
                    .process(mainStage, cbMoviesInfo.isSelected(), cbRestore.isSelected(), cbPlayreports.isSelected(), cbCombine.isSelected());
        } catch (IOException e) {
            LOG.warn("Load Process.fxml exception: ", e);
            MainApp.showMessage("Ошибка", "Не удалось загрузить окно обработки", Alert.AlertType.ERROR);
        }
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
    
    @FXML
    private void repeatCaptcha(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Process.fxml"));
        try {
            Parent root = loader.load();
            Scene scene = new Scene(root);
            mainStage.setTitle("Обработка");
            mainStage.setScene(scene);
            mainStage.setResizable(true);
            mainStage.show();
            loader.<ProcessController>getController()
                    .repeatCaptcha(mainStage);
        } catch (IOException e) {
            LOG.warn("Load Process.fxml exception: ", e);
            MainApp.showMessage("Ошибка", "Не удалось загрузить окно обработки", Alert.AlertType.ERROR);
        }
    }

    public void setStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
}
