using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Cart_Items")]
public class CartItem
{
    [Key]
    [Column("cart_item_id")]
    public int CartItemId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [Column("template_id")]
    public int TemplateId { get; set; }

    [Required]
    [Column("quantity")]
    public int Quantity { get; set; } = 1;

    [Column("added_at")]
    public DateTime AddedAt { get; set; } = DateTime.Now;

    // Navigation properties
    [ForeignKey("UserId")]
    public User? User { get; set; }

    [ForeignKey("TemplateId")]
    public ProductTemplate? ProductTemplate { get; set; }
}
