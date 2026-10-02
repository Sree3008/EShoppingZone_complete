import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-deliveries',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-deliveries.component.html',
  styleUrl: './admin-deliveries.component.css'
})
export class AdminDeliveriesComponent implements OnInit {

  deliveries: any[] = [];
  loading = false;
  errorMsg = '';
  successMsg = '';

  filterStatus = '';

  assignForm = {
    orderId: '',
    agentUserId: ''
  };
  submitting = false;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadDeliveries();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadDeliveries() {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getAllDeliveries(this.filterStatus || undefined).subscribe({
      next: (res) => { this.deliveries = this.toList(res); this.loading = false; },
      error: () => { this.errorMsg = 'Failed to load deliveries.'; this.loading = false; }
    });
  }

  assignDelivery() {
    if (!this.assignForm.orderId || !this.assignForm.agentUserId) {
      this.errorMsg = 'Please fill both Order ID and Agent User ID.';
      return;
    }
    this.submitting = true;
    this.errorMsg = '';
    this.adminService.assignDelivery({
      orderId: Number(this.assignForm.orderId),
      agentUserId: Number(this.assignForm.agentUserId)
    }).subscribe({
      next: () => {
        this.successMsg = 'Delivery assigned successfully.';
        this.assignForm = { orderId: '', agentUserId: '' };
        this.submitting = false;
        this.loadDeliveries();
      },
      error: () => { this.errorMsg = 'Failed to assign delivery.'; this.submitting = false; }
    });
  }

  statusBadgeClass(status: string): string {
    if (status === 'DELIVERED') return 'badge-active';
    if (status === 'PENDING' || status === 'ASSIGNED') return 'badge-pending';
    if (status === 'IN_TRANSIT' || status === 'OUT_FOR_DELIVERY') return 'badge-shipped';
    if (status === 'FAILED') return 'badge-rejected';
    return 'badge-default';
  }
}
