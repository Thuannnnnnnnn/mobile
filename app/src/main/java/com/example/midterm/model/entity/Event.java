package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "events",
        foreignKeys = @ForeignKey(
                entity = Account.class,
                parentColumns = "id",
                childColumns = "organizer_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "organizer_id")}
)
public class Event {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "event_id")
    private String eventID; // Mã sự kiện nội bộ (UUID hoặc custom)

    @ColumnInfo(name = "organizer_id")
    private int organizerID; // Khóa ngoại -> Account

    @ColumnInfo(name = "event_name")
    private String eventName;

    @ColumnInfo(name = "bannerUrl")
    private String bannerUrl;

    @ColumnInfo(name = "videoUrl")
    private String videoUrl;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "genre")
    private String genre;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    @ColumnInfo(name = "is_promoted")
    private boolean isPromoted;

    public Event() {}

    @Ignore
    public Event(String eventID, int organizerID, String eventName, String bannerUrl, String videoUrl,
                 String description, String genre, String location,
                 String startDate, String endDate, String createdAt, String updatedAt, boolean isPromoted) {
        this.eventID = eventID;
        this.organizerID = organizerID;
        this.eventName = eventName;
        this.bannerUrl = bannerUrl;
        this.videoUrl = videoUrl;
        this.description = description;
        this.genre = genre;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPromoted = isPromoted;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEventID() { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }

    public int getOrganizerID() { return organizerID; }
    public void setOrganizerID(int organizerID) { this.organizerID = organizerID; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }



    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public boolean isPromoted() { return isPromoted; }
    public void setPromoted(boolean promoted) { isPromoted = promoted; }
}