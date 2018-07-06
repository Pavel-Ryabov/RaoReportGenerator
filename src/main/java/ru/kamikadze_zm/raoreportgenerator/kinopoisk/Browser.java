package ru.kamikadze_zm.raoreportgenerator.kinopoisk;

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

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    //private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:54.0) Gecko/20100101 Firefox/54.0";

    private static WebEngine webEngine;

    private final ReadOnlyBooleanWrapper completed = new ReadOnlyBooleanWrapper(true);
    private Document document;

    public Browser() {
        WebView browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.setUserAgent(USER_AGENT);

        webEngine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) -> {

                    if (newValue == Worker.State.SUCCEEDED) {
                        try {
                            document = webEngine.getDocument();
                        } catch (Exception e) {
                            if (e.getClass() != ParseKinopoiskException.class) {
                                LOG.warn("Unexpected exception: ", e);
                            }
                        } finally {
                            setCompleted(true);
                        }

                    } else if (newValue == Worker.State.FAILED) {
                        setCompleted(true);
                    }
                });
    }

    public void loadPage(String url) throws ParseKinopoiskException {
        if (!getCompleted()) {
            throw new ParseKinopoiskException("Не загружена предыдущая страница");
        }
        setCompleted(false);
        document = null;
        Platform.runLater(() -> webEngine.load(url));
    }

    public boolean getCompleted() {
        return completedProperty().get();
    }

    public ReadOnlyBooleanWrapper completedProperty() {
        return completed;
    }

    public Document getDocument() {
        return document;
    }

    private void setCompleted(boolean value) {
        Platform.runLater(() -> completed.setValue(value));
    }
}
