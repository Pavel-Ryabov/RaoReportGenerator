package ru.kamikadze_zm.raoreportgenerator.playreports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.kamikadze_zm.onair.command.parameter.Duration;
import ru.kamikadze_zm.raoreportgenerator.ExcelException;
import ru.kamikadze_zm.raoreportgenerator.MainApp;

public class ExcelPlayReports {

    private static final Logger LOG = LogManager.getLogger(ExcelPlayReports.class);

    private static final int MOVIE_NAME_COLUMN_INDEX = 0;
    private static final int DATETIME_COLUMN_INDEX = 1;
    private static final int DURATION_COLUMN_INDEX = 2;

    public static List<PlayReportMovie> parse(File file) throws ExcelException {
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
            LOG.error("Cannot read excel play reports file: ", e);
            throw new ExcelException(ExcelException.getIOMessage(fileName));
        }

        Sheet sheet = wb.getSheetAt(0);

        List<PlayReportMovie> movies = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            try {
                Row row = sheet.getRow(i);

                if (row.getCell(0).getStringCellValue().isEmpty()) {
                    continue;
                }

                String duration = row.getCell(DURATION_COLUMN_INDEX).getStringCellValue();
                if (duration.length() > 1) {
                    duration += ".00";
                }
                movies.add(new PlayReportMovie(row.getCell(MOVIE_NAME_COLUMN_INDEX).getStringCellValue(),
                        new Duration(duration),
                        row.getCell(DATETIME_COLUMN_INDEX).getStringCellValue()));
            } catch (Exception e) {
                LOG.warn("Excel play report parse exception: ", e);
            }
        }
        return movies;
    }

    public static void save(List<PlayReportMovie> movies) throws ExcelException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        Row rowHeaders = sheet.createRow(0);
        rowHeaders.createCell(MOVIE_NAME_COLUMN_INDEX).setCellValue("Название");
        rowHeaders.createCell(DATETIME_COLUMN_INDEX).setCellValue("Дата/время выхода");
        rowHeaders.createCell(DURATION_COLUMN_INDEX).setCellValue("Длительность");

        int rowCount = 1;

        for (PlayReportMovie m : movies) {
            try {
                Row row = sheet.createRow(rowCount);
                row.createCell(MOVIE_NAME_COLUMN_INDEX).setCellValue(m.getMovieName());
                row.createCell(DATETIME_COLUMN_INDEX).setCellValue(m.getDateTime());
                String dur = m.getDuration().toString();
                if (dur.lastIndexOf(".") != -1) {
                    dur = dur.substring(0, dur.lastIndexOf("."));
                }
                row.createCell(DURATION_COLUMN_INDEX).setCellValue(dur);
                rowCount++;
            } catch (Exception e) {
                LOG.warn("Cannot write " + m.toString() + " to excel:", e);
                throw new ExcelException("Произошла критическая ошибка");
            }
        }

        sheet.autoSizeColumn(MOVIE_NAME_COLUMN_INDEX);
        sheet.autoSizeColumn(DATETIME_COLUMN_INDEX);
        sheet.autoSizeColumn(DURATION_COLUMN_INDEX);

        File outFile = new File(MainApp.SETTINGS.getPlayReportsPath());
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            workbook.write(out);
        } catch (Exception e) {
            LOG.error("Save play reports to Excel exception: ", e);
            throw new ExcelException("Ошибка сохранения плэй репортов в Excel");
        }
    }
}
