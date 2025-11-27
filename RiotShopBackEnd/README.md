# RiotShop Backend API (C# ASP.NET Core)

Backend API cho ứng dụng Android RiotShop, được xây dựng bằng ASP.NET Core 8.0 và kết nối với MySQL.

## Yêu cầu

- .NET 8.0 SDK
- MySQL Server
- Visual Studio 2022 hoặc Visual Studio Code

## Cài đặt

### 1. Cấu hình Database

1. Tạo database `ecommerce_acc_shop` trong MySQL
2. Chạy file SQL schema được cung cấp để tạo các bảng
3. Cập nhật connection string trong `appsettings.json`:

```json
"ConnectionStrings": {
  "DefaultConnection": "Server=localhost;Database=ecommerce_acc_shop;User=root;Password=your_password_here;Port=3306;Character Set=utf8mb4;"
}
```

### 2. Restore Dependencies

```bash
dotnet restore
```

### 3. Chạy ứng dụng

```bash
dotnet run
```

API sẽ chạy tại:
- HTTP: `http://localhost:5000`
- HTTPS: `https://localhost:5001`
- Swagger UI: `http://localhost:5000/swagger`

## API Endpoints

### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/register` - Đăng ký

### Products
- `GET /api/products` - Lấy danh sách sản phẩm (có query params: gameId, isFeatured, search)
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm
- `GET /api/products/games` - Lấy danh sách games

### Orders
- `GET /api/orders/user/{userId}` - Lấy đơn hàng của user
- `POST /api/orders` - Tạo đơn hàng mới
- `PUT /api/orders/{id}/status` - Cập nhật trạng thái đơn hàng
- `POST /api/orders/{id}/cancel` - Hủy đơn hàng

### Reviews
- `GET /api/reviews/template/{templateId}` - Lấy reviews của sản phẩm
- `POST /api/reviews` - Tạo review mới

### Wishlist
- `GET /api/wishlist/user/{userId}` - Lấy wishlist của user
- `POST /api/wishlist` - Thêm vào wishlist
- `DELETE /api/wishlist/{id}` - Xóa khỏi wishlist

### Users
- `GET /api/users/{id}` - Lấy thông tin user
- `PUT /api/users/{id}` - Cập nhật thông tin user

## CORS Configuration

Backend đã được cấu hình CORS để cho phép Android app kết nối:
- Development: Cho phép tất cả origins (`*`)
- Production: Nên chỉ cho phép domain của Android app

## Kết nối với Android App

### Android Emulator
- URL: `http://10.0.2.2:5000/api/`
- Đã được cấu hình trong `Constants.java`

### Điện thoại thật (cùng WiFi)
- URL: `http://192.168.1.5:5000/api/` (thay IP bằng IP máy tính của bạn)
- Lấy IP: Chạy `ipconfig` (Windows) hoặc `ifconfig` (Linux/Mac)

## Cấu trúc Project

```
RiotShopBackEnd/
├── Controllers/          # API Controllers
├── Models/              # Entity models
├── DTOs/                # Data Transfer Objects
├── Services/            # Business logic services
├── Data/                # DbContext
├── appsettings.json     # Configuration
└── Program.cs           # Entry point
```

## Notes

- Password được hash bằng SHA-256 (như schema MySQL gốc)
- Tất cả API responses đều có format `ApiResponse<T>`
- Backend hỗ trợ Swagger UI để test API trong development
