import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {

  orders: any[] = [];
  loading = true;
  errorMsg = '';

  readonly STEPS = ['CONFIRMED', 'PROCESSING', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED'];

  readonly STEP_LABELS: { [k: string]: string } = {
    CONFIRMED: 'Order Confirmed',
    PROCESSING: 'Processing',
    SHIPPED: 'Shipped',
    OUT_FOR_DELIVERY: 'Out for Delivery',
    DELIVERED: 'Delivered'
  };

  readonly STEP_ICONS: { [k: string]: string } = {
    CONFIRMED: '✓',
    PROCESSING: '⚙',
    SHIPPED: '📦',
    OUT_FOR_DELIVERY: '🚚',
    DELIVERED: '🏠'
  };

  constructor(private orderService: OrderService) {}

  ngOnInit() { this.loadOrders(); }

  loadOrders() {
    this.orderService.getMyOrders().subscribe({
      next: (res: any) => {
        this.loading = false;
        const all = Array.isArray(res) ? res : (res.data || []);
        this.orders = all.filter((o: any) => !['FAILED', 'PAYMENT_FAILED'].includes(o.status));
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Failed to load orders';
      }
    });
  }

  getStepIndex(status: string): number {
    return this.STEPS.indexOf(status);
  }

  isStepDone(order: any, step: string): boolean {
    const cur = this.getStepIndex(order.status);
    const s = this.getStepIndex(step);
    return s <= cur && cur >= 0;
  }

  isCancelled(order: any): boolean {
    return ['CANCELLED', 'RETURNED'].includes(order.status);
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
    return status.replace(/_/g, ' ');
  }

  formatPrice(price: number): string {
    return '₹' + (price || 0).toLocaleString('en-IN');
  }
}
