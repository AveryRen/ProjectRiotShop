using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Users")]
public class User
{
    [Key]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [MaxLength(255)]
    [Column("username")]
    public string Username { get; set; } = string.Empty;

    [Required]
    [MaxLength(255)]
    [Column("password_hash")]
    public string PasswordHash { get; set; } = string.Empty;

    [Required]
    [MaxLength(255)]
    [Column("email")]
    public string Email { get; set; } = string.Empty;

    [MaxLength(255)]
    [Column("full_name")]
    public string? FullName { get; set; }

    [MaxLength(20)]
    [Column("phone_number")]
    public string? PhoneNumber { get; set; }

    [Column("address")]
    public string? Address { get; set; }

    [Column("is_admin")]
    public bool IsAdmin { get; set; } = false;

    [Column("balance", TypeName = "decimal(10,2)")]
    public decimal Balance { get; set; } = 0.00m;

    [Column("created_at")]
    public DateTime CreatedAt { get; set; } = DateTime.Now;

    [MaxLength(255)]
    [Column("avatar_url")]
    public string? AvatarUrl { get; set; }

    [Column("last_login")]
    public DateTime? LastLogin { get; set; }
}
