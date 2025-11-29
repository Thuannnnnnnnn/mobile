package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "seats",
        foreignKeys = {
                @ForeignKey(
                        entity = EventSection.class,
                        parentColumns = "section_id", // PK của EventSection là 'long'
                        childColumns = "section_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = TicketType.class,
                        parentColumns = "id", // PK của TicketType là 'int'
                        childColumns = "ticket_type_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = "section_id"),
                @Index(value = "ticket_type_id")
        }
)
public class Seat {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "section_id")
    private long sectionId;

    @ColumnInfo(name = "ticket_type_id")
    private Integer ticketTypeID; // FK → TicketType.id (int)

    @ColumnInfo(name = "seat_row")
    private String seatRow; // Tên hàng (VD: "A", "B", "10")

    @ColumnInfo(name = "seat_number")
    private String seatNumber; // Số ghế (VD: "1", "2")

    @ColumnInfo(name = "status")
    private String status; // "available", "booked", "unassigned"

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    public Seat(long sectionId, Integer ticketTypeID, String seatRow, String seatNumber, String status, String createdAt, String updatedAt) {
        this.sectionId = sectionId;
        this.ticketTypeID = ticketTypeID;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSectionId() { return sectionId; }
    public void setSectionId(long sectionId) { this.sectionId = sectionId; }
    public Integer getTicketTypeID() { return ticketTypeID; }
    public void setTicketTypeID(Integer ticketTypeID) { this.ticketTypeID = ticketTypeID; }
    public String getSeatRow() { return seatRow; }
    public void setSeatRow(String seatRow) { this.seatRow = seatRow; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}