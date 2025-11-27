using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Wishlist")]
public class Wishlist
{
    [Key]
    [Column("wishlist_id")]
    public int WishlistId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [Column("template_id")]
    public int TemplateId { get; set; }

    [Column("added_at")]
    public DateTime AddedAt { get; set; } = DateTime.Now;

    // Navigation properties
    [ForeignKey("UserId")]
    public User? User { get; set; }

    [ForeignKey("TemplateId")]
    public ProductTemplate? ProductTemplate { get; set; }
}
