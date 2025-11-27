using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("User_Addresses")]
public class UserAddress
{
    [Key]
    [Column("address_id")]
    public int AddressId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [MaxLength(255)]
    [Column("full_name")]
    public string FullName { get; set; } = string.Empty;

    [Required]
    [MaxLength(20)]
    [Column("phone_number")]
    public string PhoneNumber { get; set; } = string.Empty;

    [Required]
    [Column("address_line")]
    public string AddressLine { get; set; } = string.Empty;

    [MaxLength(100)]
    [Column("city")]
    public string? City { get; set; }

    [MaxLength(100)]
    [Column("district")]
    public string? District { get; set; }

    [MaxLength(100)]
    [Column("ward")]
    public string? Ward { get; set; }

    [Column("is_default")]
    public bool IsDefault { get; set; } = false;

    [Column("created_at")]
    public DateTime CreatedAt { get; set; } = DateTime.Now;

    // Navigation property
    [ForeignKey("UserId")]
    public User? User { get; set; }
}
