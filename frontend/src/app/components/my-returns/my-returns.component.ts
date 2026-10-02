import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-my-returns',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-returns.component.html',
  styleUrl: './my-returns.component.css'
})
export class MyReturnsComponent implements OnInit {
  returns: any[] = [];
  loading = true;
  cancellingId: number | null = null;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.loadReturns();
  }

  loadReturns() {
    this.loading = true;
    this.orderService.getMyReturns().subscribe({
      next: (res: any) => {
        const data = res.data || res;
        this.returns = Array.isArray(data) ? data : (data.content || []);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  canCancel(status: string): boolean {
    return ['PENDING', 'REQUESTED'].includes(status);
  }

  cancelReturn(id: number) {
    if (!confirm('Cancel this return request?')) return;
    this.cancellingId = id;
    this.orderService.cancelReturn(id).subscribe({
      next: () => {
        this.cancellingId = null;
        this.loadReturns();
      },
      error: () => { this.cancellingId = null; }
    });
  }

  getStatusColor(status: string): string {
    const map: any = {
      PENDING: '#f59e0b',
      REQUESTED: '#f59e0b',
      APPROVED: '#16a34a',
      REJECTED: '#dc2626',
      CANCELLED: '#6b7280',
      COMPLETED: '#2563eb'
    };
    return map[status] || '#6b7280';
  }

  formatPrice(price: number): string {
    return '₹' + (price || 0).toLocaleString('en-IN');
  }
}
