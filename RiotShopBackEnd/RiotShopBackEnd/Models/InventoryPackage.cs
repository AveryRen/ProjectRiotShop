using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Inventory_Packages")]
public class InventoryPackage
{
    [Key]
    [Column("package_id")]
    public int PackageId { get; set; }

    [Required]
    [Column("template_id")]
    public int TemplateId { get; set; }

    [Required]
    [Column("quantity_available")]
    public int QuantityAvailable { get; set; }

    [Required]
    [Column("price", TypeName = "decimal(10,2)")]
    public decimal Price { get; set; }

    [Column("last_updated")]
    public DateTime LastUpdated { get; set; } = DateTime.Now;

    // Navigation property
    [ForeignKey("TemplateId")]
    public ProductTemplate? ProductTemplate { get; set; }
}
