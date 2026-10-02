import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-returns',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-returns.component.html',
  styleUrl: './admin-returns.component.css'
})
export class AdminReturnsComponent implements OnInit {

  returns: any[] = [];
  loading = false;
  errorMsg = '';
  successMsg = '';

  filterStatus = '';
  rejectReasonMap: { [key: number]: string } = {};
  showRejectFormMap: { [key: number]: boolean } = {};

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadReturns();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadReturns() {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getAllReturns(this.filterStatus || undefined).subscribe({
      next: (res) => { this.returns = this.toList(res); this.loading = false; },
      error: () => { this.errorMsg = 'Failed to load returns.'; this.loading = false; }
    });
  }

  approve(id: number) {
    this.adminService.approveReturn(id).subscribe({
      next: () => { this.successMsg = `Return #${id} approved.`; this.loadReturns(); },
      error: () => { this.errorMsg = 'Failed to approve return.'; }
    });
  }

  showRejectForm(id: number) {
    this.showRejectFormMap[id] = true;
    this.rejectReasonMap[id] = this.rejectReasonMap[id] || '';
  }

  confirmReject(id: number) {
    const reason = this.rejectReasonMap[id] || '';
    if (!reason.trim()) { this.errorMsg = 'Please enter a rejection reason.'; return; }
    this.adminService.rejectReturn(id, reason).subscribe({
      next: () => {
        this.successMsg = `Return #${id} rejected.`;
        this.showRejectFormMap[id] = false;
        this.loadReturns();
      },
      error: () => { this.errorMsg = 'Failed to reject return.'; }
    });
  }

  retryRestock(id: number) {
    this.adminService.retryRestock(id).subscribe({
      next: () => { this.successMsg = `Restock retried for return #${id}.`; this.loadReturns(); },
      error: () => { this.errorMsg = 'Failed to retry restock.'; }
    });
  }

  retryRefund(id: number) {
    this.adminService.retryRefund(id).subscribe({
      next: () => { this.successMsg = `Refund retried for return #${id}.`; this.loadReturns(); },
      error: () => { this.errorMsg = 'Failed to retry refund.'; }
    });
  }

  statusBadgeClass(status: string): string {
    if (status === 'APPROVED') return 'badge-active';
    if (status === 'PENDING') return 'badge-pending';
    if (status === 'REJECTED') return 'badge-rejected';
    return 'badge-default';
  }
}
