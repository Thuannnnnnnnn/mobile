package com.example.midterm.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.midterm.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    // Định nghĩa các loại sắp xếp
    public enum SortType {
        DATE_ASC,
        DATE_DESC,
        NAME_AZ
    }

    // Interface để gửi dữ liệu về Activity
    public interface FilterListener {
        void onFilterApplied(SortType sortType);
    }

    private FilterListener mListener;
    private RadioGroup rgSortOptions;
    private Button btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        rgSortOptions = v.findViewById(R.id.rg_sort_options);
        btnApply = v.findViewById(R.id.btn_apply_filter);

        // TODO: Load các cài đặt lọc hiện tại (nếu có) vào RadioGroup

        btnApply.setOnClickListener(view -> {
            // Xác định tùy chọn Sắp xếp
            SortType selectedSort = SortType.DATE_ASC; // Mặc định
            int selectedId = rgSortOptions.getCheckedRadioButtonId();

            if (selectedId == R.id.rb_sort_date_desc) {
                selectedSort = SortType.DATE_DESC;
            } else if (selectedId == R.id.rb_sort_name_az) {
                selectedSort = SortType.NAME_AZ;
            }

            // Gửi kết quả về Activity
            if (mListener != null) {
                mListener.onFilterApplied(selectedSort);
            }
            dismiss(); // Đóng Bottom Sheet
        });

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Đảm bảo Activity đã implement interface
        if (context instanceof FilterListener) {
            mListener = (FilterListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " phải implement FilterListener");
        }
    }
}
