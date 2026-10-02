import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent {

  navLinks = [
    { path: 'dashboard', label: 'Dashboard', icon: '📊' },
    { path: 'users',     label: 'Users',     icon: '👥' },
    { path: 'products',  label: 'Products',  icon: '🛍️' },
    { path: 'orders',    label: 'Orders',    icon: '📦' },
    { path: 'returns',   label: 'Returns',   icon: '↩️' },
    { path: 'deliveries',label: 'Deliveries',icon: '🚚' },
    { path: 'reviews',   label: 'Reviews',   icon: '⭐' },
    { path: 'refunds',   label: 'Refunds',   icon: '💳' },
    { path: 'audit-logs',label: 'Audit Logs',icon: '📋' },
  ];

  constructor(private authService: AuthService) {}

  logout() {
    this.authService.logout();
  }
}
