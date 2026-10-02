import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { WishlistService } from '../../services/wishlist.service';
import { AuthService } from '../../services/auth.service';
import { Product, Review, ReviewSummary } from '../../models/models';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.css'
})
export class ProductDetailComponent implements OnInit {

  product: Product | null = null;
  reviews: Review[] = [];
  reviewSummary: ReviewSummary | null = null;
  inventory: any = null;
  recommendations: Product[] = [];
  loading = true;
  cartLoading = false;
  wishlistLoading = false;
  inWishlist = false;
  cartMessage = '';
  quantity = 1;
  isLoggedIn = false;

  newRating = 5;
  newTitle = '';
  newComment = '';
  reviewSubmitting = false;
  reviewMsg = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.authService.isLoggedIn$.subscribe(v => this.isLoggedIn = v);

    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadProduct(+id);
  }

  loadProduct(id: number) {
    this.productService.getProductById(id).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.product = res.data || res;
        if (this.product) {
          this.trackProductView(this.product);
          this.loadReviews(this.product.id);
          this.loadInventory(this.product.id);
          this.loadRecommendations(this.product);
          if (this.isLoggedIn) this.checkWishlist(this.product.id);
        }
      },
      error: () => { this.loading = false; }
    });
  }

  trackProductView(product: any) {
    try {
      const key = 'esz_product_views';
      const raw = localStorage.getItem(key);
      const views: { [id: number]: { count: number; name: string; categoryName: string; price: number; imageUrl: string } }
        = raw ? JSON.parse(raw) : {};
      const existing = views[product.id];
      views[product.id] = {
        count: (existing?.count || 0) + 1,
        name: product.name,
        categoryName: product.categoryName || '',
        price: product.price || 0,
        imageUrl: product.imageUrl || ''
      };
      localStorage.setItem(key, JSON.stringify(views));
    } catch {}
  }

  loadReviews(id: number) {
    this.productService.getReviews(id).subscribe({
      next: (res: any) => {
        const page = res.data || res;
        this.reviews = page.content || [];
      },
      error: () => {}
    });
    this.productService.getReviewSummary(id).subscribe({
      next: (res: any) => { this.reviewSummary = res.data || res; },
      error: () => {}
    });
  }

  loadInventory(id: number) {
    this.productService.getInventory(id).subscribe({
      next: (res: any) => { this.inventory = res.data || res; },
      error: () => {}
    });
  }

  checkWishlist(id: number) {
    this.wishlistService.checkInWishlist(id).subscribe({
      next: (res: any) => {
        const data = res.data || res;
        this.inWishlist = data.inWishlist || false;
      },
      error: () => {}
    });
  }

  addToCart() {
    if (!this.isLoggedIn) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    if (!this.product) return;
    this.cartLoading = true;
    this.cartMessage = '';
    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: () => {
        this.cartLoading = false;
        this.cartMessage = '✓ Added to cart successfully!';
        setTimeout(() => this.cartMessage = '', 3000);
      },
      error: (err) => {
        this.cartLoading = false;
        this.cartMessage = err.error?.message || 'Failed to add to cart';
      }
    });
  }

  toggleWishlist() {
    if (!this.isLoggedIn) { this.router.navigate(['/login']); return; }
    if (!this.product) return;
    this.wishlistLoading = true;
    if (this.inWishlist) {
      this.wishlistService.removeFromWishlist(this.product.id).subscribe({
        next: () => { this.inWishlist = false; this.wishlistLoading = false; },
        error: () => { this.wishlistLoading = false; }
      });
    } else {
      this.wishlistService.addToWishlist(this.product.id).subscribe({
        next: () => { this.inWishlist = true; this.wishlistLoading = false; },
        error: () => { this.wishlistLoading = false; }
      });
    }
  }

  submitReview() {
    if (!this.isLoggedIn) { this.router.navigate(['/login']); return; }
    if (!this.product) return;
    this.reviewSubmitting = true;
    this.productService.submitReview({
      productId: this.product.id,
      rating: this.newRating,
      title: this.newTitle,
      comment: this.newComment
    }).subscribe({
      next: () => {
        this.reviewSubmitting = false;
        this.reviewMsg = 'Review submitted! It will appear after approval.';
        this.newTitle = ''; this.newComment = ''; this.newRating = 5;
      },
      error: (err) => {
        this.reviewSubmitting = false;
        this.reviewMsg = err.error?.message || 'Failed to submit review';
      }
    });
  }

  loadRecommendations(product: Product) {
    const category = (product as any).category || (product as any).categoryName;
    if (!category) return;
    this.productService.getProducts(0, 8, category).subscribe({
      next: (res: any) => {
        const page = res.data || res;
        const all = page.content || [];
        this.recommendations = all.filter((p: Product) => p.id !== product.id).slice(0, 4);
      },
      error: () => {}
    });
  }

  getStarArray(n: number): number[] {
    return Array(Math.min(5, Math.round(n || 0))).fill(0);
  }

  getEmptyStarArray(n: number): number[] {
    return Array(5 - Math.min(5, Math.round(n || 0))).fill(0);
  }

  getOriginalPrice(price: number): number {
    return Math.round(price * 1.15);
  }

  getDiscountPct(price: number): number {
    const orig = this.getOriginalPrice(price);
    return Math.round(((orig - price) / orig) * 100);
  }

  formatPrice(price: number): string {
    return '₹' + price.toLocaleString('en-IN');
  }

  isInStock(): boolean {
    if (!this.inventory) return true;
    return this.inventory.availableQuantity > 0;
  }
}
