package ru.kamikadze_zm.raoreportgenerator.kinopoisk;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

public class Browser {

    private static final Logger LOG = LogManager.getLogger(Browser.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:54.0) Gecko/20100101 Firefox/54.0";

    private static WebEngine webEngine;

    private final ReadOnlyBooleanWrapper completedProperty = new ReadOnlyBooleanWrapper(true);

    private volatile boolean isCompleted = false;

    private Document document;

    private String currentUrl;
    private int attemptCounter;
    private Timer cancellationTimer;

    public Browser() {
        WebView browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.setUserAgent(USER_AGENT);

        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {

                    switch (newValue) {
                        case SUCCEEDED:
                            document = webEngine.getDocument();
                            LOG.info("Page loaded");
                            setCompleted(true);
                            break;
                        case FAILED:
                            LOG.info("Page load failed");
                            setCompleted(true);
                            break;
                        case CANCELLED:
                            LOG.info("Page load cancelled");
                            if (attemptCounter < 5) {
                                reloadPage();
                            } else {
                                setCompleted(true);
                            }
                            break;
                        default:
                            break;
                    }
                });
    }

    public void loadPage(String url) throws ParseKinopoiskException {
        if (!getCompleted()) {
            throw new ParseKinopoiskException("Не загружена предыдущая страница");
        }
        setCompleted(false);
        document = null;
        currentUrl = url;
        attemptCounter = 0;
        LOG.info("Page load - {}", url);
        load(url);
    }

    public boolean getCompleted() {
        return completedProperty().get();
    }

    public ReadOnlyBooleanWrapper completedProperty() {
        return completedProperty;
    }

    public Document getDocument() {
        return document;
    }

    private void setCompleted(boolean value) {
        cancelCancellationTimer();
        isCompleted = value;
        Platform.runLater(() -> completedProperty.set(value));
    }

    private void load(String url) {
        Platform.runLater(() -> webEngine.load(url));
        startCancellationTimer();
    }

    private void reloadPage() {
        if (isCompleted == true) {
            return;
        }
        attemptCounter++;
        LOG.info("Page reload. Attempt number - {}, Url - {}", attemptCounter, currentUrl);
        load(currentUrl);
    }

    private void startCancellationTimer() {
        cancellationTimer = new Timer(true);
        cancellationTimer.schedule(new CancellationTask(webEngine.getLoadWorker()), 120000);
        LOG.info("Cancellation timer started");
    }

    private void cancelCancellationTimer() {
        if (cancellationTimer != null) {
            cancellationTimer.cancel();
            LOG.info("Cancellation timer cancelled");
        }
    }

    private class CancellationTask extends TimerTask {

        private final Worker worker;

        public CancellationTask(Worker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            LOG.info("Cancellation task is run");
            Platform.runLater(() -> {
                if (worker.isRunning()) {
                    worker.cancel();
                }
            });
        }
    }
}
