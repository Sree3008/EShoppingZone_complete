import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {

  profile: any = {};
  wallet: any = null;
  loading = true;
  saving = false;
  profileMsg = '';
  topupAmount = 100;
  topupLoading = false;
  topupMsg = '';

  authUser: any = null;

  constructor(private orderService: OrderService, private authService: AuthService) {}

  ngOnInit() {
    this.authUser = this.authService.getUser();
    this.loadProfile();
    this.loadWallet();
  }

  loadProfile() {
    this.orderService.getMyProfile().subscribe({
      next: (res: any) => {
        this.loading = false;
        this.profile = res.data || res;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadWallet() {
    this.orderService.getWallet().subscribe({
      next: (res: any) => {
        this.wallet = res.data || res;
      },
      error: () => {}
    });
  }

  saveProfile() {
    this.saving = true;
    this.profileMsg = '';

    const firstName = this.profile.firstName || '';
    const lastName = this.profile.lastName || '';
    const fullName = (firstName + ' ' + lastName).trim() || this.authUser?.fullName || 'User';

    this.orderService.updateProfile({
      fullName,
      phoneNumber: this.profile.phoneNumber,
      gender: this.profile.gender,
      dateOfBirth: this.profile.dateOfBirth
    }).subscribe({
      next: () => {
        this.saving = false;
        this.profileMsg = '✓ Profile updated successfully!';
      },
      error: () => {
        this.saving = false;
        this.profileMsg = 'Failed to update profile';
      }
    });
  }

  topupWallet() {
    if (this.topupAmount < 1) return;
    this.topupLoading = true;
    this.topupMsg = '';

    this.orderService.topupWallet(this.topupAmount).subscribe({
      next: (res: any) => {
        this.topupLoading = false;
        this.wallet = res.data || res;
        this.topupMsg = `✓ ₹${this.topupAmount} added to wallet!`;
      },
      error: (err) => {
        this.topupLoading = false;
        this.topupMsg = err.error?.message || 'Topup failed';
      }
    });
  }

  formatPrice(price: number): string {
    return '₹' + (price || 0).toLocaleString('en-IN');
  }
}
