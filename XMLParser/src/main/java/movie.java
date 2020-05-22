import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class movie {
    static String largestId;
    static HashMap<String, Integer> genremap = new HashMap<>();
    static int largestGenreId = -1;

    private String title, id, director;
    private int year;
    private ArrayList<String> genres = new ArrayList<>();
    public movie (String t, String d, int y) {
        updateId();
        id = largestId;
        title = t;
        director = d;
        year = y;
    }

    public movie (String t, String d, int y, String id) {
        this.id = id;
        title = t;
        director = d;
        year = y;
    }

    public movie() {
        updateId();
        id = largestId;
    }

    private void updateId() {
        String numId = String.valueOf(Integer.parseInt(largestId.substring(2)) + 1);
        int n = largestId.length() - 2 - numId.length();
        largestId = "tt" + (n > 0 ? (new String(new char[n]).replace("\0", "0")) : "") + numId;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + title.hashCode();
        result = 31 * result + year;
        result = 31 * result + director.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        movie movie = (movie) o;
        return year == movie.year &&
                Objects.equals(title, movie.title) &&
                Objects.equals(director, movie.director);
    }

    @Override
    public String toString() {
        return  id + ";;" +
                title + ";;" +
                year + ";;"+
                director + ";;";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getYear() {
        return year;
    }

    public void setYear(String year) {
        try {
            this.year = Integer.parseInt(year);
        } catch (NumberFormatException e) {
            this.year = -1;
        }
    }

    public void addGenre(String gen) {
        if (gen != null && !gen.isEmpty()) {
            genres.add(gen);
            if (!genremap.containsKey(gen)) {
                largestGenreId += 1;
                genremap.put(gen, largestGenreId);
            }
        }
    }

    public ArrayList<String> getGenres() { return genres; }


    static public void addGenreMap(String gen, int id) {
        genremap.put(gen, id);
        if (id > largestGenreId)
            largestGenreId = id;
    }

}
