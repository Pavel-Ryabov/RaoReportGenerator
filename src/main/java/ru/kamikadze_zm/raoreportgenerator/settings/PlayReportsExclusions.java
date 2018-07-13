package ru.kamikadze_zm.raoreportgenerator.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayReportsExclusions {

    private static final Logger LOG = LogManager.getLogger(PlayReportsExclusions.class);

    private static final String DEFAULT_FILE_PATH = "/default-playreports-exclusions.txt";

    /**
     * Построчно читает файл с исключениями
     *
     * @param filePath путь к файлу с исключениями (если {@code null} или файл не существует, читается файл по умолчанию)
     * @return список исключений (пустой список в случае ошибок)
     */
    public static List<String> read(String filePath) {
        if (filePath != null && new File(filePath).exists()) {
            try (InputStream is = new FileInputStream(filePath)) {
                return read(is);
            } catch (IOException e) {
                LOG.warn("Cannot read exclusions file: " + filePath, e);
            }
        }
        return read(PlayReportsExclusions.class.getResourceAsStream(DEFAULT_FILE_PATH));
    }

    private static List<String> read(InputStream in) {
        List<String> exclusions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (l.isEmpty()) {
                    continue;
                }
                exclusions.add(l.trim());
            }
        } catch (Exception e) {
            LOG.warn("Cannot read exclusions file", e);
        }
        return exclusions;
    }

    public static void write(List<String> exclusions, String filePath) {
        if (exclusions != null && !exclusions.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
                for (String e : exclusions) {
                    writer.write(e);
                }
            } catch (IOException e) {
                LOG.warn("Cannot write exclusions to file", e);
            }
        }
    }
}
