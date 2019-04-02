package com.example.benjamin.letsmeet;

public class User {
    private String email;
    private String name;
    private String location;
    private String token;

    public User(){}

    public User(String email, String name, String location, String token) {
        this.email = email;
        this.name = name;
        this.location = location;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
