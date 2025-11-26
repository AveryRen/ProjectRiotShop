# 📱 PHÂN TÍCH DỰ ÁN RIOTSHOP

## 🎯 MỤC ĐÍCH CỦA ỨNG DỤNG

**Riotshop** là một ứng dụng Android dùng để **mua bán tài khoản game** (có vẻ là League of Legends - Riot Games). Ứng dụng cho phép:
- Người dùng xem, tìm kiếm và mua tài khoản game
- Quản lý giỏ hàng và thanh toán
- Đánh giá sản phẩm
- Quản trị viên quản lý sản phẩm và đơn hàng

---

## 📂 CẤU TRÚC DỰ ÁN

### 1. **KIẾN TRÚC TỔNG QUAN**

```
riotshop/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/riotshop/
│   │   │   ├── ui/              # Giao diện người dùng
│   │   │   ├── models/          # Các model dữ liệu
│   │   │   ├── adapters/        # RecyclerView Adapters
│   │   │   ├── data/            # Database (SQLite)
│   │   │   └── utils/           # Tiện ích hỗ trợ
│   │   └── res/                 # Tài nguyên (layouts, drawables, strings)
│   └── build.gradle.kts         # Dependencies
└── settings.gradle.kts
```

### 2. **CÁC MODULE CHÍNH**

#### A. **Authentication (Xác thực)**
- `LoginActivity.java` - Màn hình đăng nhập
- `SignupActivity.java` - Màn hình đăng ký
- `ForgotPasswordActivity.java` - Quên mật khẩu

#### B. **Home & Products (Trang chủ & Sản phẩm)**
- `HomeActivity.java` - Trang chủ hiển thị danh sách tài khoản
- `SearchActivity.java` - Tìm kiếm sản phẩm
- `DetailActivity.java` - Chi tiết sản phẩm
- `FilterBottomSheet.java` - Lọc theo giá, danh mục

#### C. **Cart & Checkout (Giỏ hàng & Thanh toán)**
- `CartActivity.java` - Quản lý giỏ hàng
- `CheckoutActivity.java` - Thanh toán

#### D. **User Features (Tính năng người dùng)**
- `FavoriteActivity.java` - Danh sách yêu thích
- `ProfileActivity.java` - Thông tin cá nhân
- `OrderHistoryActivity.java` - Lịch sử đơn hàng
- `AddressActivity.java` - Quản lý địa chỉ

#### E. **Admin Features (Tính năng quản trị)**
- `AdminDashboardActivity.java` - Bảng điều khiển admin
- `AdminProductListActivity.java` - Quản lý sản phẩm
- `AdminOrderListActivity.java` - Quản lý đơn hàng

#### F. **Comments (Đánh giá)**
- `CommentActivity.java` - Xem đánh giá
- `AddCommentActivity.java` - Thêm đánh giá

---

## ⚙️ CHỨC NĂNG ĐANG XỬ LÝ

### ✅ **ĐÃ HOÀN THÀNH (Có UI nhưng chưa có backend thực)**

1. **Database Structure (SQLite)**
   - Bảng `users` - Lưu thông tin người dùng
   - Bảng `accounts` - Lưu thông tin tài khoản game (sản phẩm)
   - Bảng `comments` - Lưu đánh giá/comment
   - Bảng `favorites` - Lưu danh sách yêu thích
   - Bảng `cart` - Lưu giỏ hàng

2. **Hiển thị sản phẩm**
   - Grid layout hiển thị danh sách tài khoản
   - Lọc theo danh mục (VIP, Smurf, Giá rẻ...)
   - Lọc theo giá (min/max)
   - Tìm kiếm theo tên

3. **Chi tiết sản phẩm**
   - Hiển thị thông tin: tên, giá, rank, số skins, số champions
   - Thêm vào giỏ hàng
   - Sản phẩm liên quan

4. **Giỏ hàng**
   - Thêm/xóa sản phẩm
   - Cập nhật số lượng
   - Tính tổng tiền

5. **Shared Preferences**
   - Lưu thông tin đăng nhập (username)
   - Quản lý session

### ⚠️ **CHƯA HOÀN THÀNH (Đang dùng Mock Data/Placeholder)**

1. **Authentication**
   - ❌ Chưa có logic đăng nhập thực tế (chỉ Toast rồi chuyển màn hình)
   - ❌ Chưa có logic đăng ký thực tế
   - ❌ Chưa có logic quên mật khẩu
   - ❌ Chưa có validation email/password
   - ❌ Chưa có hash password

2. **User Management**
   - ❌ User ID đang hardcode là "1" ở nhiều nơi
   - ❌ Chưa lưu thông tin user vào database khi đăng ký
   - ❌ Chưa xác thực user khi đăng nhập

3. **Database Operations**
   - ❌ Chưa có dữ liệu mẫu (sample data) trong database
   - ❌ Chưa có method thêm User vào database
   - ❌ Method `cursorToComment()` chưa được implement đầy đủ
   - ❌ Chưa có method quản lý Cart trong DataSource
   - ❌ Chưa có method quản lý Orders

4. **Cart & Checkout**
   - ❌ Cart đang dùng Mock Data (hardcode trong `CartActivity.java`)
   - ❌ Chưa có logic lưu vào database khi thêm vào giỏ hàng
   - ❌ Chưa có logic thanh toán thực tế

5. **Payment & Orders**
   - ❌ Chưa có tích hợp cổng thanh toán
   - ❌ Chưa có logic tạo đơn hàng
   - ❌ Chưa có logic cập nhật trạng thái đơn hàng

6. **Backend API**
   - ❌ Chưa có Retrofit/Volley để gọi API
   - ❌ Chưa có base URL cho API
   - ❌ Tất cả đang làm việc offline với SQLite local

---

## 🔧 NHỮNG GÌ CẦN BỔ SUNG

### 🚨 **ƯU TIÊN CAO (Core Features)**

#### 1. **Hoàn thiện Authentication**

**Cần làm:**
- Implement logic đăng ký: validate email, password; hash password; lưu vào database
- Implement logic đăng nhập: kiểm tra email/password; lưu session
- Implement quên mật khẩu: gửi email reset (cần backend)
- Lưu User ID thực tế vào SharedPreferences sau khi đăng nhập

**Ví dụ cần thêm vào `DataSource.java`:**
```java
// Thêm User vào database
public long addUser(String email, String password, String username) {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_USER_EMAIL, email);
    values.put(DatabaseHelper.COLUMN_USER_PASSWORD, password); // Đã hash
    values.put(DatabaseHelper.COLUMN_USER_NAME, username);
    return database.insert(DatabaseHelper.TABLE_USERS, null, values);
}

// Kiểm tra đăng nhập
public User login(String email, String password) {
    // Query database và so sánh password (đã hash)
    // Trả về User nếu hợp lệ, null nếu không
}
```

#### 2. **Hoàn thiện Database Helper**

**Cần làm:**
- Implement đầy đủ `cursorToComment()` trong `DataSource.java`
- Thêm các method quản lý Cart: `addToCart()`, `removeFromCart()`, `getCartItems()`
- Thêm các method quản lý Orders: `createOrder()`, `getOrders()`, `updateOrderStatus()`
- Thêm method seed data (dữ liệu mẫu) để test

**Ví dụ:**
```java
private Comment cursorToComment(Cursor cursor) {
    return new Comment(
        String.valueOf(cursor.getInt(0)), // commentId
        String.valueOf(cursor.getInt(1)), // accountId
        String.valueOf(cursor.getInt(2)), // userId
        cursor.getString(3), // userName
        null, // userAvatarUrl (chưa có trong DB)
        cursor.getFloat(4), // rating
        cursor.getString(5), // text
        cursor.getLong(6) // timestamp
    );
}
```

#### 3. **User Session Management**

**Cần làm:**
- Hoàn thiện `SharedPrefManager.java`: lưu User ID, Email, Role
- Lấy User ID thực tế từ SharedPreferences thay vì hardcode "1"
- Kiểm tra session khi mở app (nếu đã đăng nhập thì vào Home, chưa thì vào Login)

**Ví dụ trong `HomeActivity.java`:**
```java
// Thay vì:
String currentUserId = "1"; // Placeholder

// Nên là:
String currentUserId = SharedPrefManager.getInstance(this).getUserId();
```

#### 4. **Cart Integration với Database**

**Cần làm:**
- Khi thêm vào giỏ hàng (`DetailActivity`), lưu vào database
- Khi mở `CartActivity`, load từ database thay vì Mock Data
- Khi update quantity hoặc remove, cập nhật database

**Ví dụ method cần thêm vào `DataSource.java`:**
```java
public void addToCart(String userId, String accountId, int quantity) {
    ContentValues values = new ContentValues();
    values.put(DatabaseHelper.COLUMN_USER_ID, userId);
    values.put(DatabaseHelper.COLUMN_ACCOUNT_ID, accountId);
    values.put(DatabaseHelper.COLUMN_CART_QUANTITY, quantity);
    database.insertWithOnConflict(DatabaseHelper.TABLE_CART, null, values, 
        SQLiteDatabase.CONFLICT_REPLACE);
}

public List<CartItem> getCartItems(String userId) {
    // Query và return danh sách CartItem
}
```

### ⚠️ **ƯU TIÊN TRUNG BÌNH (Important Features)**

#### 5. **Backend API Integration**

**Cần làm:**
- Thêm Retrofit/Volley dependency
- Tạo API Service interface
- Tạo Repository pattern để quản lý API calls
- Thêm error handling và loading states

**Ví dụ trong `build.gradle.kts`:**
```kotlin
dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}
```

#### 6. **Payment Integration**

**Cần làm:**
- Tích hợp cổng thanh toán (VNPay, Momo, ZaloPay...)
- Tạo đơn hàng sau khi thanh toán thành công
- Cập nhật trạng thái đơn hàng

#### 7. **Image Management**

**Cần làm:**
- Hiện tại đang dùng `imageResId` (drawable resource)
- Nên chuyển sang URL (Firebase Storage hoặc server)
- Load ảnh từ URL bằng Glide (đã có dependency)

#### 8. **Error Handling & Validation**

**Cần làm:**
- Validate input ở tất cả form
- Hiển thị error messages rõ ràng
- Handle network errors
- Handle database errors

### 📝 **ƯU TIÊN THẤP (Nice to Have)**

#### 9. **Notification System**

**Cần làm:**
- Thông báo khi có đơn hàng mới
- Thông báo khi đơn hàng được xử lý

#### 10. **Advanced Features**

**Cần làm:**
- Pull to refresh
- Infinite scroll/pagination
- Dark mode (đã có values-night nhưng chưa dùng)
- Share sản phẩm
- Push notifications

---

## 📊 TỔNG KẾT

### ✅ **Điểm mạnh:**
- UI/UX đã được thiết kế khá đầy đủ
- Database structure hợp lý
- Code structure rõ ràng, dễ maintain
- Đã có nhiều tính năng UI hoàn chỉnh

### ⚠️ **Điểm yếu:**
- Chưa có backend thực tế (tất cả đang mock)
- Authentication chưa hoạt động
- User session management chưa hoàn chỉnh
- Nhiều chức năng chỉ có UI, chưa có logic

### 🎯 **Đề xuất thứ tự triển khai:**

1. **Bước 1**: Hoàn thiện Authentication (Đăng ký, Đăng nhập)
2. **Bước 2**: User Session Management
3. **Bước 3**: Hoàn thiện Database operations (Cart, Orders, Comments)
4. **Bước 4**: Seed sample data để test
5. **Bước 5**: Tích hợp Backend API (nếu có)
6. **Bước 6**: Payment integration
7. **Bước 7**: Polish UI/UX và error handling

---

## 📝 GHI CHÚ

- File này chỉ phân tích code hiện tại
- Nhiều chức năng cần backend API thực tế mới hoạt động đầy đủ
- SQLite chỉ phù hợp cho prototype, production nên dùng server database

