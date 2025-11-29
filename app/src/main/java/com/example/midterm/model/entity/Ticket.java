package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tickets",
        foreignKeys = {
                @ForeignKey(
                        entity = TicketType.class,
                        parentColumns = "id",
                        childColumns = "ticket_type_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Seat.class,
                        parentColumns = "id",
                        childColumns = "seat_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = "ticket_type_id"),
                @Index(value = "seat_id")
        }
)
public class Ticket {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "ticket_type_id")
    private int ticketTypeID;

    @ColumnInfo(name = "buyer_id")
    private int buyerID;  // AccountID

    @ColumnInfo(name = "seat_id")
    private Long seatId;  // FK đến Seat (nullable)

    @ColumnInfo(name = "qr_code")
    private String qrCode; // UUID duy nhất — dùng để check-in

    @ColumnInfo(name = "purchase_date")
    private String purchaseDate;

    @ColumnInfo(name = "checkin_date")
    private String checkInDate;

    @ColumnInfo(name = "status")
    public String status; // "booked", "checked_in", "cancelled"

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public int getTicketTypeID() {return ticketTypeID;}
    public void setTicketTypeID(int ticketTypeID) {this.ticketTypeID = ticketTypeID;}
    public int getBuyerID() {return buyerID;}
    public void setBuyerID(int buyerID) {this.buyerID = buyerID;}
    public Long getSeatId() {return seatId;}
    public void setSeatId(Long seatId) {this.seatId = seatId;}
    public String getQrCode() {return qrCode;}
    public void setQrCode(String qrCode) {this.qrCode = qrCode;}
    public String getPurchaseDate() {return purchaseDate;}
    public void setPurchaseDate(String purchaseDate) {this.purchaseDate = purchaseDate;}
    public String getCheckInDate() {return checkInDate;}
    public void setCheckInDate(String checkInDate) {this.checkInDate = checkInDate;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public String getCreatedAt() {return createdAt;}
    public void setCreatedAt(String createdAt) {this.createdAt = createdAt;}
    public String getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(String updatedAt) {this.updatedAt = updatedAt;}
}
