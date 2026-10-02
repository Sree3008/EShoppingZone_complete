import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css'
})
export class ForgotPasswordComponent {
  email = '';
  loading = false;
  sent = false;
  errorMsg = '';

  constructor(private authService: AuthService) {}

  submit() {
    if (!this.email.trim()) {
      this.errorMsg = 'Please enter your email address.';
      return;
    }
    this.errorMsg = '';
    this.loading = true;
    this.authService.forgotPassword(this.email.trim()).subscribe({
      next: () => {
        this.loading = false;
        this.sent = true;
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMsg = err.error?.message || 'Failed to send reset link. Please try again.';
      }
    });
  }
}
