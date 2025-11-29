package com.example.midterm.model.data.local;

import android.content.Context;

import com.example.midterm.model.entity.Account;
import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.EventSection;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.Notification;
import com.example.midterm.model.entity.Organizer;
import com.example.midterm.model.entity.Review;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.entity.Ticket;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.model.entity.UserProfile;
import com.example.midterm.model.entity.relations.EventGuestCrossRef;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseSeeder {
    private final AppDatabase database;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public DatabaseSeeder(Context context) {
        this.database = AppDatabase.getInstance(context);
    }

    public void seedAll() {
        // Check if already seeded
        if (database.accountDAO().getAccountCount() > 0) {
            return;
        }

        seedAccounts();
        seedUserProfiles();
        seedOrganizers();
        seedEvents();
        seedTicketTypes();
        seedGuests();
        seedEventGuestCrossRef();
        seedEventSections();
        seedSeats();
        seedTickets();
        seedReviews();
        seedDiscounts();
        seedNotifications();
    }

    private String getCurrentDateTime() {
        return dateFormat.format(new Date());
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private String getFutureDateTime(int daysFromNow) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, daysFromNow);
        return dateFormat.format(calendar.getTime());
    }

    private String getPastDateTime(int daysAgo) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysAgo);
        return dateFormat.format(calendar.getTime());
    }

    private void seedAccounts() {
        // User's organizer account
        Account organizer1 = new Account();
        organizer1.setEmail("523h0006@student.tdtu.edu.vn");
        organizer1.setPhone("0934904890");
        organizer1.setPassword("123456");
        organizer1.setRole("organizer");
        organizer1.setCreatedAt(getCurrentDateTime());
        database.accountDAO().insert(organizer1);

        Account organizer2 = new Account();
        organizer2.setEmail("523h0007@student.tdtu.edu.vn");
        organizer2.setPhone("0975704891");
        organizer2.setPassword("123456");
        organizer2.setRole("organizer");
        organizer2.setCreatedAt(getCurrentDateTime());
        database.accountDAO().insert(organizer2);

        // users
        Account user1 = new Account();
        user1.setEmail("user1@gmail.com");
        user1.setPhone("0903456789");
        user1.setPassword("123456");
        user1.setRole("user");
        user1.setCreatedAt(getCurrentDateTime());
        database.accountDAO().insert(user1);

        Account user2 = new Account();
        user2.setEmail("user2@gmail.com");
        user2.setPhone("0904567890");
        user2.setPassword("123456");
        user2.setRole("user");
        user2.setCreatedAt(getCurrentDateTime());
        database.accountDAO().insert(user2);
    }

    private void seedUserProfiles() {
        // Profile for account 1 (hung@company.vn)
        UserProfile profile1 = new UserProfile(1, "Nguyễn Gia Bảo", "1990-05-15", "Nam", null);
        database.userProfileDAO().insert(profile1);

        // Profile for account 2
        UserProfile profile2 = new UserProfile(2, "Trần Thị Mai", "1988-08-20", "Nữ", null);
        database.userProfileDAO().insert(profile2);

        // Profile for account 3
        UserProfile profile3 = new UserProfile(3, "Lê Minh Tuấn", "1995-03-10", "Nam", null);
        database.userProfileDAO().insert(profile3);

        // Profile for account 4
        UserProfile profile4 = new UserProfile(4, "Phạm Thị Hoa", "1992-12-25", "Nữ", null);
        database.userProfileDAO().insert(profile4);
    }

    private void seedOrganizers() {
        // Organizer 1 - Hùng Entertainment (account 1)
        Organizer org1 = new Organizer();
        org1.setOrganizerId(1);
        org1.setOrganizerName("Hùng Entertainment");
        org1.setWebsite("https://hungentertainment.vn");
        org1.setAddress("123 Nguyễn Huệ, Quận 1, TP.HCM");
        org1.setDescription("Công ty tổ chức sự kiện âm nhạc hàng đầu Việt Nam");
        org1.setStatus("approved");
        org1.setUpdatedAt(getCurrentTimestamp());
        database.organizerDAO().insert(org1);

        // Organizer 2 - Star Events (account 2)
        Organizer org2 = new Organizer();
        org2.setOrganizerId(2);
        org2.setOrganizerName("Star Events");
        org2.setWebsite("https://starevents.com");
        org2.setAddress("456 Lê Lợi, Quận 1, TP.HCM");
        org2.setDescription("Chuyên tổ chức hội nghị và sự kiện doanh nghiệp");
        org2.setStatus("approved");
        org2.setUpdatedAt(getCurrentTimestamp());
        database.organizerDAO().insert(org2);
    }

    private void seedEvents() {
        // Event 1 - Active (Organizer 1)
        Event event1 = new Event();
        event1.setOrganizerID(1);
        event1.setEventName("Rock Festival 2024");
        event1.setDescription("Đại nhạc hội Rock lớn nhất năm với sự tham gia của nhiều ban nhạc nổi tiếng");
        event1.setLocation("Sân vận động Phú Thọ, TP.HCM");
        event1.setStartDate(getFutureDateTime(7));
        event1.setEndDate(getFutureDateTime(7));
        event1.setGenre("Rock");
        event1.setCreatedAt(getCurrentDateTime());
        event1.setUpdatedAt(getCurrentDateTime());
        database.eventDAO().insert(event1);

        // Event 2 - Active (Organizer 1)
        Event event2 = new Event();
        event2.setOrganizerID(1);
        event2.setEventName("Jazz Night");
        event2.setDescription("Đêm nhạc Jazz với các nghệ sĩ quốc tế");
        event2.setLocation("Nhà hát Thành phố, TP.HCM");
        event2.setStartDate(getFutureDateTime(14));
        event2.setEndDate(getFutureDateTime(14));
        event2.setGenre("Jazz");
        event2.setCreatedAt(getCurrentDateTime());
        event2.setUpdatedAt(getCurrentDateTime());
        database.eventDAO().insert(event2);

        // Event 3 - Active (Organizer 2)
        Event event3 = new Event();
        event3.setOrganizerID(2);
        event3.setEventName("Tech Conference 2024");
        event3.setDescription("Hội nghị công nghệ với các diễn giả hàng đầu");
        event3.setLocation("GEM Center, Quận 1, TP.HCM");
        event3.setStartDate(getFutureDateTime(21));
        event3.setEndDate(getFutureDateTime(22));
        event3.setGenre("Conference");
        event3.setCreatedAt(getCurrentDateTime());
        event3.setUpdatedAt(getCurrentDateTime());
        database.eventDAO().insert(event3);

        // Event 4 - Past (Organizer 1)
        Event event4 = new Event();
        event4.setOrganizerID(1);
        event4.setEventName("Acoustic Night");
        event4.setDescription("Đêm nhạc Acoustic ấm cúng");
        event4.setLocation("Cafe Acoustic, Quận 3, TP.HCM");
        event4.setStartDate(getPastDateTime(7));
        event4.setEndDate(getPastDateTime(7));
        event4.setGenre("Acoustic");
        event4.setCreatedAt(getPastDateTime(30));
        event4.setUpdatedAt(getPastDateTime(7));
        database.eventDAO().insert(event4);

        // Event 5 - Past (Organizer 2)
        Event event5 = new Event();
        event5.setOrganizerID(2);
        event5.setEventName("Startup Meetup");
        event5.setDescription("Gặp gỡ cộng đồng Startup");
        event5.setLocation("Dreamplex, Quận 1, TP.HCM");
        event5.setStartDate(getPastDateTime(14));
        event5.setEndDate(getPastDateTime(14));
        event5.setGenre("Meetup");
        event5.setCreatedAt(getPastDateTime(45));
        event5.setUpdatedAt(getPastDateTime(14));
        database.eventDAO().insert(event5);
    }

    private void seedTicketTypes() {
        String now = getCurrentDateTime();

        // Event 1 ticket types
        TicketType tt1 = new TicketType(1, "VIP", 1500000, 100, 30,
            "Khu VIP gần sân khấu", getFutureDateTime(-30), getFutureDateTime(6), now);
        database.ticketTypeDAO().insert(tt1);

        TicketType tt2 = new TicketType(1, "Standard", 800000, 500, 150,
            "Khu thường", getFutureDateTime(-30), getFutureDateTime(6), now);
        database.ticketTypeDAO().insert(tt2);

        TicketType tt3 = new TicketType(1, "Student", 500000, 200, 80,
            "Vé sinh viên", getFutureDateTime(-30), getFutureDateTime(6), now);
        database.ticketTypeDAO().insert(tt3);

        // Event 2 ticket types
        TicketType tt4 = new TicketType(2, "Premium", 2000000, 50, 20,
            "Ghế hạng Premium", getFutureDateTime(-20), getFutureDateTime(13), now);
        database.ticketTypeDAO().insert(tt4);

        TicketType tt5 = new TicketType(2, "Regular", 1000000, 200, 100,
            "Ghế thường", getFutureDateTime(-20), getFutureDateTime(13), now);
        database.ticketTypeDAO().insert(tt5);

        // Event 3 ticket types
        TicketType tt6 = new TicketType(3, "Full Pass", 3000000, 100, 50,
            "Tham dự toàn bộ", getFutureDateTime(-10), getFutureDateTime(20), now);
        database.ticketTypeDAO().insert(tt6);

        TicketType tt7 = new TicketType(3, "Day Pass", 1500000, 300, 120,
            "Tham dự 1 ngày", getFutureDateTime(-10), getFutureDateTime(20), now);
        database.ticketTypeDAO().insert(tt7);

        // Event 4 ticket types (past)
        TicketType tt8 = new TicketType(4, "General", 300000, 100, 95,
            "Vé thường", getPastDateTime(60), getPastDateTime(8), getPastDateTime(30));
        database.ticketTypeDAO().insert(tt8);

        // Event 5 ticket types (past)
        TicketType tt9 = new TicketType(5, "Free", 0, 50, 48,
            "Vé miễn phí", getPastDateTime(75), getPastDateTime(15), getPastDateTime(45));
        database.ticketTypeDAO().insert(tt9);
    }

    private void seedGuests() {
        Guest guest1 = new Guest("Sơn Tùng M-TP", "Ca sĩ chính",
            "Ca sĩ, nhạc sĩ nổi tiếng Việt Nam",
            "https://facebook.com/sontungmtp", null);
        database.guestDAO().insert(guest1);

        Guest guest2 = new Guest("Đen Vâu", "Rapper",
            "Rapper và nhạc sĩ underground",
            "https://facebook.com/denvau", null);
        database.guestDAO().insert(guest2);

        Guest guest3 = new Guest("Hoàng Thùy Linh", "Ca sĩ",
            "Ca sĩ, diễn viên đa tài",
            "https://facebook.com/hoangthuylinh", null);
        database.guestDAO().insert(guest3);

        Guest guest4 = new Guest("Nguyễn Hà Đông", "Diễn giả",
            "CEO và founder startup công nghệ",
            "https://linkedin.com/hadong", null);
        database.guestDAO().insert(guest4);

        Guest guest5 = new Guest("Kenny G", "Saxophonist",
            "Nghệ sĩ saxophone huyền thoại",
            "https://kennyg.com", null);
        database.guestDAO().insert(guest5);
    }

    private void seedEventGuestCrossRef() {
        // Event 1 guests
        database.guestDAO().insertEventGuestCrossRef(new EventGuestCrossRef(1, 1));
        database.guestDAO().insertEventGuestCrossRef(new EventGuestCrossRef(1, 2));
        database.guestDAO().insertEventGuestCrossRef(new EventGuestCrossRef(1, 3));

        // Event 2 guests
        database.guestDAO().insertEventGuestCrossRef(new EventGuestCrossRef(2, 5));

        // Event 3 guests
        database.guestDAO().insertEventGuestCrossRef(new EventGuestCrossRef(3, 4));

        // Event 4 guests
        database.guestDAO().insertEventGuestCrossRef(new EventGuestCrossRef(4, 2));
    }

    private void seedEventSections() {
        // Event 1 sections
        EventSection section1 = new EventSection(1, "Khu VIP", "seated", 100, 10, 10, 1);
        database.eventSectionDAO().insert(section1);

        EventSection section2 = new EventSection(1, "Khu A", "seated", 250, 15, 17, 2);
        database.eventSectionDAO().insert(section2);

        EventSection section3 = new EventSection(1, "Khu B", "seated", 250, 15, 17, 3);
        database.eventSectionDAO().insert(section3);

        // Event 2 sections
        EventSection section4 = new EventSection(2, "Tầng 1", "seated", 150, 10, 15, 1);
        database.eventSectionDAO().insert(section4);

        EventSection section5 = new EventSection(2, "Tầng 2", "seated", 100, 10, 10, 2);
        database.eventSectionDAO().insert(section5);
    }

    private void seedSeats() {
        String now = getCurrentDateTime();

        // Create some seats for Section 1 (Event 1 - VIP)
        for (int row = 1; row <= 5; row++) {
            for (int col = 1; col <= 10; col++) {
                String seatRow = String.valueOf((char)('A' + row - 1));
                String seatNumber = String.valueOf(col);
                String status = col <= 3 ? "booked" : "available";

                Seat seat = new Seat(1, 1, seatRow, seatNumber, status, now, now);
                database.seatDAO().insert(seat);
            }
        }

        // Create some seats for Section 4 (Event 2)
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 8; col++) {
                String seatRow = String.valueOf((char)('A' + row - 1));
                String seatNumber = String.valueOf(col);

                Seat seat = new Seat(4, 4, seatRow, seatNumber, "available", now, now);
                database.seatDAO().insert(seat);
            }
        }
    }

    private void seedTickets() {
        List<Ticket> tickets = new ArrayList<>();

        // Tickets for user 3 (upcoming event)
        Ticket ticket1 = new Ticket();
        ticket1.setTicketTypeID(1);
        ticket1.setBuyerID(3);
        ticket1.setQrCode(UUID.randomUUID().toString());
        ticket1.setPurchaseDate(getCurrentDateTime());
        ticket1.setStatus("booked");
        ticket1.setCreatedAt(getCurrentDateTime());
        ticket1.setUpdatedAt(getCurrentDateTime());
        tickets.add(ticket1);

        Ticket ticket2 = new Ticket();
        ticket2.setTicketTypeID(2);
        ticket2.setBuyerID(3);
        ticket2.setQrCode(UUID.randomUUID().toString());
        ticket2.setPurchaseDate(getCurrentDateTime());
        ticket2.setStatus("booked");
        ticket2.setCreatedAt(getCurrentDateTime());
        ticket2.setUpdatedAt(getCurrentDateTime());
        tickets.add(ticket2);

        // Tickets for user 4
        Ticket ticket3 = new Ticket();
        ticket3.setTicketTypeID(4);
        ticket3.setBuyerID(4);
        ticket3.setQrCode(UUID.randomUUID().toString());
        ticket3.setPurchaseDate(getCurrentDateTime());
        ticket3.setStatus("booked");
        ticket3.setCreatedAt(getCurrentDateTime());
        ticket3.setUpdatedAt(getCurrentDateTime());
        tickets.add(ticket3);

        // Past ticket for user 3 (checked in)
        Ticket ticket4 = new Ticket();
        ticket4.setTicketTypeID(8);
        ticket4.setBuyerID(3);
        ticket4.setQrCode(UUID.randomUUID().toString());
        ticket4.setPurchaseDate(getPastDateTime(20));
        ticket4.setCheckInDate(getPastDateTime(7));
        ticket4.setStatus("checked_in");
        ticket4.setCreatedAt(getPastDateTime(20));
        ticket4.setUpdatedAt(getPastDateTime(7));
        tickets.add(ticket4);

        // Past ticket for user 5
        Ticket ticket5 = new Ticket();
        ticket5.setTicketTypeID(9);
        ticket5.setBuyerID(5);
        ticket5.setQrCode(UUID.randomUUID().toString());
        ticket5.setPurchaseDate(getPastDateTime(30));
        ticket5.setCheckInDate(getPastDateTime(14));
        ticket5.setStatus("checked_in");
        ticket5.setCreatedAt(getPastDateTime(30));
        ticket5.setUpdatedAt(getPastDateTime(14));
        tickets.add(ticket5);

        database.ticketDAO().insertAll(tickets);
    }

    private void seedReviews() {
        // Reviews for past events
        Review review1 = new Review(4, 3, 4.5f, "Sự kiện rất hay, âm thanh tốt!", getPastDateTime(5));
        database.reviewDAO().insert(review1);

        Review review2 = new Review(4, 4, 5.0f, "Tuyệt vời! Chắc chắn sẽ tham gia lần sau", getPastDateTime(4));
        database.reviewDAO().insert(review2);

        Review review3 = new Review(5, 5, 4.0f, "Nội dung hay, mong có nhiều sự kiện hơn", getPastDateTime(10));
        database.reviewDAO().insert(review3);
    }

    private void seedDiscounts() {
        // Discount for Event 1
        Discount discount1 = new Discount(1, "EARLY20", "percentage", 20, 500000, 300000, 100,
                getCurrentDateTime(), getFutureDateTime(5), getCurrentDateTime());
        database.discountDAO().insert(discount1);

        // Discount for Event 2
        Discount discount2 = new Discount(2, "JAZZ50K", "fixed", 50000, 1000000, 50000, 50,
                getCurrentDateTime(), getFutureDateTime(10), getCurrentDateTime());
        database.discountDAO().insert(discount2);

        // Discount for Event 3
        Discount discount3 = new Discount(3, "TECH10", "percentage", 10, 1500000, 200000, 200,
                getCurrentDateTime(), getFutureDateTime(20), getCurrentDateTime());
        database.discountDAO().insert(discount3);
    }

    private void seedNotifications() {
        // Notifications for events
        Notification notif1 = new Notification(1, 1, "Sự kiện sắp diễn ra!",
                "Rock Festival 2024 sẽ diễn ra trong 7 ngày nữa. Hãy sẵn sàng!",
                "reminder", getCurrentDateTime(), 30, "sent", getCurrentDateTime());
        database.notificationDAO().insert(notif1);

        Notification notif2 = new Notification(2, 1, "Giảm giá đặc biệt",
                "Sử dụng mã JAZZ50K để được giảm 50,000đ cho vé Jazz Night",
                "broadcast", getCurrentDateTime(), 100, "sent", getCurrentDateTime());
        database.notificationDAO().insert(notif2);
    }
}
