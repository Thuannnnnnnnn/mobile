package com.example.midterm.view.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.midterm.R;
import com.example.midterm.model.entity.Review;
import com.example.midterm.viewModel.AccountViewModel;
import com.example.midterm.viewModel.UserProfileViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();
    private Context context;
    private UserProfileViewModel userProfileViewModel;
    private LifecycleOwner lifecycleOwner;

    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public ReviewAdapter(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        if (context instanceof ViewModelStoreOwner) {
            userProfileViewModel = new ViewModelProvider((ViewModelStoreOwner) context)
                    .get(UserProfileViewModel.class);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Set rating
        holder.ratingBar.setRating(review.getRating());

        // Set comment
        holder.tvComment.setText(review.getComment());

        // Format and set date
        try {
            Date date = inputFormat.parse(review.getCreatedAt());
            if (date != null) {
                holder.tvDate.setText(outputFormat.format(date));
            }
        } catch (ParseException e) {
            holder.tvDate.setText(review.getCreatedAt());
        }

        // Load user name from UserProfile
        if (userProfileViewModel != null) {
            userProfileViewModel.getUserProfileById(review.getUserId())
                    .observe(lifecycleOwner, userProfile -> {
                        if (userProfile != null && userProfile.getFullName() != null && !userProfile.getFullName().isEmpty()) {
                            holder.tvUserName.setText(userProfile.getFullName());
                        } else {
                            holder.tvUserName.setText("Người dùng #" + review.getUserId());
                        }
                    });
        } else {
            holder.tvUserName.setText("Người dùng #" + review.getUserId());
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvUserName;
        TextView tvDate;
        RatingBar ratingBar;
        TextView tvComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            tvComment = itemView.findViewById(R.id.tv_comment);
        }
    }
}
