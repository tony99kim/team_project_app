package com.example.team_project.Chat.ChatData;


public class User_ChatData {

    private String email;
    private String name;
    private String username;
    private String gender;
    private String phone;
    private String profileImageUrl;
    private String birthDate;


    public User_ChatData() {

    }

    public User_ChatData(String email, String name, String username, String gender, String phone, String profileImageUrl, String birthDate) {
        this.username = username;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.birthDate = birthDate;
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBirthDate() {
        return birthDate;
    }
}