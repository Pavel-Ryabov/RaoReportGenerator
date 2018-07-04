package ru.kamikadze_zm.raoreportgenerator;

public class ExcelException extends Exception {

    public ExcelException(String message) {
        super(message);
    }

    public static String getExtMessage(String filename, String ext) {
        return "Неверное расширение файла: " + filename + ". Требуется *" + ext;
    }

    public static String getIOMessage(String filename) {
        return "Не удалось прочитать excel файл: " + filename;
    }
}
