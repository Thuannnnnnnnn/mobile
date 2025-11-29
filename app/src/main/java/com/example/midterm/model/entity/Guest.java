package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "guest")
public class Guest {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "guestId")
    private int guestId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "role")
    private String role;
    @ColumnInfo(name = "bio")
    private String bio;
    @ColumnInfo(name = "social_link")
    private String socialLink;
    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;
    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "phone")
    private String phone;

    public Guest() {
    }

    @Ignore
    public Guest(String name, String role, String bio, String socialLink, String avatarUrl) {
        this.name = name;
        this.role = role;
        this.bio = bio;
        this.socialLink = socialLink;
        this.avatarUrl = avatarUrl;
    }

    public int getGuestId() {return guestId;}
    public void setGuestId(int guestId) {this.guestId = guestId;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}
    public String getBio() {return bio;}
    public void setBio(String bio) {this.bio = bio;}
    public String getSocialLink() {return socialLink;}
    public void setSocialLink(String socialLink) {this.socialLink = socialLink;}
    public String getAvatarUrl() {return avatarUrl;}
    public void setAvatarUrl(String avatarUrl) {this.avatarUrl = avatarUrl;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public String getPhone() {return phone;}
    public void setPhone(String phone) {this.phone = phone;}
}
