using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Product_Templates")]
public class ProductTemplate
{
    [Key]
    [Column("template_id")]
    public int TemplateId { get; set; }

    [Required]
    [Column("game_id")]
    public int GameId { get; set; }

    [Required]
    [MaxLength(255)]
    [Column("title")]
    public string Title { get; set; } = string.Empty;

    [Column("description")]
    public string? Description { get; set; }

    [Required]
    [Column("base_price", TypeName = "decimal(10,2)")]
    public decimal BasePrice { get; set; }

    [Column("is_featured")]
    public bool IsFeatured { get; set; } = false;

    [MaxLength(100)]
    [Column("tag_rank")]
    public string? TagRank { get; set; }

    [MaxLength(100)]
    [Column("tag_skins")]
    public string? TagSkins { get; set; }

    [MaxLength(100)]
    [Column("tag_collection")]
    public string? TagCollection { get; set; }

    [MaxLength(500)]
    [Column("image_url")]
    public string? ImageUrl { get; set; }

    // Navigation property
    [ForeignKey("GameId")]
    public GameType? GameType { get; set; }
}
