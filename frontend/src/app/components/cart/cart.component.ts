import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CartService } from '../../services/cart.service';
import { Cart, CartItem } from '../../models/models';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {

  cart: Cart | null = null;
  loading = true;
  errorMsg = '';

  constructor(private cartService: CartService, private router: Router) {}

  ngOnInit() {
    this.loadCart();
  }

  loadCart() {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (res: any) => {
        this.loading = false;
        this.cart = res.data || res;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = 'Failed to load cart';
      }
    });
  }

  updateQuantity(item: CartItem, change: number) {
    const newQty = item.quantity + change;
    if (newQty < 1) {
      this.removeItem(item);
      return;
    }

    this.cartService.updateQuantity(item.id, newQty).subscribe({
      next: (res: any) => {
        this.cart = res.data || res;
      },
      error: () => {}
    });
  }

  removeItem(item: CartItem) {
    this.cartService.removeItem(item.id).subscribe({
      next: (res: any) => {
        this.cart = res.data || res;
      },
      error: () => {}
    });
  }

  clearCart() {
    if (confirm('Are you sure you want to clear the cart?')) {
      this.cartService.clearCart().subscribe({
        next: () => {
          this.cart = null;
          this.loadCart();
        },
        error: () => {}
      });
    }
  }

  goToCheckout() {
    this.router.navigate(['/checkout']);
  }

  getTotal(): number {
    if (!this.cart || !this.cart.items) return 0;
    return this.cart.items.reduce((sum, item) => sum + (item.subtotal || (item.unitPrice * item.quantity) || 0), 0);
  }

  formatPrice(price: number): string {
    return '₹' + price.toLocaleString('en-IN');
  }

  isCartEmpty(): boolean {
    return !this.cart || !this.cart.items || this.cart.items.length === 0;
  }
}
