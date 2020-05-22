import java.util.Objects;

public class star {
    static String largestId;

    private String id, name;
    private int birthYear = -1;

    public star() {
        setUpId();
        id = largestId;
    };

    public star(String name) {
        this.name = name;
        setUpId();
        id = largestId;
    }

    public star(String id, String name, int b) {
        this.name = name;
        birthYear = b;
        this.id = id;
    }

    private void setUpId() {
        String numId = String.valueOf(Integer.parseInt(largestId.substring(2)) + 1);
        int n = largestId.length() - 2 - numId.length();
        largestId = "nm" + (n > 0 ? (new String(new char[n]).replace("\0", "0")) : "") + numId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        star star = (star) o;
        return birthYear == star.birthYear &&
                Objects.equals(name, star.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthYear);
    }

    @Override
    public String toString() {
        return id + ";;" + name + (birthYear > 0 ? ";;" + birthYear : "");
    }

    public String getId() {
        return id;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public String getName() {
        return name;
    }

    public void setBirthYear(String birthYear) {
        try {
            this.birthYear = Integer.parseInt(birthYear);
        } catch (NumberFormatException e) {
            this.birthYear = -1;
        }
    }

    public void setName(String name) {
        this.name = name;
    }
}