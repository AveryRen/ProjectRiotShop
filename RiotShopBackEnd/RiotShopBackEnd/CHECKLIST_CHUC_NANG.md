# CHECKLIST CÁC CHỨC NĂNG

## ✅ ĐÃ CÓ

### Authentication
- [x] Login (trả về JWT token)
- [x] Register (trả về JWT token)
- [x] JWT Authentication setup

### Products
- [x] Danh sách sản phẩm (có filter, search, category)
- [x] Chi tiết sản phẩm
- [x] Lọc theo game (category)
- [x] Lọc theo featured
- [x] Tìm kiếm theo tên/mô tả

### Orders
- [x] Lấy đơn hàng của user
- [x] Tạo đơn hàng mới
- [x] Cập nhật trạng thái đơn hàng
- [x] Hủy đơn hàng (có refund status)

### Reviews
- [x] Xem reviews của sản phẩm
- [x] Tạo review mới
- [x] Reviews chờ admin duyệt

### Wishlist
- [x] Xem wishlist
- [x] Thêm vào wishlist
- [x] Xóa khỏi wishlist

### Users
- [x] Xem profile
- [x] Cập nhật profile

## ❌ CHƯA CÓ / CẦN BỔ SUNG

### Cart (Giỏ hàng)
- [ ] Model/Entity cho Cart
- [ ] Xem giỏ hàng
- [ ] Thêm vào giỏ hàng
- [ ] Cập nhật số lượng
- [ ] Xóa khỏi giỏ hàng
- [ ] Xóa toàn bộ giỏ hàng

### Addresses (Địa chỉ)
- [ ] Model/Entity cho UserAddresses (nhiều địa chỉ)
- [ ] Xem danh sách địa chỉ
- [ ] Thêm địa chỉ mới
- [ ] Cập nhật địa chỉ
- [ ] Xóa địa chỉ
- [ ] Đặt địa chỉ mặc định

### Admin APIs
- [ ] Admin dashboard/statistics
- [ ] Quản lý sản phẩm (CRUD)
- [ ] Quản lý orders (xem tất cả, filter)
- [ ] Quản lý reviews (duyệt, xóa)
- [ ] Quản lý users
- [ ] Nạp tiền cho user

### Products - Bổ sung
- [ ] Sản phẩm liên quan (related products)
- [ ] Upload hình ảnh sản phẩm

### Orders - Bổ sung
- [ ] Lý do hủy đơn hàng (cancel reason)
- [ ] Phân quyền: User chỉ xem được đơn hàng của mình

### Reviews - Bổ sung
- [ ] Admin approve/reject reviews
- [ ] User chỉ xem được reviews đã approve

## CẦN PHÂN QUYỀN LẠI

- [x] Products - Public (không cần auth)
- [ ] Orders - User chỉ xem được đơn hàng của mình, Admin xem được tất cả
- [ ] Reviews - User tạo review, Admin duyệt
- [ ] Wishlist - User chỉ xem được wishlist của mình
- [ ] Users - User chỉ sửa được profile của mình
- [ ] Cart - User chỉ xem được cart của mình
- [ ] Addresses - User chỉ quản lý địa chỉ của mình
- [ ] Admin APIs - Chỉ Admin mới truy cập được

## GIAO DIỆN

- [ ] Kiểm tra UI/UX trên Android app
- [ ] Responsive design
- [ ] Material Design components

## NEXT STEPS

1. Tạo Cart API
2. Tạo Addresses API  
3. Tạo Admin Controllers
4. Phân quyền lại tất cả controllers
5. Bổ sung các chức năng còn thiếu
