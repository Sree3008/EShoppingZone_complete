import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-reviews',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reviews.component.html',
  styleUrl: './admin-reviews.component.css'
})
export class AdminReviewsComponent implements OnInit {

  activeTab: 'pending' | 'all' = 'pending';

  pendingReviews: any[] = [];
  allReviews: any[] = [];

  loadingPending = false;
  loadingAll = false;

  errorMsg = '';
  successMsg = '';

  rejectReasonMap: { [key: number]: string } = {};
  showRejectFormMap: { [key: number]: boolean } = {};

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadPending();
    this.loadAll();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadPending() {
    this.loadingPending = true;
    this.adminService.getPendingReviews().subscribe({
      next: (res) => { this.pendingReviews = this.toList(res); this.loadingPending = false; },
      error: () => { this.errorMsg = 'Failed to load pending reviews.'; this.loadingPending = false; }
    });
  }

  loadAll() {
    this.loadingAll = true;
    this.adminService.getAllReviews().subscribe({
      next: (res) => { this.allReviews = this.toList(res); this.loadingAll = false; },
      error: () => { this.errorMsg = 'Failed to load reviews.'; this.loadingAll = false; }
    });
  }

  approve(id: number) {
    this.adminService.approveReview(id).subscribe({
      next: () => { this.successMsg = `Review #${id} approved.`; this.loadPending(); this.loadAll(); },
      error: () => { this.errorMsg = 'Failed to approve review.'; }
    });
  }

  showRejectForm(id: number) {
    this.showRejectFormMap[id] = true;
    this.rejectReasonMap[id] = this.rejectReasonMap[id] || '';
  }

  confirmReject(id: number) {
    const reason = this.rejectReasonMap[id] || '';
    if (!reason.trim()) { this.errorMsg = 'Please enter a rejection reason.'; return; }
    this.adminService.rejectReview(id, reason).subscribe({
      next: () => {
        this.successMsg = `Review #${id} rejected.`;
        this.showRejectFormMap[id] = false;
        this.loadPending(); this.loadAll();
      },
      error: () => { this.errorMsg = 'Failed to reject review.'; }
    });
  }

  hide(id: number) {
    this.adminService.hideReview(id).subscribe({
      next: () => { this.successMsg = `Review #${id} hidden.`; this.loadAll(); },
      error: () => { this.errorMsg = 'Failed to hide review.'; }
    });
  }

  stars(rating: number): string {
    return '★'.repeat(Math.min(5, Math.max(0, rating || 0))) + '☆'.repeat(5 - Math.min(5, Math.max(0, rating || 0)));
  }

  statusBadgeClass(status: string): string {
    if (status === 'APPROVED') return 'badge-active';
    if (status === 'PENDING') return 'badge-pending';
    if (status === 'REJECTED' || status === 'HIDDEN') return 'badge-rejected';
    return 'badge-default';
  }

  truncate(text: string, len = 80): string {
    if (!text) return '';
    return text.length > len ? text.substring(0, len) + '…' : text;
  }
}
