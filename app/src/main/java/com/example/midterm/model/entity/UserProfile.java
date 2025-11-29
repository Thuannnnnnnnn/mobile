package com.example.midterm.model.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "user_profile",
        foreignKeys = @ForeignKey(
                entity = Account.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE // Xóa account thì xóa luôn user profile
        )
)
public class UserProfile {
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private int userId; // chính là AccountID

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "date_of_birth")
    private String dateOfBirth;

    @ColumnInfo(name = "sex")
    private String sex; // "Nam", "Nữ", hoặc "Khác"

    @ColumnInfo(name = "avatar")
    private String avatar; // link hoặc đường dẫn ảnh

    public UserProfile(int userId, String fullName, String dateOfBirth, String sex, String avatar) {
        this.userId = userId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
        this.avatar = avatar;
    }

    public int getUserId() {return userId;}
    public void setUserId(int userId) {this.userId = userId;}
    public String getFullName() {return fullName;}
    public void setFullName(String fullName) {this.fullName = fullName;}
    public String getDateOfBirth() {return dateOfBirth;}
    public void setDateOfBirth(String dateOfBirth) {this.dateOfBirth = dateOfBirth;}
    public String getSex() {return sex;}
    public void setSex(String sex) {this.sex = sex;}
    public String getAvatar() {return avatar;}
    public void setAvatar(String avatar) {this.avatar = avatar;}
}
