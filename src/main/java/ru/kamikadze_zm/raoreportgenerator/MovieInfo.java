package ru.kamikadze_zm.raoreportgenerator;

import java.util.Objects;

public class MovieInfo implements Comparable<MovieInfo> {

    private String name;
    private String originalName;
    private String genre;
    private String country;
    private String year;
    private String director;
    private String composer;
    private String studio;
    private String duration;
    private String releaseDateTime;
    private String notFound;
    private String link;
    private String stpName;

    public MovieInfo(String name, String genre, String country, String year, String director, String composer, String duration) {
        this.name = name;
        this.genre = genre;
        this.country = country;
        this.year = year;
        this.director = director;
        this.composer = composer;
        this.duration = duration;
    }

    public MovieInfo(String name, String originalName, String genre, String country, String year, String director, String composer,
            String studio, String duration, String releaseDateTime, String notFound, String link) {
        this(name, genre, country, year, director, composer, studio, duration, releaseDateTime, notFound, link);
        this.originalName = originalName;
    }

    public MovieInfo(String name, String genre, String country, String year, String director, String composer,
            String studio, String duration, String releaseDateTime, String notFound, String link) {
        this.name = name;
        this.genre = genre;
        this.country = country;
        this.year = year;
        this.director = director;
        this.composer = composer;
        this.studio = studio;
        this.duration = duration;
        this.releaseDateTime = releaseDateTime;
        this.notFound = notFound;
        this.link = link;
    }

    public MovieInfo(String name, String releaseDateTime, String duration, String notFound) {
        this.name = name;
        this.releaseDateTime = releaseDateTime;
        this.duration = duration;
        this.notFound = notFound;
    }

    public MovieInfo copy() {
        return new MovieInfo(name, originalName, genre, country, year, director, composer,
                studio, duration, releaseDateTime, notFound, link);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getStudio() {
        return studio;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getReleaseDateTime() {
        return releaseDateTime;
    }

    public void setReleaseDateTime(String releaseDateTime) {
        this.releaseDateTime = releaseDateTime;
    }

    public String getNotFound() {
        return notFound;
    }

    public void setNotFound(String notFound) {
        this.notFound = notFound;
    }

    public void addNotFound(NotFound notFound) {
        if (this.notFound != null) {
            this.notFound += ", " + notFound.getMessage();
        } else {
            this.notFound = notFound.getMessage();
        }
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStpName() {
        return stpName;
    }

    public void setStpName(String stpName) {
        this.stpName = stpName;
    }

    @Override
    public String toString() {
        return "MovieInfo{"
                + "name=" + name
                + ", originalName=" + originalName
                + ", genre=" + genre
                + ", country=" + country
                + ", year=" + year
                + ", director=" + director
                + ", composer=" + composer
                + ", studio=" + studio
                + ", duration=" + duration
                + ", releaseDateTime=" + releaseDateTime
                + ", notFound=" + notFound
                + ", link=" + link
                + ", stpName=" + stpName + '}';
    }

    @Override
    public int compareTo(MovieInfo o) {
        int r = getName().compareToIgnoreCase(o.getName());
        if (r == 0) {
            r = getYear().compareToIgnoreCase(o.getYear());
        }
        return r;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.country);
        hash = 17 * hash + Objects.hashCode(this.year);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MovieInfo other = (MovieInfo) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.country, other.country)) {
            return false;
        }
        if (!Objects.equals(this.year, other.year)) {
            return false;
        }
        return true;
    }

    public static enum NotFound {

        MOVIE("Не найден фильм"),
        DIRECTOR("Не найден режиссер"),
        COMPOSER("Не найден композитор"),
        STUDIO("Не найдена киностудия"),
        RELEASE_DATE_TIME("Не найдены дата/время выхода"),
        CAPTCHA("Капча"),
        ERROR("Ошибка");

        private final String message;

        private NotFound(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
