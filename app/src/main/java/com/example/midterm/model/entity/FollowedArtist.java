package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "followed_artists",
        foreignKeys = {
                @ForeignKey(
                        entity = Account.class,
                        parentColumns = "id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Guest.class,
                        parentColumns = "guestId",
                        childColumns = "artist_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "user_id"),
                @Index(value = "artist_id"),
                @Index(value = {"user_id", "artist_id"}, unique = true)
        }
)
public class FollowedArtist {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "artist_id")
    private int artistId;

    @ColumnInfo(name = "followed_at")
    private String followedAt;

    @ColumnInfo(name = "notifications_enabled")
    private boolean notificationsEnabled;

    // Constructors
    public FollowedArtist() {}

    @Ignore
    public FollowedArtist(int userId, int artistId, String followedAt, boolean notificationsEnabled) {
        this.userId = userId;
        this.artistId = artistId;
        this.followedAt = followedAt;
        this.notificationsEnabled = notificationsEnabled;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getArtistId() { return artistId; }
    public void setArtistId(int artistId) { this.artistId = artistId; }

    public String getFollowedAt() { return followedAt; }
    public void setFollowedAt(String followedAt) { this.followedAt = followedAt; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
}
