package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "ticket_types",
        foreignKeys = @ForeignKey(
                entity = Event.class,
                parentColumns = "id",
                childColumns = "event_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = "event_id")}
)
public class TicketType {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "event_id")
    private int eventID;

    @ColumnInfo(name = "code")
    private String code; // ví dụ: "VIP", "STANDARD"

    @ColumnInfo(name = "price")
    private double price;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "sold_quantity")
    private int soldQuantity;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "open_ticket_date")
    private String openTicketDate;

    @ColumnInfo(name = "close_ticket_date")
    private String closeTicketDate;

    @ColumnInfo(name = "created_at")
    private String createdAt;

    @ColumnInfo(name = "seat_rows")
    private int seatRows; // Số hàng ghế

    @ColumnInfo(name = "seat_columns")
    private int seatColumns; // Số cột ghế

    // ===== Constructors =====
    public TicketType() {}

    @Ignore
    public TicketType(int eventID, String code, double price,
                      int quantity,int soldQuantity, String description,
                      String openTicketDate, String closeTicketDate,
                      String createdAt) {
        this.eventID = eventID;
        this.code = code;
        this.price = price;
        this.quantity = quantity;
        this.soldQuantity = soldQuantity;
        this.description = description;
        this.openTicketDate = openTicketDate;
        this.closeTicketDate = closeTicketDate;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventID() { return eventID; }
    public void setEventID(int eventID) { this.eventID = eventID; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOpenTicketDate() { return openTicketDate; }
    public void setOpenTicketDate(String openTicketDate) { this.openTicketDate = openTicketDate; }

    public String getCloseTicketDate() { return closeTicketDate; }
    public void setCloseTicketDate(String closeTicketDate) { this.closeTicketDate = closeTicketDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getSeatRows() { return seatRows; }
    public void setSeatRows(int seatRows) { this.seatRows = seatRows; }

    public int getSeatColumns() { return seatColumns; }
    public void setSeatColumns(int seatColumns) { this.seatColumns = seatColumns; }
}
