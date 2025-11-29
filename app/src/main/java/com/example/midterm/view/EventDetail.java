package com.example.midterm.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.midterm.model.WeatherInfo;
import com.example.midterm.service.WeatherService;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.model.entity.TicketType;

import com.example.midterm.view.Adapter.GuestDetailAdapter;
import com.example.midterm.view.Adapter.ReviewAdapter;
import com.example.midterm.view.Adapter.TicketDetailAdapter;
import com.example.midterm.viewModel.EventViewModel;
import com.example.midterm.viewModel.ReviewViewModel;
import com.example.midterm.model.entity.Review;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.RatingBar;
import android.widget.EditText;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Thêm các import cho Google Maps
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Geocoder;

public class EventDetail extends AppCompatActivity implements OnMapReadyCallback {

    private int currentEventId;
    private int currentUserId;

    private EventViewModel eventViewModel;
    private ReviewViewModel reviewViewModel;
    private GuestDetailAdapter guestAdapter;
    private TicketDetailAdapter ticketAdapter;
    private ReviewAdapter reviewAdapter;

    private MaterialToolbar toolbar;
    private ImageView ivEventBanner;
    private TextView tvEventName, tvEventDate, tvEventLocation, tvEventDescription, tvLowestPrice;
    private RecyclerView rvTicketTypes, rvGuests;
    private LinearLayout llGuestSection;
    private Button btnBuyTicket;

    // Views cho Video Player
    private View videoTitleView;
    private View videoCardView;
    private VideoView videoViewEvent;
    private FrameLayout layoutVideoThumbnail;
    private ImageView imgVideoThumbnail;
    private ImageView btnPlayVideo;

    // Weather views
    private LinearLayout llWeatherSection;
    private TextView tvWeatherTemp, tvWeatherDesc, tvWeatherHumidity, tvWeatherWind, tvWeatherAdvice;
    private ImageView ivWeatherIcon;
    private WeatherService weatherService;

    // Review views
    private RecyclerView rvReviews;
    private Button btnWriteReview;
    private RatingBar ratingBarAvg;
    private TextView tvReviewCount;

    // Biến cho Google Map
    private GoogleMap mMap;
    private String eventAddress;

    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEE, dd MMM, yyyy • h:mm a", new Locale("vi", "VN"));
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_detail);

        currentEventId = getIntent().getIntExtra("EVENT_ID", -1);
        if (currentEventId == -1) {
            finish();
            return;
        }

        // Get userId from SharedPreferences
        currentUserId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1);

        findViews();
        setupToolbar();
        setupRecyclerViews();

        weatherService = new WeatherService();
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        reviewViewModel = new ViewModelProvider(this).get(ReviewViewModel.class);

        observeData(currentEventId);
        loadReviews(currentEventId);

        // Khởi tạo Map Fragment và đăng ký callback
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment); // Đảm bảo R.id.mapFragment tồn tại trong layout
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            // Log hoặc thông báo lỗi nếu mapFragment không tìm thấy
            Toast.makeText(this, "Map fragment not found in layout.", Toast.LENGTH_LONG).show();
        }

        // Mở Google Maps khi click vào địa chỉ
        tvEventLocation.setOnClickListener(v -> {
            String address = tvEventLocation.getText().toString();
            if (!address.isEmpty()) {
                // Tạo Uri cho Google Maps
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Kiểm tra xem máy có cài Maps không
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Nếu không có app Maps, mở bằng trình duyệt
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address))));
                }
            }
        });

        btnBuyTicket.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetail.this, BookTicketActivity.class);
            intent.putExtra("EVENT_ID", currentEventId);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void findViews() {
        toolbar = findViewById(R.id.toolbar);
        ivEventBanner = findViewById(R.id.iv_event_banner);
        tvEventName = findViewById(R.id.tv_event_name);
        tvEventDate = findViewById(R.id.tv_event_date);
        tvEventLocation = findViewById(R.id.tv_event_location);
        tvEventDescription = findViewById(R.id.tv_event_description);
        tvLowestPrice = findViewById(R.id.tv_lowest_price);
        rvTicketTypes = findViewById(R.id.rv_ticket_types);
        rvGuests = findViewById(R.id.rv_guests);
        llGuestSection = findViewById(R.id.ll_guest_section);
        btnBuyTicket = findViewById(R.id.btn_buy_ticket);

        videoTitleView = findViewById(R.id.tv_video_title);
        videoCardView = findViewById(R.id.cv_video_player);
        videoViewEvent = findViewById(R.id.videoView_event);
        layoutVideoThumbnail = findViewById(R.id.layout_video_thumbnail);
        imgVideoThumbnail = findViewById(R.id.img_video_thumbnail);
        btnPlayVideo = findViewById(R.id.btn_play_video);

        // Weather views
        llWeatherSection = findViewById(R.id.ll_weather_section);
        tvWeatherTemp = findViewById(R.id.tv_weather_temp);
        tvWeatherDesc = findViewById(R.id.tv_weather_desc);
        tvWeatherHumidity = findViewById(R.id.tv_weather_humidity);
        tvWeatherWind = findViewById(R.id.tv_weather_wind);
        tvWeatherAdvice = findViewById(R.id.tv_weather_advice);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);

        // Review views
        rvReviews = findViewById(R.id.rv_reviews);
        btnWriteReview = findViewById(R.id.btn_write_review);
        ratingBarAvg = findViewById(R.id.rating_bar_avg);
        tvReviewCount = findViewById(R.id.tv_review_count);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Event currentEvent;

    private void shareEvent() {
        if (currentEvent == null) {
            Toast.makeText(this, "Đang tải thông tin sự kiện...", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "🎉 " + currentEvent.getEventName() + "\n\n" +
                "📅 " + formatDisplayDate(currentEvent.getStartDate()) + "\n" +
                "📍 " + currentEvent.getLocation() + "\n\n" +
                currentEvent.getDescription() + "\n\n" +
                "Đặt vé ngay! 🎫";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentEvent.getEventName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        Intent chooser = Intent.createChooser(shareIntent, "Chia sẻ sự kiện qua");
        startActivity(chooser);
    }

    private void setupRecyclerViews() {
        guestAdapter = new GuestDetailAdapter(this);
        rvGuests.setLayoutManager(new LinearLayoutManager(this));
        rvGuests.setAdapter(guestAdapter);
        rvGuests.setNestedScrollingEnabled(false);

        ticketAdapter = new TicketDetailAdapter(this);
        rvTicketTypes.setLayoutManager(new LinearLayoutManager(this));
        rvTicketTypes.setAdapter(ticketAdapter);
        rvTicketTypes.setNestedScrollingEnabled(false);

        // Setup review RecyclerView
        reviewAdapter = new ReviewAdapter(this, this);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);

        // Setup write review button
        btnWriteReview.setOnClickListener(v -> showWriteReviewDialog());
    }

    private void observeData(int eventId) {

        eventViewModel.getEventWithTickets(eventId).observe(this, eventWithTickets -> {
            if (eventWithTickets == null) return;

            Event event = eventWithTickets.getEvent();
            currentEvent = event; // Save for sharing
            List<TicketType> tickets = eventWithTickets.getTickets();

            populateEventDetails(event);
            ticketAdapter.updateData(tickets);
            updateLowestPrice(tickets);
        });

        // thông tin Khách mời
        eventViewModel.getEventWithGuests(eventId).observe(this, eventWithGuests -> {
            if (eventWithGuests == null) return;

            List<Guest> guests = eventWithGuests.guests;
            populateGuests(guests);
        });
    }

    private void populateEventDetails(Event event) {
        tvEventName.setText(event.getEventName());
        tvEventLocation.setText(event.getLocation());
        tvEventDescription.setText(event.getDescription());

        // Gán địa chỉ sự kiện cho biến eventAddress
        eventAddress = event.getLocation();

        Glide.with(this)
                .load(event.getBannerUrl())
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.warning)
                .into(ivEventBanner);

        tvEventDate.setText(formatDisplayDate(event.getStartDate()));

        Glide.with(this)
                .load(event.getBannerUrl())
                .centerCrop()
                .placeholder(R.drawable.loading)
                .into(imgVideoThumbnail);

        setupVideoPlayer(event.getVideoUrl());

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            fetchWeatherForLocation(event.getLocation());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Chuyển đổi địa chỉ String thành Tọa độ (Geocoding)
        LatLng eventLocation = getLocationFromAddress(eventAddress);

        if (eventLocation != null) {
            mMap.addMarker(new MarkerOptions().position(eventLocation).title("Vị trí sự kiện"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15f));
        } else {
            Toast.makeText(this, "Không thể tìm thấy vị trí sự kiện trên bản đồ.", Toast.LENGTH_LONG).show();
        }
    }

    private LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this, Locale.getDefault());
        List<android.location.Address> address;
        LatLng p1 = null;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.isEmpty()) {
                return null;
            }
            android.location.Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Lỗi geocoding: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return p1;
    }

    private void fetchWeatherForLocation(String location) {
        String city = location;
        if (location.contains(",")) {
            String[] parts = location.split(",");
            city = parts[parts.length - 1].trim();
        }

        weatherService.getWeatherForLocation(city, new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                displayWeather(weatherInfo);
            }

            @Override
            public void onError(String error) {
                if (llWeatherSection != null) {
                    llWeatherSection.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayWeather(WeatherInfo weather) {
        if (llWeatherSection == null) return;

        llWeatherSection.setVisibility(View.VISIBLE);

        tvWeatherTemp.setText(String.format(Locale.getDefault(), "%.0f°C", weather.getTemperature()));
        tvWeatherDesc.setText(weather.getDescription());
        tvWeatherHumidity.setText(String.format(Locale.getDefault(), "Độ ẩm: %d%%", weather.getHumidity()));
        tvWeatherWind.setText(String.format(Locale.getDefault(), "Gió: %.1f m/s", weather.getWindSpeed()));

        if (weather.isOutdoorFriendly()) {
            tvWeatherAdvice.setText("✓ Thời tiết phù hợp cho sự kiện ngoài trời");
            tvWeatherAdvice.setTextColor(getResources().getColor(R.color.success, null));
        } else {
            tvWeatherAdvice.setText("⚠ Nên chuẩn bị ô/áo mưa");
            tvWeatherAdvice.setTextColor(getResources().getColor(R.color.warning, null));
        }

        String condition = weather.getCondition();
        if (condition != null) {
            if (condition.toLowerCase().contains("rain")) {
                ivWeatherIcon.setImageResource(R.drawable.ic_weather_rain);
            } else if (condition.toLowerCase().contains("cloud")) {
                ivWeatherIcon.setImageResource(R.drawable.ic_weather_cloud);
            } else {
                ivWeatherIcon.setImageResource(R.drawable.ic_weather_sun);
            }
        }
    }

    private void setupVideoPlayer(String videoUrl) {
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoTitleView.setVisibility(View.GONE);
            videoCardView.setVisibility(View.GONE);
            return;
        }

        videoTitleView.setVisibility(View.VISIBLE);
        videoCardView.setVisibility(View.VISIBLE);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoViewEvent);
        videoViewEvent.setMediaController(mediaController);

        videoViewEvent.setVideoPath(videoUrl);

        btnPlayVideo.setOnClickListener(v -> {
            layoutVideoThumbnail.setVisibility(View.GONE);
            videoViewEvent.setVisibility(View.VISIBLE);
            videoViewEvent.requestFocus();
            videoViewEvent.start();
        });

        videoViewEvent.setOnCompletionListener(mp -> {
            videoViewEvent.setVisibility(View.GONE);
            layoutVideoThumbnail.setVisibility(View.VISIBLE);
        });

        videoViewEvent.setOnErrorListener((mp, what, extra) -> {
            videoViewEvent.setVisibility(View.GONE);
            layoutVideoThumbnail.setVisibility(View.VISIBLE);
            return true;
        });
    }

    private void populateGuests(List<Guest> guests) {
        if (guests != null && !guests.isEmpty()) {
            llGuestSection.setVisibility(View.VISIBLE);
            guestAdapter.updateData(guests);
        } else {
            llGuestSection.setVisibility(View.GONE);
        }
    }

    private void updateLowestPrice(List<TicketType> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            tvLowestPrice.setText("Hết vé");
            btnBuyTicket.setEnabled(false);
            btnBuyTicket.setText("Đã hết");
            return;
        }

        double minPrice = Double.MAX_VALUE;
        boolean hasAvailableTickets = false;

        for (TicketType ticket : tickets) {
            if (ticket.getQuantity() > 0) {
                hasAvailableTickets = true;
                if (ticket.getPrice() < minPrice) {
                    minPrice = ticket.getPrice();
                }
            }
        }
        if (!hasAvailableTickets) {
            tvLowestPrice.setText("Hết vé");
            btnBuyTicket.setEnabled(false);
            btnBuyTicket.setText("Đã hết");
        } else if (minPrice == 0) {
            tvLowestPrice.setText("Miễn phí");
        } else {
            tvLowestPrice.setText(currencyFormatter.format(minPrice));
        }
    }

    private String formatDisplayDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Chưa cập nhật";
        }
        try {
            Date date = dbDateFormat.parse(dateString);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    private void loadReviews(int eventId) {
        // Load reviews list
        reviewViewModel.getReviewsByEvent(eventId).observe(this, reviews -> {
            if (reviews != null) {
                reviewAdapter.setReviews(reviews);
            }
        });

        // Load average rating
        reviewViewModel.getAverageRating(eventId).observe(this, avgRating -> {
            if (avgRating != null && ratingBarAvg != null) {
                ratingBarAvg.setRating(avgRating);
            }
        });

        // Load review count
        reviewViewModel.getReviewCount(eventId).observe(this, count -> {
            if (count != null && tvReviewCount != null) {
                tvReviewCount.setText(String.format(Locale.getDefault(), "(%d đánh giá)", count));
            }
        });
    }

    private void showWriteReviewDialog() {
        if (currentUserId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user already reviewed this event
        reviewViewModel.checkUserReviewed(currentEventId, currentUserId, existingReview -> {
            runOnUiThread(() -> {
                if (existingReview != null) {
                    showEditReviewDialog(existingReview);
                } else {
                    showNewReviewDialog();
                }
            });
        });
    }

    private void showNewReviewDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_write_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        EditText etComment = dialogView.findViewById(R.id.et_comment);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Gửi đánh giá", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();

                    if (rating == 0) {
                        Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date());

                    Review review = new Review(currentEventId, currentUserId, rating, comment, currentDateTime);

                    reviewViewModel.insert(review, reviewId -> {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đánh giá của bạn đã được gửi!", Toast.LENGTH_SHORT).show();
                            loadReviews(currentEventId);
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditReviewDialog(Review existingReview) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_write_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_bar);
        EditText etComment = dialogView.findViewById(R.id.et_comment);

        ratingBar.setRating(existingReview.getRating());
        etComment.setText(existingReview.getComment());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chỉnh sửa đánh giá")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString().trim();

                    if (rating == 0) {
                        Toast.makeText(this, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(new Date());

                    existingReview.setRating(rating);
                    existingReview.setComment(comment);
                    existingReview.setUpdatedAt(currentDateTime);

                    reviewViewModel.update(existingReview);
                    Toast.makeText(this, "Đánh giá đã được cập nhật!", Toast.LENGTH_SHORT).show();
                    loadReviews(currentEventId);
                })
                .setNegativeButton("Hủy", null)
                .setNeutralButton("Xóa", (dialog, which) -> {
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Xác nhận xóa")
                            .setMessage("Bạn có chắc muốn xóa đánh giá này?")
                            .setPositiveButton("Xóa", (d, w) -> {
                                reviewViewModel.delete(existingReview);
                                Toast.makeText(this, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                                loadReviews(currentEventId);
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                })
                .show();
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            Date date = dbDateFormat.parse(dateString);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }
}
