package xyz.pary.raoreportgenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MoviesInfoExcel {

    private static final Logger LOG = LogManager.getLogger(MoviesInfoExcel.class);

    /**
     *
     * @param file файл с результатами kinopoiska
     * @return список с результатами kinopoiska
     * @throws ExcelException в случае ошибки чтения результатов из excel
     */
    public static List<MovieInfo> parse(File file) throws ExcelException {
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
            LOG.error("Cannot read excel movies info file: ", e);
            throw new ExcelException(ExcelException.getIOMessage(fileName));
        }

        Sheet sheet = wb.getSheetAt(0);

        List<MovieInfo> moviesInfo = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            try {
                Row row = sheet.getRow(i);

                if (row.getCell(Column.NAME.getIndex(true)).getStringCellValue().isEmpty()) {
                    continue;
                }

                moviesInfo.add(new MovieInfo(
                        row.getCell(Column.NAME.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.ORIGINAL_NAME.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.GENRE.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.COUNTRY.getIndex(true)).getStringCellValue(),
                        getStringValue(row.getCell(Column.YEAR.getIndex(true))),
                        row.getCell(Column.DIRECTOR.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.COMPOSER.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.STUDIO.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.DURATION.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.DATETIME.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.NOT_FOUND.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.LINK.getIndex(true)).getStringCellValue(),
                        row.getCell(Column.KINOPOISK_NAME.getIndex(true)).getStringCellValue(),
                        getStringValue(row.getCell(Column.CAPTCHA_STEP.getIndex(true)))));
            } catch (Exception e) {
                LOG.warn("Movies info parse exception: ", e);
            }
        }
        return moviesInfo;
    }

    /**
     *
     * @param moviesInfo список фильмов в формате рао отчета
     * @param filePath путь к файлу
     * @throws ExcelException в случае ошибки сохранения результатов в excel
     */
    public static void save(List<MovieInfo> moviesInfo, String filePath) throws ExcelException {
        save(moviesInfo, filePath, false);
    }

    /**
     *
     * @param moviesInfo список фильмов
     * @param filePath путь к файлу
     * @param kinopoiskMovieInfo список фильмов в формате kinopoiska
     * @throws ExcelException в случае ошибки сохранения результатов в excel
     */
    public static void save(List<MovieInfo> moviesInfo, String filePath, boolean kinopoiskMovieInfo) throws ExcelException {
        if (moviesInfo.isEmpty()) {
            return;
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        sheet.setColumnWidth(Column.NAME.getIndex(kinopoiskMovieInfo), Column.NAME.getWidth());
        if (kinopoiskMovieInfo) {
            sheet.setColumnWidth(Column.ORIGINAL_NAME.getIndex(kinopoiskMovieInfo), Column.ORIGINAL_NAME.getWidth());
        }
        sheet.setColumnWidth(Column.DATETIME.getIndex(kinopoiskMovieInfo), Column.DATETIME.getWidth());
        sheet.setColumnWidth(Column.YEAR.getIndex(kinopoiskMovieInfo), Column.YEAR.getWidth());
        sheet.setColumnWidth(Column.GENRE.getIndex(kinopoiskMovieInfo), Column.GENRE.getWidth());
        sheet.setColumnWidth(Column.STUDIO.getIndex(kinopoiskMovieInfo), Column.STUDIO.getWidth());
        sheet.setColumnWidth(Column.COUNTRY.getIndex(kinopoiskMovieInfo), Column.COUNTRY.getWidth());
        sheet.setColumnWidth(Column.DIRECTOR.getIndex(kinopoiskMovieInfo), Column.DIRECTOR.getWidth());
        sheet.setColumnWidth(Column.COMPOSER.getIndex(kinopoiskMovieInfo), Column.COMPOSER.getWidth());
        sheet.setColumnWidth(Column.DURATION.getIndex(kinopoiskMovieInfo), Column.DURATION.getWidth());
        sheet.setColumnWidth(Column.NOT_FOUND.getIndex(kinopoiskMovieInfo), Column.NOT_FOUND.getWidth());
        sheet.setColumnWidth(Column.LINK.getIndex(kinopoiskMovieInfo), Column.LINK.getWidth());
        sheet.setColumnWidth(Column.KINOPOISK_NAME.getIndex(kinopoiskMovieInfo), Column.KINOPOISK_NAME.getWidth());
        if (!kinopoiskMovieInfo) {
            sheet.setColumnWidth(Column.STP_NAME.getIndex(kinopoiskMovieInfo), Column.STP_NAME.getWidth());
        }

        Row headersRow = sheet.createRow(0);
        headersRow.createCell(Column.NAME.getIndex(kinopoiskMovieInfo)).setCellValue(Column.NAME.getName());
        if (kinopoiskMovieInfo) {
            headersRow.createCell(Column.ORIGINAL_NAME.getIndex(kinopoiskMovieInfo)).setCellValue(Column.ORIGINAL_NAME.getName());
        }
        headersRow.createCell(Column.DATETIME.getIndex(kinopoiskMovieInfo)).setCellValue(Column.DATETIME.getName());
        headersRow.createCell(Column.YEAR.getIndex(kinopoiskMovieInfo)).setCellValue(Column.YEAR.getName());
        headersRow.createCell(Column.GENRE.getIndex(kinopoiskMovieInfo)).setCellValue(Column.GENRE.getName());
        headersRow.createCell(Column.STUDIO.getIndex(kinopoiskMovieInfo)).setCellValue(Column.STUDIO.getName());
        headersRow.createCell(Column.COUNTRY.getIndex(kinopoiskMovieInfo)).setCellValue(Column.COUNTRY.getName());
        headersRow.createCell(Column.DIRECTOR.getIndex(kinopoiskMovieInfo)).setCellValue(Column.DIRECTOR.getName());
        headersRow.createCell(Column.COMPOSER.getIndex(kinopoiskMovieInfo)).setCellValue(Column.COMPOSER.getName());
        headersRow.createCell(Column.DURATION.getIndex(kinopoiskMovieInfo)).setCellValue(Column.DURATION.getName());
        headersRow.createCell(Column.NOT_FOUND.getIndex(kinopoiskMovieInfo)).setCellValue(Column.NOT_FOUND.getName());
        headersRow.createCell(Column.LINK.getIndex(kinopoiskMovieInfo)).setCellValue(Column.LINK.getName());
        headersRow.createCell(Column.KINOPOISK_NAME.getIndex(kinopoiskMovieInfo)).setCellValue(Column.KINOPOISK_NAME.getName());
        if (!kinopoiskMovieInfo) {
            headersRow.createCell(Column.STP_NAME.getIndex(kinopoiskMovieInfo)).setCellValue(Column.STP_NAME.getName());
        }
        if (kinopoiskMovieInfo) {
            headersRow.createCell(Column.CAPTCHA_STEP.getIndex(kinopoiskMovieInfo)).setCellValue(Column.CAPTCHA_STEP.getName());
        }

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        CreationHelper createHelper = workbook.getCreationHelper();

        int rowCount = 1;
        Cell cell;
        for (MovieInfo m : moviesInfo) {
            Row row = sheet.createRow(rowCount);
            cell = row.createCell(Column.NAME.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getName());
            cell.setCellStyle(style);

            if (kinopoiskMovieInfo) {
                cell = row.createCell(Column.ORIGINAL_NAME.getIndex(kinopoiskMovieInfo));
                cell.setCellValue(m.getOriginalName());
                cell.setCellStyle(style);
            }

            cell = row.createCell(Column.DATETIME.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getReleaseDateTime());
            cell.setCellStyle(style);

            cell = row.createCell(Column.YEAR.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getYear());
            cell.setCellStyle(style);

            cell = row.createCell(Column.GENRE.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getGenre());
            cell.setCellStyle(style);

            cell = row.createCell(Column.STUDIO.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getStudio());
            cell.setCellStyle(style);

            cell = row.createCell(Column.COUNTRY.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getCountry());
            cell.setCellStyle(style);

            cell = row.createCell(Column.DIRECTOR.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getDirector());
            cell.setCellStyle(style);

            cell = row.createCell(Column.COMPOSER.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getComposer());
            cell.setCellStyle(style);

            cell = row.createCell(Column.DURATION.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getDuration());
            cell.setCellStyle(style);

            cell = row.createCell(Column.NOT_FOUND.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getNotFound());
            cell.setCellStyle(style);

            cell = row.createCell(Column.LINK.getIndex(kinopoiskMovieInfo));
            if (m.getLink() != null && !m.getLink().isEmpty()) {
                Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
                link.setAddress(m.getLink());
                cell.setCellValue(m.getLink());
                cell.setHyperlink(link);
            } else {
                cell.setCellValue("");
            }
            cell.setCellStyle(style);

            cell = row.createCell(Column.KINOPOISK_NAME.getIndex(kinopoiskMovieInfo));
            cell.setCellValue(m.getKinopoiskName());
            cell.setCellStyle(style);

            if (!kinopoiskMovieInfo) {
                cell = row.createCell(Column.STP_NAME.getIndex(kinopoiskMovieInfo));
                cell.setCellValue(m.getStpName());
                cell.setCellStyle(style);
            }
            
            if (kinopoiskMovieInfo) {
                cell = row.createCell(Column.CAPTCHA_STEP.getIndex(kinopoiskMovieInfo));
                cell.setCellValue(m.getCaptchaStep());
                cell.setCellStyle(style);
            }

            rowCount++;
        }

        File outFile = new File(filePath);
        try ( FileOutputStream out = new FileOutputStream(outFile)) {
            workbook.write(out);
        } catch (Exception e) {
            LOG.warn("Save movies info to Excel exception: ", e);
            String message;
            if (kinopoiskMovieInfo) {
                message = "Ошибка сохранения результатов кинопоиска в Excel";
            } else {
                message = "Ошибка сохранения объединенных результатов в Excel";
            }
            throw new ExcelException(message);
        }
    }

    private static String getStringValue(Cell c) {
        if (c == null) {
            return "";
        }
        return switch (c.getCellTypeEnum()) {
            case BLANK, STRING ->
                c.getStringCellValue();
            case BOOLEAN ->
                String.valueOf(c.getBooleanCellValue());
            case NUMERIC ->
                String.valueOf((int) c.getNumericCellValue());
            default ->
                "";
        };
    }

    private static enum Column {
        NAME(0, "Название", 17 * 256),
        ORIGINAL_NAME(1, "Оригинальное название", 17 * 256),
        DATETIME(2, "Дата/время выхода", 12 * 256),
        YEAR(3, "Год", 6 * 256),
        GENRE(4, "Жанр", 15 * 256),
        STUDIO(5, "Киностудия", 28 * 256),
        COUNTRY(6, "Страна", 8 * 256),
        DIRECTOR(7, "Режиссер", 14 * 256),
        COMPOSER(8, "Композитор", 14 * 256),
        DURATION(9, "Длительность", 12 * 256),
        NOT_FOUND(10, "Не найдено", 13 * 256),
        LINK(11, "Ссылка", 35 * 256),
        KINOPOISK_NAME(12, "Название на кинопоиске", 17 * 256),
        STP_NAME(13, "Название в СТП сетке", 17 * 256),
        CAPTCHA_STEP(14, "Шаг с капчой", 8 * 256);

        private final int index;
        private final String name;
        private final int width;

        Column(int columnIndex, String columnName, int columnWidth) {
            this.index = columnIndex;
            this.name = columnName;
            this.width = columnWidth;
        }

        public int getIndex(boolean kinopoiskMovieInfo) {
            if (!kinopoiskMovieInfo && index > 1) {
                return index - 1;
            }
            return index;
        }

        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }
    }
}
