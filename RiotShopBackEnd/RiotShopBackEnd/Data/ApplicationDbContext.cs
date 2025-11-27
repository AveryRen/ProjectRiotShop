using Microsoft.EntityFrameworkCore;
using RiotShopBackEnd.Models;

namespace RiotShopBackEnd.Data;

public class ApplicationDbContext : DbContext
{
    public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options)
    {
    }

    public DbSet<GameType> GameTypes { get; set; }
    public DbSet<ProductTemplate> ProductTemplates { get; set; }
    public DbSet<InventoryPackage> InventoryPackages { get; set; }
    public DbSet<User> Users { get; set; }
    public DbSet<AccountDetail> AccountDetails { get; set; }
    public DbSet<Order> Orders { get; set; }
    public DbSet<Review> Reviews { get; set; }
    public DbSet<Wishlist> Wishlist { get; set; }
    public DbSet<CartItem> CartItems { get; set; }
    public DbSet<UserAddress> UserAddresses { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Configure table names to match MySQL schema (with underscores)
        modelBuilder.Entity<GameType>().ToTable("Game_Types");
        modelBuilder.Entity<ProductTemplate>().ToTable("Product_Templates");
        modelBuilder.Entity<InventoryPackage>().ToTable("Inventory_Packages");
        modelBuilder.Entity<AccountDetail>().ToTable("Account_Details");

        // Configure foreign keys
        modelBuilder.Entity<ProductTemplate>()
            .HasOne(pt => pt.GameType)
            .WithMany()
            .HasForeignKey(pt => pt.GameId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<InventoryPackage>()
            .HasOne(ip => ip.ProductTemplate)
            .WithMany()
            .HasForeignKey(ip => ip.TemplateId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<AccountDetail>()
            .HasOne(ad => ad.ProductTemplate)
            .WithMany()
            .HasForeignKey(ad => ad.TemplateId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<Order>()
            .HasOne(o => o.User)
            .WithMany()
            .HasForeignKey(o => o.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<Order>()
            .HasOne(o => o.AccountDetail)
            .WithMany()
            .HasForeignKey(o => o.AccDetailId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<Review>()
            .HasOne(r => r.User)
            .WithMany()
            .HasForeignKey(r => r.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<Review>()
            .HasOne(r => r.ProductTemplate)
            .WithMany()
            .HasForeignKey(r => r.TemplateId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<Wishlist>()
            .HasOne(w => w.User)
            .WithMany()
            .HasForeignKey(w => w.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<Wishlist>()
            .HasOne(w => w.ProductTemplate)
            .WithMany()
            .HasForeignKey(w => w.TemplateId)
            .OnDelete(DeleteBehavior.Restrict);

        // Unique constraint for Wishlist
        modelBuilder.Entity<Wishlist>()
            .HasIndex(w => new { w.UserId, w.TemplateId })
            .IsUnique();

        // Unique constraint for Order.TransactionId
        modelBuilder.Entity<Order>()
            .HasIndex(o => o.TransactionId)
            .IsUnique();

        // Configure Cart Items
        modelBuilder.Entity<CartItem>()
            .HasOne(ci => ci.User)
            .WithMany()
            .HasForeignKey(ci => ci.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<CartItem>()
            .HasOne(ci => ci.ProductTemplate)
            .WithMany()
            .HasForeignKey(ci => ci.TemplateId)
            .OnDelete(DeleteBehavior.Restrict);

        // Unique constraint for CartItem (một user chỉ có 1 cart item cho mỗi template)
        modelBuilder.Entity<CartItem>()
            .HasIndex(ci => new { ci.UserId, ci.TemplateId })
            .IsUnique();

        // Configure User Addresses
        modelBuilder.Entity<UserAddress>()
            .HasOne(ua => ua.User)
            .WithMany()
            .HasForeignKey(ua => ua.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        // Configure table names
        modelBuilder.Entity<CartItem>().ToTable("Cart_Items");
        modelBuilder.Entity<UserAddress>().ToTable("User_Addresses");
    }
}
