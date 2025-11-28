package com.example.riotshop.api;

import com.example.riotshop.models.ApiResponse;
import com.example.riotshop.models.AuthResponse;
import com.example.riotshop.models.LoginRequest;
import com.example.riotshop.models.ProductTemplate;
import com.example.riotshop.models.RegisterRequest;
import com.example.riotshop.models.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    
    // Auth endpoints
    @POST("Auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);
    
    @POST("Auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);
    
    // Products endpoints
    @GET("Products")
    Call<ApiResponse<List<ProductTemplate>>> getProducts(
            @Query("gameId") Integer gameId,
            @Query("isFeatured") Boolean isFeatured,
            @Query("search") String search
    );
    
    @GET("Products/{id}")
    Call<ApiResponse<ProductTemplate>> getProductById(@Path("id") int id);

    @GET("Products/{id}/available-account")
    Call<ApiResponse<Object>> getAvailableAccount(@Path("id") int templateId);

    @GET("Products/{id}/related")
    Call<ApiResponse<List<ProductTemplate>>> getRelatedProducts(@Path("id") int id, @Query("limit") Integer limit);
    
    @GET("Products/games")
    Call<ApiResponse<List<com.example.riotshop.models.GameType>>> getGames();
    
    // Users endpoints
    @GET("Users/me")
    Call<ApiResponse<UserResponse>> getCurrentUser(@Header("Authorization") String token);
    
    @PUT("Users/me")
    Call<ApiResponse<UserResponse>> updateCurrentUser(@Header("Authorization") String token, @Body com.example.riotshop.models.UpdateUserRequest request);
    
    // Cart endpoints
    @GET("Cart/me")
    Call<ApiResponse<List<com.example.riotshop.models.CartItem>>> getCart(@Header("Authorization") String token);
    
    @POST("Cart")
    Call<ApiResponse<com.example.riotshop.models.CartItem>> addToCart(@Header("Authorization") String token, @Body com.example.riotshop.models.AddToCartRequest request);
    
    @PUT("Cart/{id}")
    Call<ApiResponse<com.example.riotshop.models.CartItem>> updateCartItem(@Header("Authorization") String token, @Path("id") int id, @Body com.example.riotshop.models.UpdateCartItemRequest request);
    
    @DELETE("Cart/{id}")
    Call<ApiResponse<Object>> removeCartItem(@Header("Authorization") String token, @Path("id") int id);
    
    @DELETE("Cart/clear")
    Call<ApiResponse<Object>> clearCart(@Header("Authorization") String token);
    
    // Wishlist endpoints
    @GET("Wishlist/me")
    Call<ApiResponse<List<com.example.riotshop.models.Wishlist>>> getWishlist(@Header("Authorization") String token);
    
    @POST("Wishlist")
    Call<ApiResponse<com.example.riotshop.models.Wishlist>> addToWishlist(@Header("Authorization") String token, @Body com.example.riotshop.models.AddWishlistRequest request);
    
    @DELETE("Wishlist/{id}")
    Call<ApiResponse<Object>> removeFromWishlist(@Header("Authorization") String token, @Path("id") int id);
    
    // Orders endpoints
    @GET("Orders/me")
    Call<ApiResponse<List<com.example.riotshop.models.Order>>> getMyOrders(@Header("Authorization") String token);
    
    @GET("Orders/me/purchased")
    Call<ApiResponse<List<Object>>> getMyPurchasedAccounts(@Header("Authorization") String token);
    
    @GET("Orders/{id}")
    Call<ApiResponse<Object>> getOrderById(@Header("Authorization") String token, @Path("id") int id);
    
    @POST("Orders")
    Call<ApiResponse<com.example.riotshop.models.Order>> createOrder(@Header("Authorization") String token, @Body com.example.riotshop.models.CreateOrderRequest request);
    
    @POST("Orders/{id}/cancel")
    Call<ApiResponse<com.example.riotshop.models.Order>> cancelOrder(@Header("Authorization") String token, @Path("id") int id, @Body com.example.riotshop.models.CancelOrderRequest request);
    
    // Reviews endpoints
    @GET("Reviews/template/{templateId}")
    Call<ApiResponse<List<com.example.riotshop.models.Review>>> getReviewsByTemplate(@Path("templateId") int templateId);
    
    @POST("Reviews")
    Call<ApiResponse<com.example.riotshop.models.Review>> createReview(@Header("Authorization") String token, @Body com.example.riotshop.models.CreateReviewRequest request);
    
    // Addresses endpoints
    @GET("Addresses/me")
    Call<ApiResponse<List<com.example.riotshop.models.UserAddress>>> getMyAddresses(@Header("Authorization") String token);
    
    @POST("Addresses")
    Call<ApiResponse<com.example.riotshop.models.UserAddress>> createAddress(@Header("Authorization") String token, @Body com.example.riotshop.models.CreateAddressRequest request);
    
    @PUT("Addresses/{id}")
    Call<ApiResponse<com.example.riotshop.models.UserAddress>> updateAddress(@Header("Authorization") String token, @Path("id") int id, @Body com.example.riotshop.models.UpdateAddressRequest request);
    
    @DELETE("Addresses/{id}")
    Call<ApiResponse<Object>> deleteAddress(@Header("Authorization") String token, @Path("id") int id);
    
    @PUT("Addresses/{id}/default")
    Call<ApiResponse<com.example.riotshop.models.UserAddress>> setDefaultAddress(@Header("Authorization") String token, @Path("id") int id);
    
    // Admin endpoints
    @GET("admin/orders")
    Call<ApiResponse<List<Object>>> getAdminOrders(
            @Header("Authorization") String token,
            @Query("status") String status,
            @Query("userId") Integer userId,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );
    
    @GET("admin/orders/{id}")
    Call<ApiResponse<Object>> getAdminOrderById(@Header("Authorization") String token, @Path("id") int id);
    
    @GET("admin/users")
    Call<ApiResponse<List<UserResponse>>> getAdminUsers(
            @Header("Authorization") String token,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );
    
    @GET("admin/statistics")
    Call<ApiResponse<Object>> getAdminStatistics(@Header("Authorization") String token);
    
    @POST("admin/products")
    Call<ApiResponse<ProductTemplate>> createProduct(@Header("Authorization") String token, @Body com.example.riotshop.models.CreateProductRequest request);
    
    @PUT("admin/products/{id}")
    Call<ApiResponse<ProductTemplate>> updateProduct(@Header("Authorization") String token, @Path("id") int id, @Body com.example.riotshop.models.UpdateProductRequest request);
    
    @DELETE("admin/products/{id}")
    Call<ApiResponse<Object>> deleteProduct(@Header("Authorization") String token, @Path("id") int id);
    
    // Admin Users CRUD
    @POST("admin/users")
    Call<ApiResponse<Object>> createUser(@Header("Authorization") String token, @Body com.example.riotshop.models.CreateUserRequest request);
    
    @PUT("admin/users/{id}")
    Call<ApiResponse<Object>> updateUser(@Header("Authorization") String token, @Path("id") int id, @Body com.example.riotshop.models.UpdateUserAdminRequest request);
    
    @DELETE("admin/users/{id}")
    Call<ApiResponse<Object>> deleteUser(@Header("Authorization") String token, @Path("id") int id);
    
    @PUT("admin/users/{id}/toggle-admin")
    Call<ApiResponse<UserResponse>> toggleAdmin(@Header("Authorization") String token, @Path("id") int id);
    
    // Admin Orders
    @DELETE("admin/orders/{id}")
    Call<ApiResponse<Object>> deleteOrder(@Header("Authorization") String token, @Path("id") int id);
    
    // Admin Account Details (tài khoản trong sản phẩm)
    @GET("admin/products/{templateId}/accounts")
    Call<ApiResponse<List<Object>>> getProductAccounts(@Header("Authorization") String token, @Path("templateId") int templateId);
    
    @POST("admin/products/{templateId}/accounts")
    Call<ApiResponse<Object>> createAccount(@Header("Authorization") String token, @Path("templateId") int templateId, @Body com.example.riotshop.models.CreateAccountRequest request);
    
    @PUT("admin/accounts/{id}")
    Call<ApiResponse<Object>> updateAccount(@Header("Authorization") String token, @Path("id") int id, @Body com.example.riotshop.models.UpdateAccountRequest request);
    
    @DELETE("admin/accounts/{id}")
    Call<ApiResponse<Object>> deleteAccount(@Header("Authorization") String token, @Path("id") int id);
    
    @POST("admin/products/{templateId}/accounts/bulk")
    Call<ApiResponse<Object>> bulkCreateAccounts(@Header("Authorization") String token, @Path("templateId") int templateId, @Body com.example.riotshop.models.BulkCreateAccountsRequest request);
    
    // File Upload
    @POST("fileupload/image")
    Call<ApiResponse<Object>> uploadImage(@Header("Authorization") String token, @Body okhttp3.RequestBody file);

    // Multipart file upload
    @POST("fileupload/image")
    okhttp3.Call uploadImageMultipart(@Header("Authorization") String token, @Body okhttp3.RequestBody requestBody);
    
    // Avatar Upload (for users)
    @POST("fileupload/avatar")
    okhttp3.Call uploadAvatar(@Header("Authorization") String token, @Body okhttp3.RequestBody requestBody);
}

