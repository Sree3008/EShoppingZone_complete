import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { WishlistService } from '../../services/wishlist.service';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css'
})
export class WishlistComponent implements OnInit {

  wishlistItems: any[] = [];
  loading = true;
  movingToCart: Set<number> = new Set();

  constructor(
    private wishlistService: WishlistService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit() { this.loadWishlist(); }

  loadWishlist() {
    this.wishlistService.getWishlist().subscribe({
      next: (res: any) => {
        this.loading = false;
        const wishlist = res.data || res;
        this.wishlistItems = wishlist.items || [];
      },
      error: () => { this.loading = false; }
    });
  }

  removeItem(productId: number) {
    this.wishlistService.removeFromWishlist(productId).subscribe({
      next: () => {
        this.wishlistItems = this.wishlistItems.filter(i => i.productId !== productId);
      },
      error: () => {}
    });
  }

  moveToCart(item: any) {
    this.movingToCart.add(item.productId);
    this.cartService.addToCart(item.productId, 1).subscribe({
      next: () => {
        this.wishlistService.removeFromWishlist(item.productId).subscribe({
          next: () => {
            this.wishlistItems = this.wishlistItems.filter(i => i.productId !== item.productId);
            this.movingToCart.delete(item.productId);
            this.router.navigate(['/cart']);
          },
          error: () => {
            this.movingToCart.delete(item.productId);
            this.router.navigate(['/cart']);
          }
        });
      },
      error: () => { this.movingToCart.delete(item.productId); }
    });
  }

  formatPrice(price: number): string {
    return '₹' + (price || 0).toLocaleString('en-IN');
  }

  getOriginalPrice(price: number): number {
    return Math.round(price * 1.18);
  }
}
