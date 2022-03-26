package xyz.pary.raoreportgenerator;

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
    private String kinopoiskName;
    private String captchaStep;

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
            String studio, String duration, String releaseDateTime, String notFound, String link, String kinopoiskName, String captchaStep) {
        this(name, genre, country, year, director, composer, studio, duration, releaseDateTime, notFound, link, kinopoiskName);
        this.originalName = originalName;
        this.captchaStep = captchaStep;
    }

    public MovieInfo(String name, String genre, String country, String year, String director, String composer,
            String studio, String duration, String releaseDateTime, String notFound, String link, String kinopoiskName) {
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
        this.kinopoiskName = kinopoiskName;
    }

    public MovieInfo(String name, String releaseDateTime, String duration, String notFound) {
        this.name = name;
        this.releaseDateTime = releaseDateTime;
        this.duration = duration;
        this.notFound = notFound;
    }

    public MovieInfo copy() {
        return new MovieInfo(name, originalName, genre, country, year, director, composer,
                studio, duration, releaseDateTime, notFound, link, kinopoiskName, captchaStep);
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
        return link != null && link.isEmpty() ? null : link;
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

    public String getKinopoiskName() {
        return kinopoiskName;
    }

    public void setKinopoiskName(String kinopoiskName) {
        this.kinopoiskName = kinopoiskName;
    }

    public String getCaptchaStep() {
        return captchaStep != null && captchaStep.isEmpty() ? null : captchaStep;
    }

    public void setCaptchaStep(String captchaStep) {
        this.captchaStep = captchaStep;
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
                + ", stpName=" + stpName
                + ", kinopoiskName=" + kinopoiskName
                + ", captchaStep=" + captchaStep + '}';
    }

    @Override
    public int compareTo(MovieInfo o) {
        int r = getName().compareToIgnoreCase(o.getName());
        if (r == 0) {
            r = getYear().compareToIgnoreCase(o.getYear());
        }
        return r;
    }

    public int compareForCombiner(MovieInfo o) {
        Character c1 = Character.toUpperCase(getName().charAt(0));
        Character c2 = Character.toUpperCase(o.getName().charAt(0));
        int r = c1.compareTo(c2);
        if (r == 0) {
            int l1 = getName().length();
            int l2 = o.getName().length();
            if (l1 == l2) {
                return 0;
            } else if (l1 > l2) {
                return -1;
            } else {
                return 1;
            }
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
        CANDIDATE("Найден кандидат"),
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
