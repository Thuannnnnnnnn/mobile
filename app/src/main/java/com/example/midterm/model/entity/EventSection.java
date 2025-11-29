package com.example.midterm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "event_sections",
        foreignKeys = @ForeignKey(
                entity = Event.class, // Bảng cha
                parentColumns = "id", // Khóa chính của bảng cha (Event)
                childColumns = "event_id", // Khóa ngoại trong bảng này
                onDelete = ForeignKey.CASCADE // Tự động xóa Section khi Event bị xóa
        ),
        // Tạo chỉ mục (Index) cho event_id để truy vấn nhanh hơn
        indices = {@Index("event_id")}
)
public class EventSection {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_id")
    public long sectionId;

    @ColumnInfo(name = "event_id")
    public int eventId;

    @ColumnInfo(name = "name")
    public String name; // Tên khu vực (VD: "Khu VIP")

    @ColumnInfo(name = "section_type")
    public String sectionType; // "seated" hoặc "standing"

    @ColumnInfo(name = "capacity")
    public int capacity;

    // Dùng kiểu Integer (object) thay vì int (primitive) để cho phép NULL
    @ColumnInfo(name = "map_total_rows")
    public Integer mapTotalRows;

    @ColumnInfo(name = "map_total_cols")
    public Integer mapTotalCols;

    @ColumnInfo(name = "display_order")
    public int displayOrder; // Thứ tự hiển thị

    public EventSection(int eventId, String name, String sectionType, int capacity, Integer mapTotalRows, Integer mapTotalCols, int displayOrder) {
        this.eventId = eventId;
        this.name = name;
        this.sectionType = sectionType;
        this.capacity = capacity;
        this.mapTotalRows = mapTotalRows;
        this.mapTotalCols = mapTotalCols;
        this.displayOrder = displayOrder;
    }

    public long getSectionId() {return sectionId;}
    public void setSectionId(long sectionId) {this.sectionId = sectionId;}
    public int getEventId() {return eventId;}
    public void setEventId(int eventId) {this.eventId = eventId;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getSectionType() {return sectionType;}
    public void setSectionType(String sectionType) {this.sectionType = sectionType;}
    public int getCapacity() {return capacity;}
    public void setCapacity(int capacity) {this.capacity = capacity;}
    public Integer getMapTotalRows() {return mapTotalRows;}
    public void setMapTotalRows(Integer mapTotalRows) {this.mapTotalRows = mapTotalRows;}
    public Integer getMapTotalCols() {return mapTotalCols;}
    public void setMapTotalCols(Integer mapTotalCols) {this.mapTotalCols = mapTotalCols;}
    public int getDisplayOrder() {return displayOrder;}
    public void setDisplayOrder(int displayOrder) {this.displayOrder = displayOrder;}
}