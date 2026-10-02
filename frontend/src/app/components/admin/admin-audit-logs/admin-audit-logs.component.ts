import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-audit-logs.component.html',
  styleUrl: './admin-audit-logs.component.css'
})
export class AdminAuditLogsComponent implements OnInit {

  logs: any[] = [];
  loading = false;
  errorMsg = '';

  filterAction = '';
  filterUser = '';

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadLogs();
  }

  private toList(res: any): any[] {
    const data = res?.data ?? res;
    return Array.isArray(data) ? data : (data?.content || data?.items || []);
  }

  loadLogs() {
    this.loading = true;
    this.errorMsg = '';
    const params: any = {};
    if (this.filterAction) params['action'] = this.filterAction;
    if (this.filterUser) params['userId'] = this.filterUser;
    this.adminService.getAuditLogs(params).subscribe({
      next: (res) => { this.logs = this.toList(res); this.loading = false; },
      error: () => { this.errorMsg = 'Failed to load audit logs.'; this.loading = false; }
    });
  }
}
