using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Reviews")]
public class Review
{
    [Key]
    [Column("review_id")]
    public int ReviewId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [Column("template_id")]
    public int TemplateId { get; set; }

    [Required]
    [Range(1, 5)]
    [Column("rating")]
    public int Rating { get; set; }

    [Required]
    [Column("comment")]
    public string Comment { get; set; } = string.Empty;

    [Column("created_at")]
    public DateTime CreatedAt { get; set; } = DateTime.Now;

    [Column("is_approved")]
    public bool IsApproved { get; set; } = false;

    // Navigation properties
    [ForeignKey("UserId")]
    public User? User { get; set; }

    [ForeignKey("TemplateId")]
    public ProductTemplate? ProductTemplate { get; set; }
}
