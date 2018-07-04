package ru.kamikadze_zm.raoreportgenerator.playreports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import ru.kamikadze_zm.onair.command.parameter.Duration;
import ru.kamikadze_zm.onair.command.parameter.MarkIn;
import ru.kamikadze_zm.raoreportgenerator.MainApp;

public class PlayReportsParser {

    private static final Logger LOG = LogManager.getLogger(PlayReportsParser.class);

    private static final String PLAYREPORT_EXT = "playreport";
    private static final String ROOT_CLOSER = "</root>";

    private final List<String> exclusions = MainApp.SETTINGS.getPlayReportsExclusions();

    private List<PlayReportMovie> movies;
    private Map<PlayReportMovie, PlayReportMovie> moviesMap;
    private List<String> errors;

    private DocumentBuilder documentBuilder;
    private XPathExpression itemExpression;
    private XPathExpression movieExpression;

    private final SimpleDateFormat movieDateFormat = new SimpleDateFormat("yyyy-MM-dd"); //date="2017-07-25"

    public PlayReportsParser(File playReportsDir) {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            XPathFactory xpf = XPathFactory.newInstance();
            itemExpression = xpf.newXPath().compile("//item[@type='Movie']");
            movieExpression = xpf.newXPath().compile("movie");
        } catch (ParserConfigurationException | XPathExpressionException e) {
            LOG.error("Cannot create play reports parser: ", e);
            movies = Collections.emptyList();
            return;
        }

        moviesMap = new HashMap<>();
        File[] files = playReportsDir.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                addMoviesToMapFromXml(f);
            }
        }

        List<PlayReportMovie> moviesList = new ArrayList<>(moviesMap.values());
        Collections.sort(moviesList);
        this.movies = moviesList;
        moviesMap = null;
    }

    public List<PlayReportMovie> getMovies() {
        return movies;
    }

    public List<String> getErrors() {
        if (errors == null) {
            return Collections.emptyList();
        }
        return errors;
    }

    private void addMoviesToMapFromXml(File file) {
        String fileName = file.getName();
        int extIndex = fileName.lastIndexOf(".");
        String ext = "";
        if (extIndex > 0) {
            ext = fileName.substring(extIndex + 1);
        }

        if (!ext.equalsIgnoreCase(PLAYREPORT_EXT)) {
            return;
        }

        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            accessFile.seek(accessFile.length() - 8);
            byte[] bytes = new byte[8];
            accessFile.readFully(bytes);
            String root = new String(bytes);
            if (!root.toLowerCase().contains(ROOT_CLOSER)) {
                accessFile.writeBytes(ROOT_CLOSER);
            }
        } catch (IOException e) {
            LOG.warn("Append root closer exception: ", e);
            addError(file);
            return;
        }

        try {

            Document document;
            try (Reader r = new InputStreamReader(new FileInputStream(file), Charset.forName("cp1251"))) {
                InputSource is = new InputSource(r);
                document = this.documentBuilder.parse(is);
            } catch (IOException e) {
                LOG.warn("Read xml file " + file.getAbsolutePath() + "exception: ", e);
                addError(file);
                return;
            }

            NodeList items = (NodeList) this.itemExpression.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                NamedNodeMap attrs = item.getAttributes();

                Node movieFileAttr = attrs.getNamedItem("file");
                if (movieFileAttr == null) {
                    continue;
                }
                String movieFile = movieFileAttr.getNodeValue();

                Node dateAttr = attrs.getNamedItem("date");
                if (dateAttr == null) {
                    continue;
                }
                Date date;
                try {
                    date = this.movieDateFormat.parse(dateAttr.getNodeValue());
                } catch (ParseException parseException) {
                    LOG.error("Movie date parse exception: ", parseException);
                    continue;
                }

                Node timeAttr = attrs.getNamedItem("time");
                if (timeAttr == null) {
                    continue;
                }
                Duration time = new Duration(timeAttr.getNodeValue());

                Node markInAttr = attrs.getNamedItem("markIn");
                MarkIn markIn = null;
                if (markInAttr != null) {
                    markIn = new MarkIn(markInAttr.getNodeValue());
                }

                if ((markIn == null || markIn.getDuration() == 0) && !isExclusion(movieFile)) {
                    Node movieNode = (Node) this.movieExpression.evaluate(item, XPathConstants.NODE);
                    if (movieNode == null) {
                        continue;
                    }
                    NamedNodeMap movieAttrs = movieNode.getAttributes();
                    Node movieFileDurationAttr = movieAttrs.getNamedItem("file_duration");
                    Duration movieFileDuration;
                    if (movieFileDurationAttr != null) {
                        movieFileDuration = new Duration(movieFileDurationAttr.getNodeValue());
                    } else {
                        movieFileDuration = new Duration();
                    }

                    PlayReportMovie prm = new PlayReportMovie(movieFile, movieFileDuration, date, time);
                    if (!moviesMap.containsKey(prm)) {
                        moviesMap.put(prm, prm);
                    } else {
                        PlayReportMovie cm = moviesMap.get(prm);
                        cm.addDateTime(prm.getDateTime());
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("Parse xml file " + file.getAbsolutePath() + " exception: ", e);
            addError(file);
        }
    }

    private boolean isExclusion(String verifiablePath) {
        if (!exclusions.isEmpty()) {
            for (String exc : exclusions) {
                exc = exc.toLowerCase();
                verifiablePath = verifiablePath.toLowerCase();
                String[] parts = exc.split("\\|");
                boolean isExclusion = verifiablePath.contains(parts[0].trim());
                if (parts.length > 1) {
                    for (int i = 1; i < parts.length; i++) {
                        isExclusion = isExclusion && !verifiablePath.contains(parts[i].trim());
                    }
                }
                if (isExclusion) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addError(File f) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add("Ошибка при обработке файла: " + f.getAbsolutePath());
    }
}
