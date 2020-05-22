import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class cast {
    static HashMap<String, String> movieMap = new HashMap<>();
    static HashMap<String, String> starMap = new HashMap<>();
    private String movieId, starId;

    public cast(String movieId, String starId) {
        this.movieId = movieId;
        this.starId = starId;
    }

    public cast(){}

    public String getMovieId() {
        return movieId;
    }

    public String getStarId() {
        return starId;
    }

    public void setMovieId(String movieName) {
        movieId = movieMap.get(movieName);
    }

    public void setStarId(String starName) {
        starId = starMap.get(starName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        cast cast = (cast) o;
        return Objects.equals(movieId, cast.movieId) &&
                Objects.equals(starId, cast.starId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, starId);
    }

    @Override
    public String toString() {
        return movieId + ";;" + starId;
    }
}
