# BÁO CÁO DỰ ÁN RIOTSHOP

## I. TỔNG QUAN DỰ ÁN

**Tên dự án:** RiotShop - Ứng dụng thương mại điện tử bán tài khoản game

**Mô tả:** RiotShop là một hệ thống thương mại điện tử hoàn chỉnh được xây dựng để phục vụ việc mua bán tài khoản game, đặc biệt là các tài khoản liên quan đến Riot Games. Dự án bao gồm ứng dụng Android cho người dùng cuối và hệ thống Backend API để quản lý dữ liệu và xử lý nghiệp vụ.

**Công nghệ sử dụng:**
- **Backend:** ASP.NET Core 8.0 (C#), MySQL Database
- **Frontend:** Android Native (Java)
- **Authentication:** JWT (JSON Web Token)
- **API Documentation:** Swagger/OpenAPI
- **Cloud Storage:** Cloudinary (quản lý hình ảnh)
- **Payment:** Stripe.net (tích hợp thanh toán)

---

## II. KIẾN TRÚC HỆ THỐNG

### 2.1. Kiến trúc tổng thể
- **Mô hình:** Client-Server Architecture
- **Giao tiếp:** RESTful API
- **Database:** MySQL với Entity Framework Core
- **Security:** JWT Authentication & Authorization

### 2.2. Cấu trúc Backend (RiotShopBackEnd)
```
RiotShopBackEnd/
├── Controllers/          # API Controllers (11 controllers)
├── Models/              # Entity models (10 models)
├── DTOs/                # Data Transfer Objects
├── Services/            # Business logic services
├── Data/                # DbContext & Database configuration
├── Helpers/             # Utility classes
├── Filters/             # Custom filters
└── Attributes/          # Custom attributes
```

### 2.3. Cấu trúc Frontend (Android App)
```
riotshop/
├── ui/
│   ├── auth/           # Authentication (Login, Signup)
│   ├── home/           # Home screen với products
│   ├── product/        # Product detail
│   ├── cart/           # Shopping cart
│   ├── checkout/       # Checkout & Payment
│   ├── order/          # Order management
│   ├── profile/        # User profile
│   └── admin/          # Admin dashboard
├── models/             # Data models
├── api/                # API service layer
└── utils/              # Utilities & helpers
```

---

## III. CÁC CHỨC NĂNG ĐÃ THỰC HIỆN

### 3.1. Xác thực người dùng (Authentication)
- **Đăng ký tài khoản:** Người dùng có thể tạo tài khoản mới với email và mật khẩu
- **Đăng nhập:** Hệ thống xác thực và cấp JWT token
- **Phân quyền:** Hỗ trợ 2 loại người dùng (User và Admin)
- **Quản lý session:** Lưu trữ token và thông tin người dùng

### 3.2. Quản lý sản phẩm (Products)
- **Danh sách sản phẩm:** Hiển thị tất cả sản phẩm với phân trang
- **Chi tiết sản phẩm:** Xem thông tin chi tiết, hình ảnh, mô tả
- **Tìm kiếm:** Tìm kiếm sản phẩm theo tên hoặc mô tả
- **Lọc sản phẩm:** 
  - Lọc theo danh mục game (Category)
  - Lọc sản phẩm nổi bật (Featured)
- **Sản phẩm liên quan:** Hiển thị các sản phẩm tương tự

### 3.3. Giỏ hàng (Shopping Cart)
- **Xem giỏ hàng:** Hiển thị tất cả sản phẩm đã thêm vào giỏ
- **Thêm vào giỏ hàng:** Thêm sản phẩm với số lượng
- **Cập nhật số lượng:** Tăng/giảm số lượng sản phẩm
- **Xóa sản phẩm:** Xóa sản phẩm khỏi giỏ hàng

### 3.4. Danh sách yêu thích (Wishlist)
- **Xem wishlist:** Danh sách sản phẩm yêu thích
- **Thêm vào wishlist:** Lưu sản phẩm yêu thích
- **Xóa khỏi wishlist:** Bỏ sản phẩm khỏi danh sách yêu thích

### 3.5. Quản lý đơn hàng (Orders)
- **Tạo đơn hàng:** Tạo đơn hàng từ giỏ hàng
- **Lịch sử đơn hàng:** Xem tất cả đơn hàng đã đặt
- **Chi tiết đơn hàng:** Xem thông tin chi tiết đơn hàng
- **Hủy đơn hàng:** Hủy đơn với lý do và xử lý hoàn tiền
- **Trạng thái đơn hàng:** Theo dõi trạng thái đơn hàng

### 3.6. Thanh toán (Payment)
- **Tích hợp thanh toán:** Kết nối với hệ thống thanh toán
- **Xử lý giao dịch:** Lưu trữ thông tin giao dịch thanh toán
- **Quản lý địa chỉ giao hàng:** Chọn địa chỉ nhận hàng

### 3.7. Quản lý địa chỉ (Address Management)
- **Danh sách địa chỉ:** Xem tất cả địa chỉ đã lưu
- **Thêm địa chỉ:** Thêm địa chỉ mới
- **Cập nhật địa chỉ:** Chỉnh sửa thông tin địa chỉ
- **Xóa địa chỉ:** Xóa địa chỉ không còn sử dụng
- **Địa chỉ mặc định:** Đặt địa chỉ mặc định

### 3.8. Đánh giá sản phẩm (Reviews)
- **Xem đánh giá:** Xem tất cả đánh giá của sản phẩm
- **Tạo đánh giá:** Người dùng có thể đánh giá và bình luận
- **Hệ thống rating:** Đánh giá bằng sao (1-5 sao)
- **Duyệt đánh giá:** Admin có thể duyệt/từ chối đánh giá

### 3.9. Quản lý tài khoản (User Profile)
- **Xem thông tin:** Xem thông tin cá nhân
- **Cập nhật thông tin:** Chỉnh sửa thông tin tài khoản
- **Quản lý avatar:** Upload và cập nhật ảnh đại diện

### 3.10. Quản trị viên (Admin Features)
- **Dashboard:** Trang quản trị tổng quan
- **Quản lý sản phẩm:** CRUD sản phẩm
- **Quản lý đơn hàng:** Xem và quản lý tất cả đơn hàng
- **Quản lý người dùng:** Quản lý tài khoản người dùng
- **Duyệt đánh giá:** Phê duyệt hoặc từ chối đánh giá

---

## IV. API ENDPOINTS

### 4.1. Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký

### 4.2. Products
- `GET /api/products` - Danh sách sản phẩm (có query params: gameId, isFeatured, search)
- `GET /api/products/{id}` - Chi tiết sản phẩm
- `GET /api/products/games` - Danh sách games/categories

### 4.3. Cart
- `GET /api/cart/me` - Lấy giỏ hàng của user
- `POST /api/cart` - Thêm vào giỏ hàng
- `PUT /api/cart/{id}` - Cập nhật giỏ hàng
- `DELETE /api/cart/{id}` - Xóa khỏi giỏ hàng

### 4.4. Orders
- `GET /api/orders/me` - Lấy đơn hàng của user
- `GET /api/orders/{id}` - Chi tiết đơn hàng
- `POST /api/orders` - Tạo đơn hàng mới
- `PUT /api/orders/{id}/status` - Cập nhật trạng thái
- `POST /api/orders/{id}/cancel` - Hủy đơn hàng

### 4.5. Reviews
- `GET /api/reviews/template/{templateId}` - Lấy reviews của sản phẩm
- `POST /api/reviews` - Tạo review mới

### 4.6. Wishlist
- `GET /api/wishlist/me` - Lấy wishlist của user
- `POST /api/wishlist` - Thêm vào wishlist
- `DELETE /api/wishlist/{id}` - Xóa khỏi wishlist

### 4.7. Addresses
- `GET /api/addresses/me` - Lấy địa chỉ của user
- `POST /api/addresses` - Thêm địa chỉ mới
- `PUT /api/addresses/{id}` - Cập nhật địa chỉ
- `DELETE /api/addresses/{id}` - Xóa địa chỉ

### 4.8. Users
- `GET /api/users/me` - Lấy thông tin user
- `PUT /api/users/me` - Cập nhật thông tin user

### 4.9. File Upload
- `POST /api/upload/image` - Upload hình ảnh
- `POST /api/upload/avatar` - Upload avatar

---

## V. CƠ SỞ DỮ LIỆU

### 5.1. Các bảng chính
- **Users:** Thông tin người dùng
- **ProductTemplate:** Mẫu sản phẩm
- **InventoryPackage:** Gói hàng tồn kho
- **CartItem:** Giỏ hàng
- **Order:** Đơn hàng
- **PaymentTransaction:** Giao dịch thanh toán
- **Review:** Đánh giá sản phẩm
- **Wishlist:** Danh sách yêu thích
- **UserAddress:** Địa chỉ người dùng
- **AccountDetail:** Chi tiết tài khoản game

### 5.2. Quan hệ dữ liệu
- User 1-N Order (Một user có nhiều đơn hàng)
- User 1-N CartItem (Một user có nhiều item trong giỏ)
- User 1-N Wishlist (Một user có nhiều sản phẩm yêu thích)
- ProductTemplate 1-N InventoryPackage (Một sản phẩm có nhiều gói hàng)
- ProductTemplate 1-N Review (Một sản phẩm có nhiều đánh giá)

---

## VI. BẢO MẬT

### 6.1. Authentication & Authorization
- JWT Token-based authentication
- Password hashing với SHA-256
- Role-based access control (User/Admin)
- Token expiration và validation

### 6.2. API Security
- CORS configuration cho Android app
- Input validation
- SQL injection prevention (Entity Framework)
- XSS protection

### 6.3. Data Protection
- Secure password storage
- Sensitive data encryption
- API response standardization

---

## VII. GIAO DIỆN NGƯỜI DÙNG

### 7.1. Màn hình chính
- **Home Screen:** Danh sách sản phẩm với category filter và search
- **Product Detail:** Chi tiết sản phẩm với hình ảnh, mô tả, đánh giá
- **Cart:** Giỏ hàng với tổng tiền và checkout
- **Checkout:** Form thanh toán và chọn địa chỉ

### 7.2. Navigation
- Bottom Navigation với các tab: Home, Cart, Favorite, Orders, Profile
- Drawer Navigation cho các chức năng bổ sung
- Material Design components

### 7.3. User Experience
- Real-time search
- Pull-to-refresh
- Loading indicators
- Error handling và thông báo
- Responsive layouts

---

## VIII. TÍCH HỢP BÊN THỨ BA

### 8.1. Cloudinary
- Quản lý và lưu trữ hình ảnh sản phẩm
- Upload và optimize hình ảnh
- CDN delivery cho hình ảnh

### 8.2. Stripe
- Tích hợp thanh toán trực tuyến
- Xử lý giao dịch thanh toán
- Quản lý payment methods

---

## IX. TÀI LIỆU VÀ TESTING

### 9.1. API Documentation
- Swagger/OpenAPI documentation
- Interactive API testing interface
- Endpoint descriptions và examples

### 9.2. Code Documentation
- Inline comments
- README files
- Status summary documents

---

## X. KẾT QUẢ ĐẠT ĐƯỢC

### 10.1. Chức năng hoàn thiện
- Hệ thống đăng nhập/đăng ký hoàn chỉnh
- Quản lý sản phẩm đầy đủ
- Giỏ hàng và thanh toán
- Quản lý đơn hàng
- Hệ thống đánh giá
- Quản lý người dùng
- Quản trị viên

### 10.2. Công nghệ áp dụng
- RESTful API architecture
- JWT Authentication
- Entity Framework Core
- Material Design UI
- Cloud storage integration
- Payment gateway integration

### 10.3. Chất lượng code
- Clean code structure
- Separation of concerns
- Reusable components
- Error handling
- API response standardization

---

## XI. HẠN CHẾ VÀ HƯỚNG PHÁT TRIỂN

### 11.1. Hạn chế hiện tại
- Chưa có hệ thống thông báo push notification
- Chưa có tính năng chat hỗ trợ khách hàng
- Chưa có báo cáo thống kê chi tiết cho admin
- Chưa có tính năng đổi/trả hàng

### 11.2. Hướng phát triển
- Thêm hệ thống thông báo real-time
- Tích hợp nhiều phương thức thanh toán hơn
- Phát triển ứng dụng web
- Thêm tính năng khuyến mãi và voucher
- Tối ưu hóa hiệu năng và bảo mật
- Thêm tính năng đa ngôn ngữ

---

## XII. KẾT LUẬN

Dự án RiotShop đã được phát triển thành công với đầy đủ các chức năng cốt lõi của một hệ thống thương mại điện tử. Hệ thống bao gồm:

- **Backend API** hoàn chỉnh với ASP.NET Core 8.0
- **Android Application** với giao diện thân thiện
- **Database** được thiết kế hợp lý với MySQL
- **Security** được đảm bảo với JWT authentication
- **Integration** với các dịch vụ bên thứ ba (Cloudinary, Stripe)

Dự án đã đáp ứng được các yêu cầu cơ bản và có thể mở rộng thêm nhiều tính năng trong tương lai. Code được tổ chức rõ ràng, dễ bảo trì và mở rộng.

---

**Ngày hoàn thành:** [Ngày hiện tại]  
**Sinh viên thực hiện:** [Tên sinh viên]  
**Giảng viên hướng dẫn:** [Tên giảng viên]

