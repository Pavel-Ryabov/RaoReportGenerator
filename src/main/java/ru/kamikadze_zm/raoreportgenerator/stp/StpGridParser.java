package ru.kamikadze_zm.raoreportgenerator.stp;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.kamikadze_zm.raoreportgenerator.ExcelException;
import ru.kamikadze_zm.raoreportgenerator.MainApp;
import ru.kamikadze_zm.raoreportgenerator.MovieInfo;
import ru.kamikadze_zm.raoreportgenerator.settings.StpSettings;

public class StpGridParser {

    private static final Logger LOG = LogManager.getLogger(StpGridParser.class);

    private Set<MovieInfo> movies = new HashSet<>();

    /**
     *
     * @param file - СТП сетка .xlsx
     * @throws ExcelException при неверном расширении или при ошибках чтения файла
     */
    public StpGridParser(File file) throws ExcelException {
        String fileName = file.getName();
        int index = fileName.lastIndexOf(".");
        String ext = "";
        if (index > 0) {
            ext = fileName.substring(index);
        }

        String excelExt = MainApp.SETTINGS.getExcelExt();
        if (!ext.equalsIgnoreCase(excelExt)) {
            throw new ExcelException(ExcelException.getExtMessage(fileName, excelExt));
        }

        Workbook wb;
        try {
            wb = new XSSFWorkbook(file);
        } catch (IOException | InvalidFormatException e) {
            LOG.error("Cannot read stp grid: ", e);
            throw new ExcelException(ExcelException.getIOMessage(fileName));
        }

        StpSettings stpSettings = MainApp.SETTINGS.getStpSettings();

        Sheet sheet = wb.getSheetAt(stpSettings.getSheetIndex());

        for (int i = stpSettings.getStartRowIndex(); i <= sheet.getLastRowNum(); i++) {
            try {
                Row row = sheet.getRow(i);

                if (getStringFromCell(row.getCell(1)).isEmpty()) {
                    continue;
                }
                String name = getStringFromCell(row.getCell(stpSettings.getMovieNameColumnIndex()));
                String genre = getStringFromCell(row.getCell(stpSettings.getGenreColumnIndex()));
                String country = getStringFromCell(row.getCell(stpSettings.getCountryColumnIndex()));
                String year = getStringFromCell(row.getCell(stpSettings.getYearColumnIndex()));

                String director = null;
                if (stpSettings.getDirectorColumnIndex() != -1) {
                    director = getStringFromCell(row.getCell(stpSettings.getDirectorColumnIndex()));
                }
                String composer = null;
                if (stpSettings.getComposerColumnIndex() != -1) {
                    composer = getStringFromCell(row.getCell(stpSettings.getComposerColumnIndex()));
                }
                String duration = null;
                if (stpSettings.getDurationColumnIndex() != -1) {
                    duration = getStringFromCell(row.getCell(stpSettings.getDurationColumnIndex()));
                }

                MovieInfo movie = new MovieInfo(name, genre, country, year, director, composer, duration);
                LOG.info("Parsed stp movie: " + movie);
                movies.add(movie);
            } catch (Exception e) {
                LOG.warn("Parse stp grid exception, row number: " + i, e);
            }
        }
    }

    public Set<MovieInfo> getMovies() {
        return movies;
    }

    private String getStringFromCell(Cell c) {
        if (c == null) {
            return "";
        }
        CellType type = c.getCellTypeEnum();
        if (CellType.NUMERIC == type) {
            return String.valueOf(Double.valueOf(c.getNumericCellValue()).intValue());
        } else {
            return c.getStringCellValue().trim();
        }
    }
}
