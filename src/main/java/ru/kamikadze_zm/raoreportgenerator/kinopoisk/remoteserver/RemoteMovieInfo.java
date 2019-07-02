package ru.kamikadze_zm.raoreportgenerator.kinopoisk.remoteserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemoteMovieInfo {

    private static final Pattern SIMPLE_ID_PATTERN = Pattern.compile("/\\d+/");
    private static final Pattern DIFFICULT_ID_PATTERN = Pattern.compile("/.+-\\d+/");

    private Integer id;
    private String name;
    private String stpName;
    private String link;
    private String mainHtml;
    private String castHtml;
    private String studiosHtml;

    public RemoteMovieInfo(String stpName) {
        this.stpName = stpName;
    }

    public RemoteMovieInfo(Integer id, String name, String stpName, String link, String mainHtml, String castHtml, String studiosHtml) {
        this.id = getIdFromLink(link);
        this.name = name;
        this.stpName = stpName;
        this.link = link;
        this.mainHtml = mainHtml;
        this.castHtml = castHtml;
        this.studiosHtml = studiosHtml;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStpName() {
        return stpName;
    }

    public void setStpName(String stpName) {
        this.stpName = stpName;
    }

    public String getLink() {
        return link;
    }

    public void setLinkAndId(String link) {
        this.link = link;
        this.id = getIdFromLink(link);
    }

    public String getMainHtml() {
        return mainHtml;
    }

    public void setMainHtml(String mainHtml) {
        this.mainHtml = mainHtml;
    }

    public String getCastHtml() {
        return castHtml;
    }

    public void setCastHtml(String castHtml) {
        this.castHtml = castHtml;
    }

    public String getStudiosHtml() {
        return studiosHtml;
    }

    public void setStudiosHtml(String studiosHtml) {
        this.studiosHtml = studiosHtml;
    }

    @Override
    public String toString() {
        return "RemoteMovieInfo{" + "id=" + id + ", name=" + name + ", stpName=" + stpName + ", link=" + link + '}';
    }

    private Integer getIdFromLink(String link) {
        Matcher matcher = SIMPLE_ID_PATTERN.matcher(link);
        try {
            if (matcher.find()) {
                int s = matcher.start() + 1;
                int e = matcher.end() - 1;
                return Integer.parseInt(link.substring(s, e));
            }
            matcher = DIFFICULT_ID_PATTERN.matcher(link);
            if (matcher.find()) {
                int sp = matcher.start() + 1;
                int ep = matcher.end() - 1;
                String idPart = link.substring(sp, ep);
                int startId = idPart.lastIndexOf("-") + 1;
                return Integer.parseInt(link.substring(startId, ep));
            }
        } catch (NumberFormatException e) {
        }
        return null;
    }
}
