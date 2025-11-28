using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using RiotShopBackEnd.Data;
using RiotShopBackEnd.Services;
using MySqlConnector;
using System.Text;
using Microsoft.OpenApi.Models;
using Microsoft.OpenApi.Any;
using Swashbuckle.AspNetCore.SwaggerGen;
using System.Linq;
using Microsoft.AspNetCore.Authorization;
using RiotShopBackEnd.Filters;
using System.Collections.Generic;
using CloudinaryDotNet;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo 
    { 
        Title = "RiotShop API", 
        Version = "v1",
        Description = "API for RiotShop E-commerce Application"
    });

    // Configure JWT Authentication in Swagger
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = @"JWT Authorization header using the Bearer scheme. 
                      Enter 'Bearer' [space] and then your token in the text input below.
                      Example: 'Bearer 12345abcdef'",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "Bearer",
        BearerFormat = "JWT"
    });
    
    // Thêm Operation Filter để chỉ áp dụng security cho endpoints có [Authorize]
    c.OperationFilter<SecurityRequirementsOperationFilter>();
});

// Configure CORS - Cho phép Android app kết nối
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAndroidApp", policy =>
    {
        policy.WithOrigins("*") // Cho phép tất cả origins (development only)
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// Configure Entity Framework với MySQL
// Dùng ConnectionStringBuilder để xử lý password có ký tự đặc biệt an toàn hơn
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");

if (!string.IsNullOrEmpty(connectionString))
{
    // Parse connection string và rebuild để xử lý password có ký tự đặc biệt
    var connectionStringBuilder = new MySqlConnectionStringBuilder(connectionString);
    
    builder.Services.AddDbContext<ApplicationDbContext>(options =>
    {
        options.UseMySql(connectionStringBuilder.ConnectionString, 
            new MySqlServerVersion(new Version(8, 0, 21)),
            mysqlOptions => mysqlOptions.EnableRetryOnFailure());
    });
}

// Add services
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IJwtService, JwtService>();

// Add Cloudinary
var cloudinarySettings = builder.Configuration.GetSection("Cloudinary");
var cloudinaryAccount = new CloudinaryDotNet.Account(
    cloudinarySettings["CloudName"],
    cloudinarySettings["ApiKey"],
    cloudinarySettings["ApiSecret"]
);
builder.Services.AddSingleton(new CloudinaryDotNet.Cloudinary(cloudinaryAccount));

// Configure JWT Authentication
var jwtSettings = builder.Configuration.GetSection("JwtSettings");
var secretKey = jwtSettings["SecretKey"] ?? "YourSuperSecretKeyForJWTTokenGeneration2024!RiotShopBackend";
var issuer = jwtSettings["Issuer"] ?? "RiotShop";
var audience = jwtSettings["Audience"] ?? "RiotShopUsers";

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey)),
        ValidateIssuer = true,
        ValidIssuer = issuer,
        ValidateAudience = true,
        ValidAudience = audience,
        ValidateLifetime = true,
        ClockSkew = TimeSpan.Zero
    };
    
    // Bỏ qua authentication cho Swagger endpoints
    options.Events = new Microsoft.AspNetCore.Authentication.JwtBearer.JwtBearerEvents
    {
        OnMessageReceived = context =>
        {
            // Bỏ qua authentication cho Swagger paths
            if (context.Request.Path.StartsWithSegments("/swagger"))
            {
                context.Token = null;
            }
            return System.Threading.Tasks.Task.CompletedTask;
        }
    };
});

builder.Services.AddAuthorization(options =>
{
    // Cho phép Swagger endpoints không cần authentication
    options.FallbackPolicy = null;
});

var app = builder.Build();

// Configure the HTTP request pipeline.
// Thứ tự middleware theo best practice

// 0. Create uploads directory structure
var uploadsPath = Path.Combine(builder.Environment.ContentRootPath, "uploads");
if (!Directory.Exists(uploadsPath))
{
    Directory.CreateDirectory(uploadsPath);
}

var imagesPath = Path.Combine(uploadsPath, "images");
if (!Directory.Exists(imagesPath))
{
    Directory.CreateDirectory(imagesPath);
}

var avatarsPath = Path.Combine(uploadsPath, "avatars");
if (!Directory.Exists(avatarsPath))
{
    Directory.CreateDirectory(avatarsPath);
}

// 1. Static files for uploaded images (serve from uploads folder)
app.UseStaticFiles(new StaticFileOptions
{
    FileProvider = new Microsoft.Extensions.FileProviders.PhysicalFileProvider(uploadsPath),
    RequestPath = "/uploads"
});

// 1. CORS
app.UseCors("AllowAndroidApp");

// 2. Middleware để đảm bảo token có prefix "Bearer"
app.Use(async (context, next) =>
{
    var token = context.Request.Headers["Authorization"].FirstOrDefault();
    if (!string.IsNullOrEmpty(token) && !token.StartsWith("Bearer "))
    {
        context.Request.Headers["Authorization"] = $"Bearer {token}";
    }
    await next();
});

// 3. Authentication và Authorization
app.UseAuthentication();
app.UseAuthorization();

// 4. Swagger và SwaggerUI - đặt SAU UseAuthentication và UseAuthorization
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "RiotShop API v1");
        c.RoutePrefix = "swagger"; // Swagger UI ở /swagger (truy cập: https://localhost:5001/swagger)
        c.DisplayRequestDuration();
        c.EnableDeepLinking();
        c.EnableFilter();
        c.ShowExtensions();
        c.EnableValidator();
        c.DocExpansion(Swashbuckle.AspNetCore.SwaggerUI.DocExpansion.List);
    });
}

// 5. Map controllers với prefix /api
app.MapControllers();

app.Run();