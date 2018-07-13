package ru.kamikadze_zm.raoreportgenerator.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import ru.kamikadze_zm.raoreportgenerator.MainApp;
import ru.kamikadze_zm.raoreportgenerator.settings.Settings;
import ru.kamikadze_zm.raoreportgenerator.settings.StpSettings;

public class SettingsController implements Initializable {
    
    @FXML
    private TextField inputDir;
    @FXML
    private TextField outputDir;
    @FXML
    private TextField movieInfoFile;
    @FXML
    private TextField playReportsFile;
    @FXML
    private TextField raoFile;
    @FXML
    private ListView<String> lvExclusions;
    @FXML
    private TextField exclusion;
    
    @FXML
    private TextField sheetIndex;
    @FXML
    private TextField startRowIndex;
    @FXML
    private TextField movieNameColumnIndex;
    @FXML
    private TextField genreColumnIndex;
    @FXML
    private TextField countryColumnIndex;
    @FXML
    private TextField yearColumnIndex;
    @FXML
    private TextField directorColumnIndex;
    @FXML
    private TextField composerColumnIndex;
    @FXML
    private TextField durationColumnIndex;
    
    private final ObservableList<String> exclusions = FXCollections.observableArrayList();
    
    private Settings s;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        s = MainApp.SETTINGS;
        inputDir.setText(s.getInputDir());
        outputDir.setText(s.getOutputDir());
        movieInfoFile.setText(s.getMoviesInfoFile());
        playReportsFile.setText(s.getPlayReportsFile());
        raoFile.setText(s.getRaoFile());
        exclusions.addAll(s.getPlayReportsExclusions());
        
        lvExclusions.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                    if (newVal != null) {
                        Platform.runLater(() -> {
                            exclusionDialog(newVal);
                            lvExclusions.getSelectionModel().clearSelection();
                            
                        });
                    }
                });
        lvExclusions.setItems(exclusions);
        
        StpSettings stp = s.getStpSettings();
        
        sheetIndex.setText(getIndexToInput(stp.getSheetIndex()));
        startRowIndex.setText(getIndexToInput(stp.getStartRowIndex()));
        movieNameColumnIndex.setText(getIndexToInput(stp.getMovieNameColumnIndex()));
        genreColumnIndex.setText(getIndexToInput(stp.getGenreColumnIndex()));
        countryColumnIndex.setText(getIndexToInput(stp.getCountryColumnIndex()));
        yearColumnIndex.setText(getIndexToInput(stp.getYearColumnIndex()));
        directorColumnIndex.setText(getIndexToInput(stp.getDirectorColumnIndex()));
        composerColumnIndex.setText(getIndexToInput(stp.getComposerColumnIndex()));
        durationColumnIndex.setText(getIndexToInput(stp.getDurationColumnIndex()));
    }
    
    @FXML
    private void addExclusion(ActionEvent event) {
        exclusions.add(exclusion.getText());
        exclusion.setText("");
    }
    
    private void exclusionDialog(String s) {
        Dialog<Pair<ButtonType, String>> dialog = new Dialog<>();
        dialog.setTitle("Редактирование исключения");
        dialog.setHeaderText("Редактирование исключения");
        
        Label label = new Label("Исключение: ");
        TextField text = new TextField();
        text.setText(s);
        
        GridPane grid = new GridPane();
        grid.add(label, 1, 1);
        grid.add(text, 2, 1);
        dialog.getDialogPane().setContent(grid);
        
        ButtonType buttonTypeChange = new ButtonType("Изменить");
        ButtonType buttonTypeRemove = new ButtonType("Удалить");
        ButtonType buttonTypeCancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeChange, buttonTypeRemove, buttonTypeCancel);
        
        dialog.setResultConverter(button -> {
            return new Pair<>(button, text.getText());
        });
        
        Optional<Pair<ButtonType, String>> result = dialog.showAndWait();
        if (result.get().getKey() == buttonTypeRemove) {
            removeExclusion(s);
        } else if (result.get().getKey() == buttonTypeChange) {
            int index = exclusions.indexOf(s);
            if (index != -1) {
                lvExclusions.setItems(null);
                exclusions.set(index, result.get().getValue());
                lvExclusions.setItems(exclusions);
            }
        } else {
            dialog.close();
        }
    }
    
    private void removeExclusion(String s) {
        lvExclusions.setItems(null);
        exclusions.remove(s);
        lvExclusions.setItems(exclusions);
    }
    
    @FXML
    private void cancel(ActionEvent event) {
        closeWindow();
    }
    
    @FXML
    private void save(ActionEvent event) {
        s.setInputDir(inputDir.getText());
        s.setOutputDir(outputDir.getText());
        s.setMoviesInfoFile(movieInfoFile.getText());
        s.setPlayReportsFile(playReportsFile.getText());
        s.setRaoFile(raoFile.getText());
        s.setPlayReportsExclusions(new ArrayList<>(exclusions));
        
        StpSettings stpSettings = new StpSettings(
                getIndexFromInput(sheetIndex.getText()),
                getIndexFromInput(startRowIndex.getText()),
                getIndexFromInput(movieNameColumnIndex.getText()),
                getIndexFromInput(genreColumnIndex.getText()),
                getIndexFromInput(countryColumnIndex.getText()),
                getIndexFromInput(yearColumnIndex.getText()),
                getIndexFromInput(directorColumnIndex.getText()),
                getIndexFromInput(composerColumnIndex.getText()),
                getIndexFromInput(durationColumnIndex.getText())
        );
        s.setStpSettings(stpSettings);
        MainApp.showWriteAccessMessages();
        if (!s.canWriteToOutputDir()) {
            s.setOutputDir(Settings.APP_DIR);
        }
        
        s.save();
        closeWindow();
    }
    
    private String getIndexToInput(int index) {
        if (index != -1) {
            index += 1;
        }
        return String.valueOf(index);
    }
    
    private int getIndexFromInput(String input) {
        int index = Integer.parseInt(input);
        if (index != -1) {
            index -= 1;
        }
        return index;
    }
    
    private void closeWindow() {
        ((Stage) outputDir.getScene().getWindow()).close();
    }
    
    @FXML
    private void aboutContains(ActionEvent event) {
        MainApp.hostServices().showDocument("https://docs.oracle.com/javase/7/docs/api/java/lang/String.html#contains(java.lang.CharSequence)");
    }
}
