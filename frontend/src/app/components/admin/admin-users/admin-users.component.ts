import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {

  users: any[] = [];
  loading = false;
  errorMsg = '';
  successMsg = '';

  filterRole = '';
  filterStatus = '';

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadUsers();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadUsers() {
    this.loading = true;
    this.errorMsg = '';
    this.successMsg = '';
    const role   = this.filterRole   || undefined;
    const status = this.filterStatus || undefined;
    this.adminService.getUsers(role, status).subscribe({
      next: (res) => { this.users = this.toList(res); this.loading = false; },
      error: () => { this.errorMsg = 'Failed to load users.'; this.loading = false; }
    });
  }

  roleBadgeClass(roles: string[]): string {
    if (!roles || roles.length === 0) return 'badge-default';
    if (roles.includes('ROLE_ADMIN')) return 'badge-blocked';
    return 'badge-default';
  }

  statusBadgeClass(status: string): string {
    if (status === 'ACTIVE') return 'badge-active';
    if (status === 'BLOCKED') return 'badge-blocked';
    return 'badge-default';
  }

  updateStatus(user: any, status: string) {
    this.adminService.updateUserStatus(user.id, status).subscribe({
      next: () => { this.successMsg = `User ${user.id} status updated to ${status}.`; this.loadUsers(); },
      error: () => { this.errorMsg = 'Failed to update user status.'; }
    });
  }
}
