import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-refunds',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-refund.component.html',
  styleUrl: './admin-refund.component.css'
})
export class AdminRefundsComponent implements OnInit {

  refunds: any[] = [];
  loading = false;
  errorMsg = '';
  successMsg = '';

  rejectReasonMap: { [key: number]: string } = {};
  showRejectFormMap: { [key: number]: boolean } = {};

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadRefunds();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadRefunds() {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getPendingRefunds().subscribe({
      next: (res) => { this.refunds = this.toList(res); this.loading = false; },
      error: () => { this.errorMsg = 'Failed to load refunds.'; this.loading = false; }
    });
  }

  approve(id: number) {
    this.adminService.approveRefund(id).subscribe({
      next: () => { this.successMsg = `Refund #${id} approved.`; this.loadRefunds(); },
      error: () => { this.errorMsg = 'Failed to approve refund.'; }
    });
  }

  showRejectForm(id: number) {
    this.showRejectFormMap[id] = true;
    this.rejectReasonMap[id] = this.rejectReasonMap[id] || '';
  }

  confirmReject(id: number) {
    const reason = this.rejectReasonMap[id] || '';
    if (!reason.trim()) { this.errorMsg = 'Please enter a rejection reason.'; return; }
    this.adminService.rejectRefund(id, reason).subscribe({
      next: () => {
        this.successMsg = `Refund #${id} rejected.`;
        this.showRejectFormMap[id] = false;
        this.loadRefunds();
      },
      error: () => { this.errorMsg = 'Failed to reject refund.'; }
    });
  }

  statusBadgeClass(status: string): string {
    if (status === 'APPROVED' || status === 'COMPLETED') return 'badge-active';
    if (status === 'PENDING') return 'badge-pending';
    if (status === 'REJECTED') return 'badge-rejected';
    return 'badge-default';
  }
}
