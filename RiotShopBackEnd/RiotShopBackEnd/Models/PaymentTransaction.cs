using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("PaymentTransactions")]
public class PaymentTransaction
{
    [Key]
    [Column("transaction_id")]
    public int TransactionId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [Column("stripe_payment_intent_id")]
    [MaxLength(255)]
    public string StripePaymentIntentId { get; set; } = string.Empty;

    [Required]
    [Column("amount", TypeName = "decimal(10,2)")]
    public decimal Amount { get; set; }

    [Required]
    [Column("currency")]
    [MaxLength(10)]
    public string Currency { get; set; } = "USD";

    [Required]
    [Column("status")]
    [MaxLength(50)]
    public string Status { get; set; } = "pending"; // pending, succeeded, failed, canceled

    [Column("created_at")]
    public DateTime CreatedAt { get; set; } = DateTime.Now;

    [Column("updated_at")]
    public DateTime? UpdatedAt { get; set; }

    [Column("completed_at")]
    public DateTime? CompletedAt { get; set; }

    [Column("failure_reason")]
    public string? FailureReason { get; set; }

    // Navigation property
    [ForeignKey("UserId")]
    public User? User { get; set; }
}

