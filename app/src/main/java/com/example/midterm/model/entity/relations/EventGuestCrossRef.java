package com.example.midterm.model.entity.relations;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "event_guest_cross_ref",
        primaryKeys = {"eventId", "guestId"},
        indices = {@Index(value = "guestId")}
)
public class EventGuestCrossRef {
    @ColumnInfo(name = "eventId")
    private int eventId;
    @ColumnInfo(name = "guestId")
    private int guestId;

    public EventGuestCrossRef(int eventId, int guestId) {
        this.eventId = eventId;
        this.guestId = guestId;
    }

    public int getEventId() {return eventId;}
    public int getGuestId() {return guestId;}
}
