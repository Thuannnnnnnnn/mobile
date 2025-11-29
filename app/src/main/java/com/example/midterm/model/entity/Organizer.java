package com.example.midterm.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(
        tableName = "organizers",
        foreignKeys = @ForeignKey(
                entity = Account.class,
                parentColumns = "id",
                childColumns = "organizerId",
                onDelete = ForeignKey.CASCADE
        )
)
public class Organizer {

    @PrimaryKey
    @ColumnInfo(name = "organizerId")
    private int organizerId;  // Chính là AccountID

    @ColumnInfo(name = "organizerName")
    private String organizerName;

    @ColumnInfo(name = "logo")
    private String logo; // URL hoặc đường dẫn ảnh logo

    @ColumnInfo(name = "website")
    private String website;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "status")
    private String status;
    @ColumnInfo(name = "updatedAt")
    private long updatedAt;

    @Ignore
    public Organizer(int organizerId, String organizerName, String logo, String website, String address,
                     String description, String status,long updatedAt) {
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.logo = logo;
        this.website = website;
        this.address = address;
        this.description = description;
        this.status = status;
        this.updatedAt = updatedAt;
    }
    // Constructor
    public Organizer() {}

    // Getter & Setter
    public int getOrganizerId() { return organizerId;}
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId;}
    public String getOrganizerName() { return organizerName;}
    public void setOrganizerName(String organizerName) {this.organizerName = organizerName;}
    public String getLogo() { return logo;}
    public void setLogo(String logo) {this.logo = logo;}
    public String getWebsite() {return website;}
    public void setWebsite(String website) {this.website = website;}
    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(long updatedAt) {this.updatedAt = updatedAt;}
}
