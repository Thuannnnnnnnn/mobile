package com.example.midterm.model.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.midterm.model.entity.Discount;

import java.util.List;

@Dao
public interface DiscountDAO {
    @Insert
    long insert(Discount discount);

    @Update
    void update(Discount discount);

    @Delete
    void delete(Discount discount);

    // Lấy tất cả discount của 1 sự kiện
    @Query("SELECT * FROM discounts WHERE event_id = :eventId ORDER BY created_at DESC")
    LiveData<List<Discount>> getDiscountsByEvent(int eventId);

    // Lấy discount còn khả dụng
    @Query("SELECT * FROM discounts WHERE event_id = :eventId AND is_active = 1 " +
           "AND start_date <= datetime('now', 'localtime') AND end_date >= datetime('now', 'localtime') " +
           "AND used_count < usage_limit ORDER BY discount_value DESC")
    LiveData<List<Discount>> getActiveDiscounts(int eventId);

    // Lấy discount bằng mã code
    @Query("SELECT * FROM discounts WHERE code = :code AND is_active = 1 LIMIT 1")
    Discount getDiscountByCode(String code);

    // Validate discount code for specific event
    //Kiểm tra mã discount cho sự kiện cụ thể (Tồn tại, đã hết,...)
    @Query("SELECT * FROM discounts WHERE code = :code AND event_id = :eventId AND is_active = 1 " +
           "AND start_date <= datetime('now', 'localtime') AND end_date >= datetime('now', 'localtime') " +
           "AND used_count < usage_limit LIMIT 1")
    Discount validateDiscount(String code, int eventId);

    // Tăng số lượng sửu dủng
    @Query("UPDATE discounts SET used_count = used_count + 1 WHERE id = :discountId")
    void incrementUsedCount(int discountId);

    // Deactivate discount
    @Query("UPDATE discounts SET is_active = 0 WHERE id = :discountId")
    void deactivateDiscount(int discountId);

    // Lấy mã giảm giá by ID
    @Query("SELECT * FROM discounts WHERE id = :discountId")
    LiveData<Discount> getDiscountById(int discountId);

    // Kiểm tra mã discount có tồn tại
    @Query("SELECT COUNT(*) FROM discounts WHERE code = :code")
    int isCodeExists(String code);
}
