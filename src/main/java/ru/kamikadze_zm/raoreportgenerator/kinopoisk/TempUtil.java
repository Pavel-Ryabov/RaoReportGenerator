package ru.kamikadze_zm.raoreportgenerator.kinopoisk;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kamikadze_zm.raoreportgenerator.MainApp;
import ru.kamikadze_zm.raoreportgenerator.MovieInfo;

public class TempUtil {

    private static final Logger LOG = LogManager.getLogger(TempUtil.class);

    private final static String FILE_PATH = MainApp.SETTINGS.getOutputDir() + "temp.txt";

    public static void save(MovieInfo movieInfo) {
        Gson gson = new Gson();
        String json = gson.toJson(movieInfo);
        File file = new File(FILE_PATH);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            LOG.warn("Cannot write kinopoisk result to temp file", e);
        }
    }

    public static List<MovieInfo> restore() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return Collections.emptyList();
        }
        List<String> lines;
        try {
            lines = getLinesFromFile(file);
        } catch (IOException e) {
            LOG.warn("Cannot read kinopoisk results from temp file", e);
            return Collections.emptyList();
        }

        List<MovieInfo> movies = new ArrayList<>();
        Gson gson = new Gson();
        lines.stream().forEach(l -> movies.add(gson.fromJson(l, MovieInfo.class)));
        deleteTemp();
        return movies;
    }

    public static void deleteTemp() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    public static boolean checkTemp() {
        return new File(FILE_PATH).exists();
    }

    private static List<String> getLinesFromFile(File file) throws IOException {
        return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream()
                .filter(s -> !s.isEmpty())
                .collect(toList());
    }
}
