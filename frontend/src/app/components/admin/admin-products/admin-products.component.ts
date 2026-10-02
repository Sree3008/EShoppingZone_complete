import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-products.component.html',
  styleUrl: './admin-products.component.css'
})
export class AdminProductsComponent implements OnInit {

  activeTab: 'pending' | 'all' = 'pending';

  pendingProducts: any[] = [];
  allProducts: any[] = [];
  categories: any[] = [];

  loadingPending = false;
  loadingAll = false;
  loadingCats = false;

  errorMsg = '';
  successMsg = '';

  newCategoryName = '';
  submittingCategory = false;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadPending();
    this.loadAll();
    this.loadCategories();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadPending() {
    this.loadingPending = true;
    this.adminService.getPendingProducts().subscribe({
      next: (res) => { this.pendingProducts = this.toList(res); this.loadingPending = false; },
      error: () => { this.errorMsg = 'Failed to load pending products.'; this.loadingPending = false; }
    });
  }

  loadAll() {
    this.loadingAll = true;
    this.adminService.getAllProducts().subscribe({
      next: (res) => { this.allProducts = this.toList(res); this.loadingAll = false; },
      error: () => { this.errorMsg = 'Failed to load products.'; this.loadingAll = false; }
    });
  }

  loadCategories() {
    this.loadingCats = true;
    this.adminService.getAdminCategories().subscribe({
      next: (res) => { this.categories = this.toList(res); this.loadingCats = false; },
      error: () => { this.loadingCats = false; }
    });
  }

  approveProduct(id: number) {
    this.adminService.approveProduct(id).subscribe({
      next: () => { this.successMsg = 'Product approved.'; this.loadPending(); this.loadAll(); },
      error: () => { this.errorMsg = 'Failed to approve product.'; }
    });
  }

  rejectProduct(id: number) {
    this.adminService.rejectProduct(id).subscribe({
      next: () => { this.successMsg = 'Product rejected.'; this.loadPending(); this.loadAll(); },
      error: () => { this.errorMsg = 'Failed to reject product.'; }
    });
  }

  deactivateProduct(id: number) {
    this.adminService.deactivateProduct(id).subscribe({
      next: () => { this.successMsg = 'Product deactivated.'; this.loadAll(); },
      error: () => { this.errorMsg = 'Failed to deactivate product.'; }
    });
  }

  addCategory() {
    if (!this.newCategoryName.trim()) return;
    this.submittingCategory = true;
    this.adminService.createCategory({ name: this.newCategoryName.trim() }).subscribe({
      next: () => {
        this.successMsg = 'Category created.';
        this.newCategoryName = '';
        this.submittingCategory = false;
        this.loadCategories();
      },
      error: () => { this.errorMsg = 'Failed to create category.'; this.submittingCategory = false; }
    });
  }

  statusBadgeClass(status: string): string {
    if (status === 'ACTIVE' || status === 'APPROVED') return 'badge-active';
    if (status === 'PENDING') return 'badge-pending';
    if (status === 'REJECTED' || status === 'INACTIVE') return 'badge-rejected';
    return 'badge-default';
  }
}
