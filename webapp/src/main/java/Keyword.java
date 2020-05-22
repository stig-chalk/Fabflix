import java.util.ArrayList;

public class Keyword {
    private String title, actor, director, orderBy, limit, keyType, browseBy, browseKey, ascOptR, ascOptT, offset;
    private int year;
    public Keyword(String t, String a, String d, String y, String o, String l, String ascR, String ascT, String off) {
        keyType = "search";
        browseKey = browseBy = "";
        title = t;
        actor = a;
        director = d;
        orderBy = o;
        limit = l;
        ascOptT = ascT;
        ascOptR = ascR;
        offset = off;
        try {
            year = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            year = -1;
        }
    }

    public Keyword(String browseBy, String browseKey, String o, String l, String ascR, String ascT, String off) {
        keyType = "browse";
        this.browseBy = browseBy;
        this.browseKey = browseKey;
        limit = l;
        orderBy = o;
        ascOptR = ascR;
        ascOptT = ascT;
        offset = off;
        title = actor = director = "";
    }

    public Keyword(){};

    public String getTitle() { return title; }
    public void setTitle(String t) { title = t; }

    public String getActor() {
        return actor;
    }
    public void setActor(String a) { actor = a; }

    public String getDirector() {
        return director;
    }
    public void setDirector(String d) { director = d; }

    public int getYear() {
        return year;
    }
    public void setYear(int y) { year = y; }

    @Override
    public String toString() {
        return "Keyword{" +
                "title='" + title + '\'' +
                ", actor='" + actor + '\'' +
                ", director='" + director + '\'' +
                ", keyType='" + keyType + '\'' +
                ", browseBy='" + browseBy + '\'' +
                ", browseKey='" + browseKey + '\'' +
                ", year=" + year +
                '}';
    }

    public boolean isKwEmpty() {
        return (title.isEmpty() && director.isEmpty() && actor.isEmpty() && year < 0
                && browseBy.isEmpty() && browseKey.isEmpty());
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getkeyType() {
        return keyType;
    }

    public void setkeyType(String type) {
        this.keyType = type;
    }

    public String getBrowseBy() {
        return browseBy;
    }

    public void setBrowseBy(String browseBy) {
        this.browseBy = browseBy;
    }

    public String getBrowseKey() {
        return browseKey;
    }

    public void setBrowseKey(String browseKey) {
        this.browseKey = browseKey;
    }

    public String getAscOptR() {
        return ascOptR;
    }

    public void setAscOptR(String ascOpt) {
        this.ascOptR = ascOpt;
    }

    public String getAscOptT() {
        return ascOptT;
    }

    public void setAscOptT(String ascOpt) {
        this.ascOptT = ascOpt;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
}
