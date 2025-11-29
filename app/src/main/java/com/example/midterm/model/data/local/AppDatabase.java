package com.example.midterm.model.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

import com.example.midterm.model.entity.Account;
import com.example.midterm.model.entity.Discount;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.EventSection;
import com.example.midterm.model.entity.FollowedArtist;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.Notification;
import com.example.midterm.model.entity.Organizer;
import com.example.midterm.model.entity.OrganizerVerification;
import com.example.midterm.model.entity.Review;
import com.example.midterm.model.entity.Seat;
import com.example.midterm.model.entity.Ticket;
import com.example.midterm.model.entity.TicketType;
import com.example.midterm.model.entity.UserProfile;
import com.example.midterm.model.entity.relations.EventGuestCrossRef;

@Database(entities = {
        TicketType.class,
        Event.class,
        EventSection.class,
        Account.class,
        Organizer.class,
        UserProfile.class,
        Guest.class,
        EventGuestCrossRef.class,
        Ticket.class,
        Seat.class,
        Notification.class,
        OrganizerVerification.class,
        Review.class,
        FollowedArtist.class,
        Discount.class
}, version = 21, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public abstract TicketTypeDAO ticketTypeDAO();
    public abstract TicketDAO ticketDAO();
    public abstract EventDAO eventDAO();
    public abstract EventSectionDAO eventSectionDAO();
    public abstract AccountDAO accountDAO();
    public abstract OrganizerDAO organizerDAO();
    public abstract UserProfileDAO userProfileDAO();
    public abstract GuestDAO guestDAO();
    public abstract SeatDAO seatDAO();
    public abstract NotificationDAO notificationDAO();
    public abstract OrganizerVerificationDAO organizerVerificationDAO();
    public abstract ReviewDAO reviewDAO();
    public abstract FollowedArtistDAO followedArtistDAO();
    public abstract DiscountDAO discountDAO();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(appContext,
                                    AppDatabase.class, "event_booking_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Seed when database is created for the first time
                                    seedDatabase(appContext);
                                }

                                @Override
                                public void onDestructiveMigration(@NonNull SupportSQLiteDatabase db) {
                                    super.onDestructiveMigration(db);
                                    // Seed when database is recreated due to version change
                                    seedDatabase(appContext);
                                }

                                private void seedDatabase(Context context) {
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        new DatabaseSeeder(context).seedAll();
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
