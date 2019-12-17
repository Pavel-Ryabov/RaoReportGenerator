package ru.kamikadze_zm.raoreportgenerator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Button22Movie implements Comparable<Button22Movie> {

    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private String name;
    private String director;
    private String actors;
    private String releaseDateTime;

    public Button22Movie(String name, String director, String actors) {
        this.name = name;
        this.director = director;
        this.actors = actors;
    }

    public Button22Movie(String name, String director, String actors, String releaseDateTime) {
        this.name = name;
        this.director = director;
        this.actors = actors;
        this.releaseDateTime = releaseDateTime;
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
