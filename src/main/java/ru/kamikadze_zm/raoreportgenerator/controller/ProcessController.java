package ru.kamikadze_zm.raoreportgenerator.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kamikadze_zm.raoreportgenerator.ExcelException;
import ru.kamikadze_zm.raoreportgenerator.MainApp;
import ru.kamikadze_zm.raoreportgenerator.MovieInfo;
import ru.kamikadze_zm.raoreportgenerator.kinopoisk.Browser;
import ru.kamikadze_zm.raoreportgenerator.kinopoisk.KinopoiskParser;
import ru.kamikadze_zm.raoreportgenerator.kinopoisk.TempUtil;
import ru.kamikadze_zm.raoreportgenerator.playreports.ExcelPlayReports;
import ru.kamikadze_zm.raoreportgenerator.playreports.PlayReportMovie;
import ru.kamikadze_zm.raoreportgenerator.playreports.PlayReportsParser;
import ru.kamikadze_zm.raoreportgenerator.rao.Combiner;
import ru.kamikadze_zm.raoreportgenerator.MoviesInfoExcel;
import stp.StpGridParser;

public class ProcessController implements Initializable {

    private static final Logger LOG = LogManager.getLogger(ProcessController.class);

    private static final int NUMBER_FILMS_AT_A_TIME = 25;

    private Stage stage;
    private boolean moviesInfo;
    private boolean moviesInfoComplete;
    private boolean playreports;
    private boolean playReportsComplete;
    private boolean combine;
    private boolean combineComplete;

    private int countFilms = 0;

    private int lastFilmIndex = 0;

    private final List<String> errors = new ArrayList<>();

    private List<MovieInfo> kinopoiskMovies;
    private List<PlayReportMovie> playReportMovies;

    private List<MovieInfo> restoredFilms;

    private File kinopoiskMoviesFile;
    private File playReportMoviesFile;

    private int processed = 0;

    @FXML
    private Label lblProcessed;
    @FXML
    private Label lblCount;
    @FXML
    private CheckBox cbPlayReports;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void process(Stage stage, boolean moviesInfo, boolean restore, boolean playreports, boolean combine) {

        this.stage = stage;
        this.moviesInfo = moviesInfo;
        this.playreports = playreports;
        this.combine = combine;

        if (combine) {
            checkDataForCombine();
        }

        if (moviesInfo) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите сетку СТП");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
            fileChooser.getExtensionFilters().add(extFilter);
            File stpGrid = fileChooser.showOpenDialog(stage);

            if (stpGrid == null) {
                MainApp.showErrorAndExit("Файл не выбран");
            }

            StpGridParser sgp;
            try {
                sgp = new StpGridParser(stpGrid);
                LOG.info("Parse stp grid complete");
                this.countFilms = sgp.getMovies().size();
                lblCount.setText(String.valueOf(this.countFilms));
            } catch (ExcelException e) {
                MainApp.showErrorAndExit(e.getMessage());
                return;
            }

            kinopoiskMovies = new ArrayList<>(sgp.getMovies());
            Collections.sort(kinopoiskMovies);

            if (restore) {
                restoredFilms = TempUtil.restore();
            } else {
                TempUtil.deleteTemp();
            }

            processKinopoiskPart();
        }

        if (playreports) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку с плэй репортами");
            File playReportsDir = directoryChooser.showDialog(stage);

            if (playReportsDir == null || !playReportsDir.isDirectory()) {
                MainApp.showErrorAndExit("Папка c плэй репортами не выбрана");
            } else {
                Task<List<PlayReportMovie>> playreportskTask = new Task<List<PlayReportMovie>>() {
                    @Override
                    protected List<PlayReportMovie> call() throws Exception {
                        PlayReportsParser prp = new PlayReportsParser(playReportsDir);
                        if (!prp.getErrors().isEmpty()) {
                            errors.addAll(prp.getErrors());
                        }
                        return prp.getMovies();
                    }
                };

                playreportskTask.setOnSucceeded(e -> completePlayReports(playreportskTask.getValue()));
                playreportskTask.setOnFailed(e -> MainApp.showErrorAndExit("Произошла непредвиденная ошибка."));

                Thread playreportskThread = new Thread(playreportskTask);
                playreportskThread.setDaemon(true);
                playreportskThread.start();
            }
        } else {
            cbPlayReports.setSelected(true);
        }

        if (!moviesInfo && !playreports && combine) {
            combine();
        }
    }

    private void processKinopoiskPart() {
        List<MovieInfo> kinopoiskMoviesPart = getNextKinopoiskMoviesPart();
        Browser browser = new Browser();
        KinopoiskParser kp = new KinopoiskParser(kinopoiskMoviesPart, browser, restoredFilms);
        kp.progressProperty().addListener((obs, ov, nv) -> updateProcessed(nv.intValue() - ov.intValue()));

        kp.completedProperty().addListener((obs, ov, nv) -> {
            if (nv == true) {
                completeKinopoiskPart();
            }
        });

        kp.parse();
    }

    private List<MovieInfo> getNextKinopoiskMoviesPart() {
        int next = lastFilmIndex + NUMBER_FILMS_AT_A_TIME;
        if (next > kinopoiskMovies.size()) {
            next = kinopoiskMovies.size();
        }
        List<MovieInfo> part = kinopoiskMovies.subList(lastFilmIndex, next);
        lastFilmIndex = next;
        return part;
    }

    private void completeKinopoiskPart() {
        if (lastFilmIndex >= kinopoiskMovies.size()) {
            completeKinopoisk();
        } else {
            processKinopoiskPart();
        }
    }

    private void updateProcessed(int value) {
        processed += value;
        Platform.runLater(() -> lblProcessed.setText(String.valueOf(processed)));
    }

    private void completeKinopoisk() {
        this.moviesInfoComplete = true;
        saveKinopoiskResults(kinopoiskMovies);
        combine();
        finish();
    }

    private void completePlayReports(List<PlayReportMovie> playReportMovies) {
        this.playReportsComplete = true;
        savePlayReportsToExcel(playReportMovies);
        cbPlayReports.setSelected(true);
        this.playReportMovies = playReportMovies;
        combine();
        finish();
    }

    private void checkDataForCombine() {
        if (combine) {
            if (!moviesInfo && !playreports) {
                kinopoiskMoviesFile = showFileChooser("Выберите готовый файл с результатами кинопоиска");
                playReportMoviesFile = showFileChooser("Выберите готовый файл с данными плей репортов");
                if (kinopoiskMoviesFile == null || playReportMoviesFile == null) {
                    combine = false;
                }
            } else if (!moviesInfo) {
                kinopoiskMoviesFile = showFileChooser("Выберите готовый файл с результатами кинопоиска");
                if (kinopoiskMoviesFile == null) {
                    combine = false;
                }
            } else if (!playreports) {
                playReportMoviesFile = showFileChooser("Выберите готовый файл с плей репортами");
                if (playReportMoviesFile == null) {
                    combine = false;
                }
            }
        }
    }

    private File showFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLSX", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showOpenDialog(stage);
    }

    private void combine() {
        if (!combine) {
            return;
        }

        try {
            if (moviesInfo && playreports) {
                if (moviesInfoComplete && playReportsComplete) {
                    Combiner.combine(kinopoiskMovies, playReportMovies);
                    combineComplete();
                }
            } else if (moviesInfo) {
                if (moviesInfoComplete) {
                    Combiner.combineFilms(kinopoiskMovies, playReportMoviesFile);
                    combineComplete();
                }
            } else if (playreports) {
                if (playReportsComplete) {
                    Combiner.combinePlayReports(kinopoiskMoviesFile, playReportMovies);
                    combineComplete();
                }
            } else {
                Combiner.combine(kinopoiskMoviesFile, playReportMoviesFile);
                combineComplete();
                finish();
            }
        } catch (ExcelException e) {
            MainApp.showErrorAndExit(e.getMessage());
        }

    }

    private void combineComplete() {
        this.combineComplete = true;
    }

    private void saveKinopoiskResults(List<MovieInfo> moviesInfo) {
        try {
            MoviesInfoExcel.save(moviesInfo, MainApp.SETTINGS.getMoviesInfoPath(), true);
        } catch (ExcelException e) {
            showError(e.getMessage());
        }
    }

    private void savePlayReportsToExcel(List<PlayReportMovie> playReportMovies) {
        try {
            ExcelPlayReports.save(playReportMovies);
        } catch (ExcelException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        MainApp.showMessage("Ошибка", message, Alert.AlertType.ERROR);
    }

    private void finish() {
        if (moviesInfoIsCompleted() && playreportsIsCompleted() && combineIsCompleted()) {
            if (moviesInfo) {
                TempUtil.deleteTemp();
            }
            MainApp.showMessage("Выполнение завершено", "Выполнение завершено", Alert.AlertType.INFORMATION);
            errors.stream().forEach(e -> showError(e));
            MainApp.exit(0);
        }
    }

    private boolean moviesInfoIsCompleted() {
        return !moviesInfo || moviesInfoComplete;
    }

    private boolean playreportsIsCompleted() {
        return !playreports || playReportsComplete;
    }

    private boolean combineIsCompleted() {
        return !combine || combineComplete;

    }
}
