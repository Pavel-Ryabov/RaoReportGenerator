package xyz.pary.raoreportgenerator.kinopoisk;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

public class Browser {

    private static final Logger LOG = LogManager.getLogger(Browser.class);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:54.0) Gecko/20100101 Firefox/54.0";

    private static final String WINDOW_TITLE = "Окно браузера";

    private final WebView browser;
    private final Stage window;

    private final ReadOnlyBooleanWrapper completedProperty = new ReadOnlyBooleanWrapper(true);

    private volatile boolean isCompleted = false;

    private Document document;

    private String currentUrl;
    private int attemptCounter;
    private Timer cancellationTimer;

    public Browser(WebView webView) {
        if (webView != null) {
            browser = webView;
        } else {
            browser = new WebView();
        }

        window = new Stage();
        window.setTitle(WINDOW_TITLE);
        window.setScene(new Scene(new VBox(browser), 800, 600));

        WebEngine webEngine = browser.getEngine();
        webEngine.setUserAgent(USER_AGENT);
        webEngine.setJavaScriptEnabled(false);

        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {

                    switch (newValue) {
                        case SCHEDULED: {
                            setCompleted(false);
                            break;
                        }
                        case SUCCEEDED:
                            if (webEngine.getDocument() != null) {
                                document = webEngine.getDocument();
                            } else {
                                System.out.println(webEngine.executeScript("document.documentElement.innerHTML"));
                            }
                            LOG.info("Page loaded");
                            setCompleted(true);
                            break;
                        case FAILED:
                            LOG.info("Page load failed");
                            document = null;
                            setCompleted(true);
                            break;
                        case CANCELLED:
                            LOG.info("Page load cancelled");
                            document = null;
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

        webEngine.documentProperty().addListener((v, o, n) -> {
            if (n != null) {
                document = n;
            }
        });
    }

    public Browser() {
        this(null);
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

    public void openWindow(String title) {
        window.setTitle(title);
        window.show();
    }

    public void closeWindow() {
        window.setTitle(WINDOW_TITLE);
        window.hide();
    }

    public boolean windowIsOpen() {
        return window.isShowing();
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
        if (value) {
            cancelCancellationTimer();
        }
        isCompleted = value;
        Platform.runLater(() -> completedProperty.set(value));
    }

    private void load(String url) {
        Platform.runLater(() -> browser.getEngine().load(url));
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
        cancellationTimer.schedule(new CancellationTask(browser.getEngine().getLoadWorker()), 120000);
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
