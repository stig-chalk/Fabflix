public class star implements Comparable<star> {
    private String name, id;
    private int num_movies;

    public star(String n, String i, int nm) {
        name = n;
        id = i;
        num_movies = nm;
    }

    public int getNum_movies() {
        return num_movies;
    }

    public void setNum_movies(int num_movies) {
        this.num_movies = num_movies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(star o) {
        if (this.num_movies != o.num_movies)
            return  o.num_movies - this.num_movies;
        return this.name.compareTo(o.name);
    }
}
