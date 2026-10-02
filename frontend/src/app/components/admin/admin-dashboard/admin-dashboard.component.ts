import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin, catchError, of } from 'rxjs';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {

  loading = true;
  errorMsg = '';

  stats = [
    { label: 'Total Users',       count: 0, color: '#6c2bd9', icon: '👥' },
    { label: 'Pending Products',  count: 0, color: '#ca8a04', icon: '🛍️' },
    { label: 'All Orders',        count: 0, color: '#2563eb', icon: '📦' },
    { label: 'Pending Returns',   count: 0, color: '#dc2626', icon: '↩️' },
    { label: 'Pending Refunds',   count: 0, color: '#ea580c', icon: '💳' },
    { label: 'Pending Reviews',   count: 0, color: '#16a34a', icon: '⭐' },
  ];

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadStats();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadStats() {
    this.loading = true;
    this.errorMsg = '';

    forkJoin({
      users:    this.adminService.getUsers().pipe(catchError(() => of(null))),
      pending:  this.adminService.getPendingProducts().pipe(catchError(() => of(null))),
      orders:   this.adminService.getAllOrders().pipe(catchError(() => of(null))),
      returns:  this.adminService.getAllReturns('PENDING').pipe(catchError(() => of(null))),
      refunds:  this.adminService.getPendingRefunds().pipe(catchError(() => of(null))),
      reviews:  this.adminService.getPendingReviews().pipe(catchError(() => of(null))),
    }).subscribe({
      next: (results) => {
        this.stats[0].count = this.toList(results.users).length;
        this.stats[1].count = this.toList(results.pending).length;
        this.stats[2].count = this.toList(results.orders).length;
        this.stats[3].count = this.toList(results.returns).length;
        this.stats[4].count = this.toList(results.refunds).length;
        this.stats[5].count = this.toList(results.reviews).length;
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Failed to load dashboard stats.';
        this.loading = false;
      }
    });
  }
}
