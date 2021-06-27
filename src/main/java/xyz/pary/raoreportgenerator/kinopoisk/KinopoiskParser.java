package xyz.pary.raoreportgenerator.kinopoisk;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ObservableValue;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import xyz.pary.raoreportgenerator.MovieInfo;
import xyz.pary.raoreportgenerator.MovieInfo.NotFound;
import xyz.pary.raoreportgenerator.kinopoisk.remoteserver.HttpClient;
import xyz.pary.raoreportgenerator.kinopoisk.remoteserver.RemoteMovieInfo;

public class KinopoiskParser {

    private static final Logger LOG = LogManager.getLogger(KinopoiskParser.class);

    private static final String HOST = "https://www.kinopoisk.ru";
    private static final String ENCODING = "UTF8";

    private static final Pattern FILM_PATTERN = Pattern.compile("film/.+|series/.+");
    private static final String CAPTCHA = "showcaptcha";
    private static final String CAST_URL_PART = "cast/";
    private static final String STUDIO_URL_PART = "studio/";

    private final Iterator<MovieInfo> iterator;

    private final Browser browser;
    private Timer waitTimer;

    private final Countries countries = Countries.INSTANCE;

    private final List<MovieInfo> restored;

    private final ReadOnlyIntegerWrapper progress = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyBooleanWrapper completed = new ReadOnlyBooleanWrapper(false);

    private MovieInfo currentMovie;
    private RemoteMovieInfo remoteMovieInfo;
    private Status status;
    private boolean russian;
    
    private final Random random = new Random();

    public KinopoiskParser(List<MovieInfo> movies, Browser browser, List<MovieInfo> restored) {
        this.iterator = movies.iterator();
        this.browser = browser;
        if (restored != null) {
            this.restored = restored;
        } else {
            this.restored = Collections.emptyList();
        }

        this.browser.completedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue == true) {
                if (this.browser.getDocument() == null) {
                    currentMovie.addNotFound(NotFound.ERROR);
                    status = Status.NEXT;
                }
                nextStep();
            }

        });

    }

    public int getProgress() {
        return progressProperty().get();
    }

    public ReadOnlyIntegerWrapper progressProperty() {
        return progress;
    }

    public boolean getCompleted() {
        return completedProperty().getValue();
    }

    public ReadOnlyBooleanWrapper completedProperty() {
        return completed;
    }

    public void parse() {
        loadNextMovie();
    }

    private void nextStep() {
        try {
            switch (status) {
                case NEXT:
                    loadNextMovie();
                    break;
                case SEARCH:
                    parseSearchResult(this.browser.getDocument(), currentMovie);
                    break;
                case FILM:
                    parseFilm(this.browser.getDocument(), currentMovie);
                    break;
                case STUDIO:
                    parseStudio(this.browser.getDocument(), currentMovie);
                    break;
                case CAST:
                    parseCast(this.browser.getDocument(), currentMovie);
                    break;
                default:
                    LOG.warn("Unknown status: " + status);
                    loadNextMovie();
            }
        } catch (Exception e) {
            if (e.getClass() != ParseKinopoiskException.class) {
                LOG.warn("Unexpected exception: ", e);
            }
            currentMovie.addNotFound(NotFound.ERROR);
            loadNextMovie();
        }
    }

    private void loadNextMovie() {
        if (currentMovie != null) {
            TempUtil.save(currentMovie);
            LOG.info("Finish {}", currentMovie);
            if (remoteMovieInfo.getId() != null) {
                HttpClient.post(remoteMovieInfo);
            }
        }
        if (iterator.hasNext()) {
            updateProgress();
            currentMovie = iterator.next();
            LOG.info("Start {}", currentMovie);
            remoteMovieInfo = new RemoteMovieInfo(currentMovie.getName());
            russian = false;
            int ri = -1;
            if (!restored.isEmpty()) {
                ri = restored.indexOf(currentMovie);
            }
            if (ri != -1) {
                MovieInfo rm = restored.get(ri);
                currentMovie.setOriginalName(rm.getOriginalName());
                currentMovie.setLink(rm.getLink());
                currentMovie.setNotFound(rm.getNotFound());
                currentMovie.setDirector(rm.getDirector());
                currentMovie.setComposer(rm.getComposer());
                currentMovie.setStudio(rm.getStudio());
                status = Status.NEXT;
                nextStep();
            } else {
                loadSearchPage(currentMovie);
            }
        } else {
            setCompleted(true);
        }
    }

    private void waitAndLoadPage(String url, long waitMs) throws ParseKinopoiskException {
        if (waitTimer != null) {
            waitTimer.cancel();
        }
        waitTimer = new Timer(true);
        int offset = random.nextInt(2500);
        boolean plus = random.nextBoolean();
        waitMs = plus ? waitMs + offset : waitMs - offset;
        LOG.info("Start timer. Url - {}, waiting = {}", url, waitMs);
        waitTimer.schedule(new WaitingTask(this, url), waitMs);
    }

    private void loadPage(String url) throws ParseKinopoiskException {
        Platform.runLater(() -> browser.loadPage(url));
    }

    private void loadSearchPage(MovieInfo m) throws ParseKinopoiskException {
        status = Status.SEARCH;
        waitAndLoadPage(getUrl(m.getName(), m.getYear(), m.getCountry()), 25000);
        LOG.info("Search");
    }

    private void parseSearchResult(Document d, MovieInfo m) {
        String location = d.getDocumentURI();

        LOG.info("Location: " + location);

        Matcher matcher = FILM_PATTERN.matcher(location.toLowerCase());

        if (!checkCaptcha(location, m)) {
            if (matcher.find()) {
                parseFilm(d, m);
            } else {
                loadFilmPage(d, m);
            }
        } else {
            nextStep();
        }
    }

    private void loadFilmPage(Document d, MovieInfo m) throws ParseKinopoiskException {
        org.jsoup.nodes.Document jDoc = getJsoupDocument(d);

        Elements els = jDoc.getElementsByClass("element most_wanted");
        if (els == null || els.isEmpty()) {
            m.addNotFound(NotFound.MOVIE);
            loadNextMovie();
            return;
        }
        Element name = els.get(0).getElementsByClass("name").get(0);
        Element link = name.getElementsByTag("a").get(0);

        status = Status.FILM;
        String url = link.attr("data-url");
        if (!url.endsWith("/")) {
            url += "/";
        }
        waitAndLoadPage(HOST + url, 20000);
        LOG.info("Film");
    }

    private void parseFilm(Document d, MovieInfo m) {
        String location = d.getDocumentURI();

        if (checkCaptcha(location, m)) {
            nextStep();
            return;
        }

        String html = documentToString(d);
        org.jsoup.nodes.Document jDoc = getJsoupDocument(html);

        Elements els = jDoc.getElementsByAttributeValue("itemprop", "name");
        Element e = els.first();
        String name = e.getElementsByTag("span").first().text().trim();

        if (!m.getName().equalsIgnoreCase(name)) {
            m.addNotFound(NotFound.CANDIDATE);
        }

        m.setKinopoiskName(name);
        m.setLink(location);

        remoteMovieInfo.setLinkAndId(location);
        remoteMovieInfo.setName(name);
        remoteMovieInfo.setMainHtml(html);

        Elements originalNameEls = jDoc.getElementsByAttributeValueContaining("class", "styles_originalTitle");
        if (!originalNameEls.isEmpty()) {
            String originalName = originalNameEls.first().text();
            if (!originalName.isEmpty()) {
                m.setOriginalName(originalName.trim());
            }
        }

        Elements sections = jDoc.getElementsByClass("film-page-section-title");
        if (!sections.isEmpty() && sections.first().tag().getName().equalsIgnoreCase("h3")) {
            Elements infoBlock = sections.first().nextElementSibling().children();
            if (infoBlock.size() > 1) {
                Element countriesRow = infoBlock.get(1);
                Elements aCountries = countriesRow.getElementsByTag("a");
                for (Element a : aCountries) {
                    String c = a.text().toLowerCase();
                    if (c.contains("россия") || c.contains("ссср")) {
                        russian = true;
                        break;
                    }
                }
            }
        }

        Elements spoilers = jDoc.getElementsByAttributeValueContaining("class", "styles_itemsSpoiler");
        if (!spoilers.isEmpty()) {
            Elements menu = spoilers.first().getElementsByTag("a");
            Element aStudio = null;
            for (Element a : menu) {
                if (a.text().toLowerCase().contains("студии")) {
                    aStudio = a;
                    break;
                }
            }
            if (aStudio == null || aStudio.className().toLowerCase().contains("styles_itemdisbale")) {
                m.addNotFound(NotFound.STUDIO);
                loadCastPage(m);
            } else {
                loadStudioPage(m);
            }
        }
    }

    private void loadStudioPage(MovieInfo m) throws ParseKinopoiskException {
        status = Status.STUDIO;
        waitAndLoadPage(m.getLink().replace("series", "film") + STUDIO_URL_PART, 20000);
        LOG.info("Studio");
    }

    private void parseStudio(Document d, MovieInfo m) {
        String location = d.getDocumentURI();
        if (checkCaptcha(location, m)) {
            nextStep();
            return;
        }

        String html = documentToString(d);
        org.jsoup.nodes.Document jDoc = getJsoupDocument(html);

        remoteMovieInfo.setStudiosHtml(html);

        Element start = jDoc.getElementById("block_left");
        Element el = start.child(0);
        el = el.getElementsByTag("table").get(0);
        el = el.getElementsByTag("tr").get(0);
        el = el.getElementsByTag("table").get(0);
        el = el.getElementsByTag("tr").get(3);
        el = el.getElementsByTag("td").get(0);
        el = el.getElementsByTag("div").get(0);
        el = el.getElementsByTag("table").get(0);

        Elements trs = el.getElementsByTag("tr");
        String studio = "";
        boolean first = true;
        for (int i = 2; i < trs.size() - 1; i++) {
            Element a = trs.get(i).getElementsByTag("td").get(1).getElementsByTag("a").get(0);
            if (!first) {
                studio += ", ";
            } else {
                first = false;
            }
            studio += a.text();
        }
        m.setStudio(studio);

        loadCastPage(m);
    }

    private void loadCastPage(MovieInfo m) throws ParseKinopoiskException {
        status = Status.CAST;
        waitAndLoadPage(m.getLink().replace("series", "film") + CAST_URL_PART, 20000);
        LOG.info("Cast");
    }

    private void parseCast(Document d, MovieInfo m) {
        String location = d.getDocumentURI();
        if (checkCaptcha(location, m)) {
            nextStep();
            return;
        }

        String html = documentToString(d);
        org.jsoup.nodes.Document jDoc = getJsoupDocument(html);

        remoteMovieInfo.setCastHtml(html);

        Elements els = jDoc.getElementsByAttributeValue("name", "director");
        if (!els.isEmpty()) {
            Element aDirector = els.first();
            String director = "";
            boolean first = true;
            Element elDirector = aDirector.nextElementSibling().nextElementSibling();
            while (elDirector != null && elDirector.tagName().equalsIgnoreCase("div") && elDirector.hasClass("dub")) {
                Element actorInfo = elDirector.getElementsByClass("actorInfo").first();
                Element nameDiv = actorInfo.getElementsByClass("name").first();
                String name = nameDiv.getElementsByTag("a").first().text().trim();
                Elements spanSecondName = nameDiv.getElementsByTag("span");
                String secondName = null;
                if (!spanSecondName.isEmpty()) {
                    secondName = spanSecondName.first().text().trim();
                }
                if (first) {
                    first = false;
                } else {
                    director += ", ";
                }
                if (russian || secondName == null) {
                    director += name;
                } else {
                    director += secondName;
                }
                elDirector = elDirector.nextElementSibling();
            }
            m.setDirector(director);
        } else {
            m.addNotFound(NotFound.DIRECTOR);
        }

        els = jDoc.getElementsByAttributeValue("name", "composer");

        if (!els.isEmpty()) {
            Element aComposer = els.first();
            String composer = "";
            boolean first = true;
            Element elComposer = aComposer.nextElementSibling().nextElementSibling();
            while (elComposer != null && elComposer.tagName().equalsIgnoreCase("div") && elComposer.hasClass("dub")) {
                Element actorInfo = elComposer.getElementsByClass("actorInfo").first();
                Element nameDiv = actorInfo.getElementsByClass("name").first();
                String name = nameDiv.getElementsByTag("a").first().text().trim();
                Elements spanSecondName = nameDiv.getElementsByTag("span");
                String secondName = null;
                if (!spanSecondName.isEmpty()) {
                    secondName = spanSecondName.first().text().trim();
                }
                if (first) {
                    first = false;
                } else {
                    composer += ", ";
                }
                if (russian || secondName == null) {
                    composer += name;
                } else {
                    composer += secondName;
                }
                elComposer = elComposer.nextElementSibling();
            }
            m.setComposer(composer);
        } else {
            m.addNotFound(NotFound.COMPOSER);
        }

        loadNextMovie();
    }

    private void updateProgress() {
        Platform.runLater(() -> progress.set(getProgress() + 1));
    }

    private void setCompleted(boolean value) {
        Platform.runLater(() -> completed.set(value));
    }

    private org.jsoup.nodes.Document getJsoupDocument(Document d) throws ParseKinopoiskException {
        return getJsoupDocument(documentToString(d));
    }

    private org.jsoup.nodes.Document getJsoupDocument(String html) throws ParseKinopoiskException {
        return Jsoup.parse(html, ENCODING);
    }

    private String documentToString(Document doc) throws ParseKinopoiskException {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            LOG.warn("TransformerException: ", e);
            throw new ParseKinopoiskException("TransformerException");

        }
    }

    //https://www.kinopoisk.ru/index.php?level=7&from=forma&result=adv&m_act[from]=forma&m_act[what]=content&m_act[find]=��������&m_act[year]=2005
    //m_act[country]
    private String getUrl(String name, String year, String country) throws ParseKinopoiskException {
        try {
            StringBuilder sb = new StringBuilder(HOST)
                    .append("/index.php?level=7&from=forma&result=adv&")
                    .append(encode("m_act[from]"))
                    .append("=forma&")
                    .append(encode("m_act[what]"))
                    .append("=content&")
                    .append(encode("m_act[find]"))
                    .append("=").append(encode(name))
                    .append("&").append(encode("m_act[year]"))
                    .append("=").append(encode(year));
            Integer countryId = countries.getCountryId(country);
            if (countryId != null) {
                sb
                        .append("&").append(encode("m_act[country]"))
                        .append("=").append(countryId);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            LOG.warn("Unsupported encoding exception: ", e);
            throw new ParseKinopoiskException("Не поддерживаемая кодировка");
        }
    }

    private boolean checkCaptcha(String location, MovieInfo m) {
        if (location.contains(CAPTCHA)) {
            LOG.warn("CAPCHA " + location + "   " + m);
            m.addNotFound(NotFound.CAPTCHA);
            status = Status.NEXT;
            return true;
        }
        return false;
    }

    private String encode(String p) throws UnsupportedEncodingException {
        return URLEncoder.encode(p, ENCODING);

    }

    private static enum Status {
        NEXT, SEARCH, FILM, STUDIO, CAST;
    }

    private class WaitingTask extends TimerTask {

        private final KinopoiskParser kp;
        private final String url;

        public WaitingTask(KinopoiskParser kp, String url) {
            this.kp = kp;
            this.url = url;
        }

        @Override
        public void run() {
            LOG.info("Timer task is run. Url - {}", url);
            kp.loadPage(url);
        }
    }
}
