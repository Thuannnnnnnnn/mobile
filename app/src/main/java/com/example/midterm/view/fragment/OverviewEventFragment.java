package com.example.midterm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.midterm.R;
import com.example.midterm.model.entity.Event;
import com.example.midterm.model.entity.Guest;
import com.example.midterm.viewModel.EventViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class OverviewEventFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";
    private int eventId;

    private EventViewModel eventViewModel;
    // Views
    private ImageView imgEventBanner;
    private TextView tvEventName, tvCategory, tvGuest, tvDescription;
    private TextView tvTimeStart, tvTimeEnd, tvLocation;

    private View videoTitleView;
    private View videoCardView;
    private VideoView videoViewEvent;
    private FrameLayout layoutVideoThumbnail;
    private ImageView imgVideoThumbnail;
    private ImageButton btnPlayVideo;

    /*** Phương thức Factory để tạo Fragment và truyền event*/
    public static OverviewEventFragment newInstance(int eventId) {
        OverviewEventFragment fragment = new OverviewEventFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initViewModels();
        loadEventAndGuestsData();
    }

    private void initViews(View view) {
        imgEventBanner = view.findViewById(R.id.img_event_banner);
        tvEventName = view.findViewById(R.id.tv_event_name);
        tvCategory = view.findViewById(R.id.tv_category);
        tvGuest = view.findViewById(R.id.tv_guest);
        tvDescription = view.findViewById(R.id.tv_description);
        tvTimeStart = view.findViewById(R.id.tv_time_start);
        tvTimeEnd = view.findViewById(R.id.tv_time_end);
        tvLocation = view.findViewById(R.id.tv_location);

        videoTitleView = view.findViewById(R.id.tv_video_title);
        videoCardView = view.findViewById(R.id.card_video_player);
        videoViewEvent = view.findViewById(R.id.video_view_event);
        layoutVideoThumbnail = view.findViewById(R.id.layout_video_thumbnail);
        imgVideoThumbnail = view.findViewById(R.id.img_video_thumbnail);
        btnPlayVideo = view.findViewById(R.id.btn_play_video);
    }

    private void initViewModels() {
        // Dùng requireActivity() để chia sẻ ViewModel với Activity cha
        eventViewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
    }

    private void loadEventAndGuestsData() {
        // Gọi hàm Transaction từ EventViewModel
        eventViewModel.getEventWithGuests(eventId).observe(getViewLifecycleOwner(), eventWithGuests -> {
            if (eventWithGuests == null || eventWithGuests.event == null) {
                return;
            }
            Event event = eventWithGuests.event;
            //Cập nhật thông tin Event
            Glide.with(this)
                    .load(event.getBannerUrl())
                    .placeholder(R.drawable.unnamed_removebg_preview)
                    .into(imgEventBanner);

            tvEventName.setText(event.getEventName());
            tvCategory.setText(event.getGenre());
            tvDescription.setText(event.getDescription());
            tvTimeStart.setText(event.getStartDate());
            tvTimeEnd.setText(event.getEndDate());
            tvLocation.setText(event.getLocation());

            //Cập nhật danh sách Khách mời (từ cùng 1 object)
            if (eventWithGuests.guests != null && !eventWithGuests.guests.isEmpty()) {
                List<Guest> guests = eventWithGuests.guests;
                // Ghép tên các khách mời
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    List<String> guestNames = guests.stream().map(Guest::getName).collect(Collectors.toList());
                    tvGuest.setText(String.join(", ", guestNames));
                } else {
                    StringBuilder names = new StringBuilder();
                    for(int i=0; i < guests.size(); i++) {
                        names.append(guests.get(i).getName());
                        if(i < guests.size() - 1) names.append(", ");
                    }
                    tvGuest.setText(names.toString());
                }
            } else {
                tvGuest.setText("Không có khách mời");
            }

            Glide.with(this)
                    .load(event.getBannerUrl())
                    .into(imgVideoThumbnail);
            setupVideoPlayer(event.getVideoUrl());
        });
    }

    /*** Cài đặt trình phát video với URL từ Firebase*/
    private void setupVideoPlayer(String videoUrl) {
        // Kiểm tra nếu không có URL, ẩn toàn bộ phần video
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoTitleView.setVisibility(View.GONE);
            videoCardView.setVisibility(View.GONE);
            return;
        }

        videoTitleView.setVisibility(View.VISIBLE);
        videoCardView.setVisibility(View.VISIBLE);

        // Chuẩn bị MediaController
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoViewEvent);
        videoViewEvent.setMediaController(mediaController);

        // Đặt đường dẫn Video (dùng setVideoPath cho URL)
        videoViewEvent.setVideoPath(videoUrl);

        // Sự kiện khi nhấn nút Play (trên thumbnail)
        btnPlayVideo.setOnClickListener(v -> {
            layoutVideoThumbnail.setVisibility(View.GONE); // Ẩn thumbnail
            videoViewEvent.setVisibility(View.VISIBLE); // Hiện VideoView
            videoViewEvent.requestFocus();
            videoViewEvent.start(); // Bắt đầu phát
        });

        //  Khi video phát xong, hiện lại thumbnail
        videoViewEvent.setOnCompletionListener(mp -> {
            videoViewEvent.setVisibility(View.GONE);
            layoutVideoThumbnail.setVisibility(View.VISIBLE);
        });

        // Xử lý lỗi
        videoViewEvent.setOnErrorListener((mp, what, extra) -> {
            // Có lỗi xảy ra, quay lại thumbnail
            videoViewEvent.setVisibility(View.GONE);
            layoutVideoThumbnail.setVisibility(View.VISIBLE);
            return true;
        });
    }
}