import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-my-refunds',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-refunds.component.html',
  styleUrl: './my-refunds.component.css'
})
export class MyRefundsComponent implements OnInit {
  refunds: any[] = [];
  loading = true;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.orderService.getMyRefunds().subscribe({
      next: (res: any) => {
        const data = res.data || res;
        this.refunds = Array.isArray(data) ? data : (data.content || []);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  getStatusColor(status: string): string {
    const map: any = {
      PENDING: '#f59e0b',
      APPROVED: '#16a34a',
      REJECTED: '#dc2626',
      COMPLETED: '#2563eb',
      PROCESSING: '#7c3aed'
    };
    return map[status] || '#6b7280';
  }

  formatPrice(amount: number): string {
    return '₹' + (amount || 0).toLocaleString('en-IN');
  }
}
