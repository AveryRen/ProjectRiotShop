using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RiotShopBackEnd.Models;

[Table("Orders")]
public class Order
{
    [Key]
    [Column("order_id")]
    public int OrderId { get; set; }

    [Required]
    [Column("user_id")]
    public int UserId { get; set; }

    [Required]
    [Column("acc_detail_id")]
    public int AccDetailId { get; set; }

    [Required]
    [Column("total_amount", TypeName = "decimal(10,2)")]
    public decimal TotalAmount { get; set; }

    [Required]
    [MaxLength(50)]
    [Column("status")]
    public string Status { get; set; } = "pending"; // pending, completed, cancelled

    [Column("order_date")]
    public DateTime OrderDate { get; set; } = DateTime.Now;

    [Required]
    [MaxLength(255)]
    [Column("transaction_id")]
    public string TransactionId { get; set; } = string.Empty;

    [MaxLength(50)]
    [Column("payment_method")]
    public string PaymentMethod { get; set; } = "Balance";

    [MaxLength(50)]
    [Column("refund_status")]
    public string RefundStatus { get; set; } = "None"; // None, Requested, Completed

    [Column("cancel_reason")]
    public string? CancelReason { get; set; }

    // Navigation properties
    [ForeignKey("UserId")]
    public User? User { get; set; }

    [ForeignKey("AccDetailId")]
    public AccountDetail? AccountDetail { get; set; }
}
