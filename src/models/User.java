package models;

public class User {
    private int userId;
    private String username;
    private String userEmail;
    private String userSubject;
    private boolean isTeacher;
    private boolean isAdmin;

    public User(int userId, String username, String userEmail, boolean isTeacher, boolean isAdmin) {
        this.setId(userId);
        this.setUsername(username);
        this.setEmail(userEmail);
        this.setTeacher(isTeacher);
        this.setAdmin(isAdmin);
        this.setSubject("");
    }

    public User(int userId, String username, String userEmail, boolean isTeacher, boolean isAdmin, String userSubject) {
        this.setId(userId);
        this.setUsername(username);
        this.setEmail(userEmail);
        this.setTeacher(isTeacher);
        this.setAdmin(isAdmin);
        this.setSubject(userSubject);
    }

    public void setId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getEmail() {
        return userEmail;
    }

    public void setSubject(String userSubject) {
        this.userSubject = userSubject;
    }

    public String getSubject() {
        return userSubject;
    }

    public void setTeacher(boolean isTeacher) {
        this.isTeacher = isTeacher;
    }

    public boolean isTeacher() {
        return isTeacher;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}