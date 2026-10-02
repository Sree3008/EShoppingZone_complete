import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-detail.component.html',
  styleUrl: './order-detail.component.css'
})
export class OrderDetailComponent implements OnInit {

  order: any = null;
  loading = true;
  cancelLoading = false;
  cancelMsg = '';

  delivery: any = null;
  showReturnForm = false;
  returnReason = '';
  returnLoading = false;
  returnMsg = '';

  readonly STEPS = ['CONFIRMED', 'PROCESSING', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED'];

  readonly STEP_LABELS: { [k: string]: string } = {
    CONFIRMED: 'Order Confirmed',
    PROCESSING: 'Processing',
    SHIPPED: 'Shipped',
    OUT_FOR_DELIVERY: 'Out for Delivery',
    DELIVERED: 'Delivered'
  };

  constructor(private route: ActivatedRoute, private orderService: OrderService) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadOrder(+id);
  }

  loadOrder(id: number) {
    this.orderService.getOrderById(id).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.order = res.data || res;
        if (this.order && !['CANCELLED', 'RETURNED'].includes(this.order.status)) {
          this.loadDelivery(this.order.id);
        }
      },
      error: () => { this.loading = false; }
    });
  }

  loadDelivery(orderId: number) {
    this.orderService.getDelivery(orderId).subscribe({
      next: (res: any) => { this.delivery = res.data || res; },
      error: () => {}
    });
  }

  canReturn(): boolean {
    return this.order?.status === 'DELIVERED';
  }

  submitReturn() {
    if (!this.returnReason.trim() || !this.order) return;
    this.returnLoading = true;
    const item = this.order.items?.[0];
    this.orderService.createReturn({
      orderId: this.order.id,
      orderItemId: item?.id,
      quantity: item?.quantity,
      reason: this.returnReason
    }).subscribe({
      next: () => {
        this.returnLoading = false;
        this.returnMsg = 'Return request submitted successfully!';
        this.showReturnForm = false;
      },
      error: (err: any) => {
        this.returnLoading = false;
        this.returnMsg = err.error?.message || 'Failed to submit return request';
      }
    });
  }

  isStepDone(step: string): boolean {
    if (!this.order) return false;
    const cur = this.STEPS.indexOf(this.order.status);
    const s = this.STEPS.indexOf(step);
    return s <= cur && cur >= 0;
  }

  isCancelled(): boolean {
    return this.order && ['CANCELLED', 'RETURNED'].includes(this.order.status);
  }

  getStatusColor(status: string): string {
    const map: any = {
      PENDING: '#f59e0b', CONFIRMED: '#6c2bd9', PROCESSING: '#2563eb',
      SHIPPED: '#0891b2', OUT_FOR_DELIVERY: '#7c3aed', DELIVERED: '#16a34a',
      CANCELLED: '#dc2626', RETURNED: '#f59e0b'
    };
    return map[status] || '#6b7280';
  }

  getStatusLabel(status: string): string {
    return (status || '').replace(/_/g, ' ');
  }

  cancelOrder() {
    if (!this.order || !confirm('Are you sure you want to cancel this order?')) return;
    this.cancelLoading = true;
    this.orderService.cancelOrder(this.order.id, 'Customer requested cancellation').subscribe({
      next: (res: any) => {
        this.cancelLoading = false;
        this.order = res.data || res;
        this.cancelMsg = 'Order cancelled successfully.';
      },
      error: (err) => {
        this.cancelLoading = false;
        this.cancelMsg = err.error?.message || 'Failed to cancel order';
      }
    });
  }

  get orderNumber(): string {
    return this.order?.orderNumber || ('ORD-' + this.order?.id);
  }
  get orderTotal(): number {
    return this.order?.totalAmount || this.order?.totalPrice || 0;
  }
  get paymentStatus(): string {
    return this.order?.paymentStatus || 'PENDING';
  }
  get orderStatus(): string {
    return this.order?.status || '';
  }

  canCancel(): boolean {
    if (!this.order) return false;
    return ['PENDING', 'CONFIRMED', 'PROCESSING'].includes(this.order.status);
  }

  formatPrice(price: number): string {
    return '₹' + (price || 0).toLocaleString('en-IN');
  }
}
