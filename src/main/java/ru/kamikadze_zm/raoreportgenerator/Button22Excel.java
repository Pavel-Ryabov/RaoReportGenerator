package ru.kamikadze_zm.raoreportgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.kamikadze_zm.raoreportgenerator.playreports.PlayReportMovie;

public class Button22Excel {

    private static final Logger LOG = LogManager.getLogger(Button22Excel.class);

    public static List<Button22Movie> parseSTP(File file) throws ExcelException {
        Workbook wb;
        try {
            wb = new XSSFWorkbook(file);
        } catch (IOException | InvalidFormatException e) {
            LOG.error("Cannot read excel stp file: ", e);
            throw new ExcelException(ExcelException.getIOMessage(file.getAbsolutePath()));
        }

        Sheet sheet = wb.getSheetAt(2);

        List<Button22Movie> button22Movies = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            try {
                Row row = sheet.getRow(i);

                if (row.getCell(0).getStringCellValue().isEmpty()) {
                    continue;
                }

                button22Movies.add(new Button22Movie(
                        row.getCell(0).getStringCellValue(),
                        row.getCell(5).getStringCellValue(),
                        row.getCell(6).getStringCellValue()));
            } catch (Exception e) {
                LOG.warn("Stp parse exception: ", e);
            }
        }
        return button22Movies;
    }

    public static void save(List<Button22Movie> button22Movies, String filePath) throws ExcelException {
        if (button22Movies.isEmpty()) {
            return;
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        int colWidth = 35 * 256;
        sheet.setColumnWidth(0, colWidth);
        sheet.setColumnWidth(1, colWidth);
        sheet.setColumnWidth(2, colWidth);
        sheet.setColumnWidth(3, colWidth);
        sheet.setColumnWidth(4, colWidth);

        Row headersRow = sheet.createRow(0);
        headersRow.createCell(0).setCellValue("Название передачи");
        headersRow.createCell(1).setCellValue("Дата и время выхода в эфир");
        headersRow.createCell(2).setCellValue("Режиссёр");
        headersRow.createCell(3).setCellValue("Ведущие");
        headersRow.createCell(4).setCellValue("Актёры");

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        int rowNumber = 1;
        Cell cell;
        for (Button22Movie m : button22Movies) {
            Row row = sheet.createRow(rowNumber);
            cell = row.createCell(0);
            cell.setCellValue(m.getName());
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(m.getReleaseDateTime());
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue(m.getDirector());
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue(m.getActors());
            cell.setCellStyle(style);

            rowNumber++;
        }

        File outFile = new File(filePath);
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            workbook.write(out);
        } catch (Exception e) {
            LOG.warn("Save button22 movies to Excel exception: ", e);
            throw new ExcelException("Ошибка сохранения результатов 22 кнопки в Excel");
        }
    }

    public static void combine(List<Button22Movie> button22Movies, List<PlayReportMovie> playReportMovies, String path) throws ExcelException {
        if (button22Movies.isEmpty() || playReportMovies.isEmpty()) {
            return;
        }
        List<Button22Movie> sortedMovies = new ArrayList<>(button22Movies);
        Collections.sort(sortedMovies, Button22Movie::compareForCombiner);
        List<Button22Movie> combined = new ArrayList<>();

        String replaceRegEx = "[ _\\-.]";
        Button22Movie founded;
        for (PlayReportMovie prm : playReportMovies) {
            String reportMovieName = prm.getMovieName().replaceAll(replaceRegEx, "").toLowerCase();
            String stpFilmName;
            founded = null;
            for (Button22Movie m : sortedMovies) {
                stpFilmName = m.getName().replaceAll(replaceRegEx, "").toLowerCase();
                if (reportMovieName.startsWith(stpFilmName)) {
                    founded = m;
                    break;
                }
            }
            String[] releases = prm.getDateTime().split(",");
            for (String date : releases) {
                String trimmedDate = date.trim();
                if (founded != null) {
                    String name = prm.getMovieName();
                    combined.add(new Button22Movie(name, founded.getDirector(), founded.getActors(), trimmedDate));
                } else {
                    combined.add(new Button22Movie(prm.getMovieName(), "", "", trimmedDate));
                }
            }
        }
        Collections.sort(combined);
        save(combined, path);
    }
}
