import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-wallet-transactions',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './wallet-transactions.component.html',
  styleUrl: './wallet-transactions.component.css'
})
export class WalletTransactionsComponent implements OnInit {
  transactions: any[] = [];
  loading = true;

  totalCredits = 0;
  totalDebits = 0;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.orderService.getWalletTransactions().subscribe({
      next: (res: any) => {
        const data = res.data || res;
        this.transactions = Array.isArray(data) ? data : (data.content || []);
        this.totalCredits = this.transactions
          .filter((t: any) => t.type === 'CREDIT')
          .reduce((sum: number, t: any) => sum + (t.amount || 0), 0);
        this.totalDebits = this.transactions
          .filter((t: any) => t.type === 'DEBIT')
          .reduce((sum: number, t: any) => sum + (t.amount || 0), 0);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  formatPrice(amount: number): string {
    return '₹' + (amount || 0).toLocaleString('en-IN');
  }

  isCredit(type: string): boolean {
    return type === 'CREDIT';
  }
}
