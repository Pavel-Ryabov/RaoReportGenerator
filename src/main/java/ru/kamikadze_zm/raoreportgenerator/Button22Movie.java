package ru.kamikadze_zm.raoreportgenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import xyz.pary.onair.command.parameter.Duration;

public class Button22Movie implements Comparable<Button22Movie> {

    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private String name;
    private String director;
    private String actors;
    private String releaseDateTime;
    private Duration duration;

    public Button22Movie(String name, String director, String actors, Duration duration) {
        this.name = name;
        this.director = director;
        this.actors = actors;
        this.duration = duration;
    }

    public Button22Movie(String name, String director, String actors, String releaseDateTime, Duration duration) {
        this.name = name;
        this.director = director;
        this.actors = actors;
        this.releaseDateTime = releaseDateTime;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getReleaseDateTime() {
        return releaseDateTime;
    }

    public void setReleaseDateTime(String releaseDateTime) {
        this.releaseDateTime = releaseDateTime;
    }

    public Duration getDuration() {
        return duration;
    }
    
    public String getStringDuration() {
        return duration != null ? duration.toString() : "";
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public int compareTo(Button22Movie o) {
        if (this.releaseDateTime == null && o.releaseDateTime == null) {
            return 0;
        } else if (this.releaseDateTime == null) {
            return -o.releaseDateTime.compareTo(this.releaseDateTime);
        }
        try {
            Date d1 = DATE_FORMATTER.parse(this.releaseDateTime);
            Date d2 = DATE_FORMATTER.parse(o.releaseDateTime);
            return d1.compareTo(d2);
        } catch (ParseException e) {
            return this.releaseDateTime.compareTo(o.releaseDateTime);
        }
    }

    public int compareForCombiner(Button22Movie o) {
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
        final Button22Movie other = (Button22Movie) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
