Người tổ chức sự kiện (Organizer App hoặc Module quản trị)
 I. Tính năng cơ bản
 ✅ 1.1 Đăng ký / Xác minh tài khoản tổ chức sự kiện.
 ✅ 1.2 Tạo sự kiện mới: nhập thông tin (tên, mô tả, nghệ sĩ, thể loại, thời gian, địa điểm, giá vé).
 ✅ 1.3 Quản lý vé: số lượng vé, loại vé, giá vé.
 ✅ 1.4 Xem danh sách vé đã bán & tình trạng sự kiện.
 ✅ 1.5 Quản lý hồ sơ cá nhân/doanh nghiệp tổ chức.
 II. Tính năng trung bình
 ✅ 2.1 Chỉnh sửa / cập nhật thông tin sự kiện đã tạo.
 ✅ 2.2 Import/export người tham dự sự kiện và sinh vé tự động
 ✅ 2.3 Quản lý khách tham gia: danh sách người mua vé, xuất file Excel/PDF.
 ✅ 2.4 Gửi thông báo đến toàn bộ người tham gia (event broadcast).
 ✅ 2.5 Quản lý nhiều sự kiện cùng lúc

 I. Cơ bản: Chức năng cốt lõi, CRUD + trải nghiệm cơ bản
 ✅ 1 Tìm Kiếm Sự Kiện (theo tên nghệ sĩ, chương trình, thể loại, ngày).
 ✅ 2 Xem Chi Tiết Sự Kiện (nghệ sĩ, địa điểm, thời gian, giá vé, mô tả)
 ✅ 3 Đặt Vé Trực Tuyến (chọn ngày, suất diễn, loại vé).
 ✅ 4 Tạo Hồ Sơ Người Dùng (quản lý tài khoản, lịch sử sự kiện).
 ✅ 5 Xác Minh Vé Điện Tử (mã QR/mã vạch).
 ✅ 6 Đánh Giá và Phản Hồi (viết review sau sự kiện).
 Trung bình: Chức năng tiện ích, nâng trải nghiệm người dùng
 ✅ 7 Chọn Vị Trí Ngồi (bản đồ ghế, chọn vị trí theo hạng vé).
 ✅ 8 Tích Hợp Thanh Toán Đa Dạng (thẻ tín dụng, ví điện tử, thanh toán địa phương).
 ⏳ 9 Nhắc Nhở và Thông Báo Sự Kiện (reminder, cập nhật thay đổi). [Cần Firebase Cloud Messaging]
 ✅ 10 Chia Sẻ Sự Kiện với Bạn Bè (qua mạng xã hội, mời bạn bè).
 ✅ 11 Xem Video Giới Thiệu Sự Kiện (video/clip nghệ sĩ).
 ✅ 12 Theo Dõi Nghệ Sĩ & Thông Báo Sự Kiện Mới.
 ✅ 13 Chế Độ Đặt Vé Tập Thể (group booking, chia sẻ thanh toán).
 ✅ 14 Cập Nhật Thông Tin Giao Thông & Địa Điểm Lân Cận (map, nhà hàng, café).
 ✅ 15 Chế Độ Ưu Đãi Đặc Biệt (giảm giá vé sớm, ưu đãi thành viên).
 ✅ 16 Bảng Tin Sự Kiện Hot & Tin Tức Liên Quan.
 ✅ 17 Dự Báo Thời Tiết Cho Sự Kiện Ngoài Trời.

 code javaa android và roomDatabase là chính.

=== TIẾN ĐỘ ===
ORGANIZER: 10/10 ✅ (100%)
USER CƠ BẢN: 6/6 ✅ (100%)
USER TRUNG BÌNH: 10/11 ✅ (91%)

TỔNG: 26/27 tính năng hoàn thành (96%)

Còn lại: #9 Nhắc Nhở và Thông Báo Sự Kiện (cần Firebase Cloud Messaging)

=== CÁC FILE ĐÃ TẠO MỚI ===
Entities: Review.java, FollowedArtist.java, Discount.java, WeatherInfo.java
DAOs: ReviewDAO.java, FollowedArtistDAO.java, DiscountDAO.java
Services: WeatherService.java
Activities: BookTicketActivity.java, PaymentActivity.java, MyTicketsActivity.java, SeatSelectionActivity.java, GroupBookingActivity.java
Layouts: 15+ layouts mới (seat selection, group booking, weather section)
Drawables: 15+ icons mới (weather icons, seat states, etc.)
