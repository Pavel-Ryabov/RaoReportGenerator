package xyz.pary.raoreportgenerator.settings;

import java.io.Serializable;

public class StpSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    //настройки стп сетки
    private static final int DEFAULT_SHEET_INDEX = 2;
    private static final int DEFAULT_START_ROW_INDEX = 1;
    private static final int DEFAULT_MOVIE_NAME_COLUMN_INDEX = 1;
    private static final int DEFAULT_GENRE_COLUMN_INDEX = 2;
    private static final int DEFAULT_COUNTRY_COLUMN_INDEX = 3;
    private static final int DEFAULT_YEAR_COLUMN_INDEX = 4;
    private static final int DEFAULT_DIRECTOR_COLUMN_INDEX = 6;
    private static final int DEFAULT_COMPOSER_COLUMN_INDEX = 7;
    private static final int DEFAULT_DURATION_COLUMN_INDEX = 5;

    private int sheetIndex = DEFAULT_SHEET_INDEX;
    private int startRowIndex = DEFAULT_START_ROW_INDEX;
    private int movieNameColumnIndex = DEFAULT_MOVIE_NAME_COLUMN_INDEX;
    private int genreColumnIndex = DEFAULT_GENRE_COLUMN_INDEX;
    private int countryColumnIndex = DEFAULT_COUNTRY_COLUMN_INDEX;
    private int yearColumnIndex = DEFAULT_YEAR_COLUMN_INDEX;
    private int directorColumnIndex = DEFAULT_DIRECTOR_COLUMN_INDEX;
    private int composerColumnIndex = DEFAULT_COMPOSER_COLUMN_INDEX;
    private int durationColumnIndex = DEFAULT_DURATION_COLUMN_INDEX;

    public StpSettings() {
    }

    public StpSettings(int sheetIndex,
            int startRowIndex,
            int movieNameColumnIndex,
            int genreColumnIndex,
            int countryColumnIndex,
            int yearColumnIndex,
            int directorColumnIndex,
            int composerColumnIndex,
            int durationColumnIndex) {
        this.sheetIndex = sheetIndex;
        this.startRowIndex = startRowIndex;
        this.movieNameColumnIndex = movieNameColumnIndex;
        this.genreColumnIndex = genreColumnIndex;
        this.countryColumnIndex = countryColumnIndex;
        this.yearColumnIndex = yearColumnIndex;
        this.directorColumnIndex = directorColumnIndex;
        this.composerColumnIndex = composerColumnIndex;
        this.durationColumnIndex = durationColumnIndex;
    }

    public int getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public int getStartRowIndex() {
        return startRowIndex;
    }

    public void setStartRowIndex(int startRowIndex) {
        this.startRowIndex = startRowIndex;
    }

    public int getMovieNameColumnIndex() {
        return movieNameColumnIndex;
    }

    public void setMovieNameColumnIndex(int movieNameColumnIndex) {
        this.movieNameColumnIndex = movieNameColumnIndex;
    }

    public int getGenreColumnIndex() {
        return genreColumnIndex;
    }

    public void setGenreColumnIndex(int genreColumnIndex) {
        this.genreColumnIndex = genreColumnIndex;
    }

    public int getCountryColumnIndex() {
        return countryColumnIndex;
    }

    public void setCountryColumnIndex(int countryColumnIndex) {
        this.countryColumnIndex = countryColumnIndex;
    }

    public int getYearColumnIndex() {
        return yearColumnIndex;
    }

    public void setYearColumnIndex(int yearColumnIndex) {
        this.yearColumnIndex = yearColumnIndex;
    }

    public int getDirectorColumnIndex() {
        return directorColumnIndex;
    }

    public void setDirectorColumnIndex(int directorColumnIndex) {
        this.directorColumnIndex = directorColumnIndex;
    }

    public int getComposerColumnIndex() {
        return composerColumnIndex;
    }

    public void setComposerColumnIndex(int composerColumnIndex) {
        this.composerColumnIndex = composerColumnIndex;
    }

    public int getDurationColumnIndex() {
        return durationColumnIndex;
    }

    public void setDurationColumnIndex(int durationColumnIndex) {
        this.durationColumnIndex = durationColumnIndex;
    }
}
