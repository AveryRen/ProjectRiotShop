using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Account_Details")]
public class AccountDetail
{
    [Key]
    [Column("acc_detail_id")]
    public int AccDetailId { get; set; }

    [Required]
    [Column("template_id")]
    public int TemplateId { get; set; }

    [Required]
    [MaxLength(255)]
    [Column("account_username")]
    public string AccountUsername { get; set; } = string.Empty;

    [Required]
    [MaxLength(255)]
    [Column("account_password")]
    public string AccountPassword { get; set; } = string.Empty;

    [MaxLength(255)]
    [Column("riot_id")]
    public string? RiotId { get; set; }

    [Column("is_sold")]
    public bool IsSold { get; set; } = false;

    [MaxLength(255)]
    [Column("recovery_email")]
    public string? RecoveryEmail { get; set; }

    [Column("is_drop_mail")]
    public bool IsDropMail { get; set; } = false;

    [Column("original_price", TypeName = "decimal(10,2)")]
    public decimal OriginalPrice { get; set; } = 0.00m;

    // Navigation property
    [ForeignKey("TemplateId")]
    public ProductTemplate? ProductTemplate { get; set; }
}
