package ru.kamikadze_zm.raoreportgenerator.playreports;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.kamikadze_zm.onair.command.parameter.Duration;

public class PlayReportMovie implements Comparable<PlayReportMovie> {

    private static final Logger LOG = LogManager.getLogger(PlayReportMovie.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private String file;
    private Duration duration;
    private String dateTime;

    public PlayReportMovie() {
    }

    public PlayReportMovie(String file, Duration duration, String dateTime) {
        this.file = file;
        this.duration = duration;
        this.dateTime = dateTime;
    }

    public PlayReportMovie(String file, Duration duration, Date date, Duration time) {
        this(file, duration, formatDateTime(date, time));
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getMovieName() {
        int start = file.lastIndexOf("\\");
        int end = file.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return file.substring(start + 1, end);
        } else if (start != -1) {
            return file.substring(start + 1);
        } else {
            return file;
        }
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setDateTime(Date date, Duration time) {
        this.dateTime = formatDateTime(date, time);
    }

    public void addDateTime(Date date, Duration time) {
        addDateTime(formatDateTime(date, time));
    }

    public void addDateTime(String dateTime) {
        if (this.dateTime == null || this.dateTime.isEmpty()) {
            this.dateTime = dateTime;
        } else {
            this.dateTime += ", " + dateTime;
        }
    }

    @Override
    public int compareTo(PlayReportMovie o) {
        return getMovieName().compareToIgnoreCase(o.getMovieName());
    }

    @Override
    public String toString() {
        return "PlayReportMovie{" + "file=" + file + ", duration=" + duration + ", dateTime=" + dateTime + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
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
        final PlayReportMovie other = (PlayReportMovie) obj;
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (!Objects.equals(this.duration, other.duration)) {
            return false;
        }
        return true;
    }

    private static String formatDateTime(Date date, Duration time) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Input date = {}, input time = {}, output datetime = {}", date, time, DATE_FORMAT.format(date.getTime() + time.getDuration()));
        }
        return DATE_FORMAT.format(date.getTime() + time.getDuration());
    }
}
