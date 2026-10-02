import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-orders.component.html',
  styleUrl: './admin-orders.component.css'
})
export class AdminOrdersComponent implements OnInit {

  orders: any[] = [];
  loading = false;
  errorMsg = '';
  successMsg = '';

  statusOptions = ['CONFIRMED', 'PROCESSING', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];

  selectedStatus: { [key: number]: string } = {};

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadOrders();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadOrders() {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getAllOrders().subscribe({
      next: (res) => {
        this.orders = this.toList(res);
        this.orders.forEach(o => this.selectedStatus[o.id] = o.status);
        this.loading = false;
      },
      error: () => { this.errorMsg = 'Failed to load orders.'; this.loading = false; }
    });
  }

  updateStatus(order: any) {
    const newStatus = this.selectedStatus[order.id];
    if (!confirm(`Update order #${order.id} status to "${newStatus}"?`)) return;
    this.adminService.updateOrderStatus(order.id, newStatus).subscribe({
      next: () => { this.successMsg = `Order #${order.id} updated to ${newStatus}.`; this.loadOrders(); },
      error: () => { this.errorMsg = 'Failed to update order status.'; }
    });
  }

  statusBadgeClass(status: string): string {
    if (status === 'DELIVERED') return 'badge-active';
    if (status === 'PROCESSING' || status === 'CONFIRMED') return 'badge-pending';
    if (status === 'CANCELLED') return 'badge-rejected';
    if (status === 'SHIPPED' || status === 'OUT_FOR_DELIVERY') return 'badge-shipped';
    return 'badge-default';
  }
}
