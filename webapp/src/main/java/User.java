public class User {
    private final String email;
    private final String firstName;
    private final String lastName;
    private int id;
    public User(String username, String firstName, String lastName, int id) {
        this.email = new String(username);
        this.firstName = new String(firstName);
        this.lastName = new String(lastName);
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }
}
