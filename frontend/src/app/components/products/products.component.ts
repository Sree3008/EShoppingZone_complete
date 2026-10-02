import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { WishlistService } from '../../services/wishlist.service';
import { Product, Category } from '../../models/models';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent implements OnInit {

  products: Product[] = [];
  categories: Category[] = [];
  loading = true;
  isLoggedIn = false;
  cartMessages: { [key: number]: string } = {};
  wishlistedIds = new Set<number>();
  wishlistMessages: { [key: number]: string } = {};

  searchKeyword = '';
  selectedCategory = '';
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 12;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private wishlistService: WishlistService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) this.loadWishlist();
    this.authService.isLoggedIn$.subscribe(v => {
      this.isLoggedIn = v;
      if (v) this.loadWishlist();
    });

    this.route.queryParams.subscribe(params => {
      this.searchKeyword = params['keyword'] || '';
      this.selectedCategory = params['category'] || '';
      this.currentPage = 0;
      this.loadProducts();
    });

    this.loadCategories();
  }

  loadProducts() {
    this.loading = true;
    this.productService.getProducts(
      this.currentPage, this.pageSize,
      this.selectedCategory || undefined,
      this.searchKeyword || undefined
    ).subscribe({
      next: (res: any) => {
        this.loading = false;
        const page = res.data || res;
        this.products = page.content || [];
        this.totalPages = page.totalPages || 0;
        this.totalElements = page.totalElements || 0;
      },
      error: () => {
        this.loading = false;
        this.products = [];
      }
    });
  }

  loadWishlist() {
    this.wishlistService.getWishlist().subscribe({
      next: (res: any) => {
        const items = res.data?.items || res.items || res.data || [];
        this.wishlistedIds = new Set(items.map((i: any) => i.productId));
      },
      error: () => {}
    });
  }

  toggleWishlist(event: Event, product: Product) {
    event.stopPropagation();
    if (!this.isLoggedIn) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/products' } });
      return;
    }
    if (this.wishlistedIds.has(product.id)) {
      this.wishlistService.removeFromWishlist(product.id).subscribe({
        next: () => {
          this.wishlistedIds.delete(product.id);
          this.showWishlistMsg(product.id, 'Removed from wishlist');
        },
        error: () => {}
      });
    } else {
      this.wishlistService.addToWishlist(product.id).subscribe({
        next: () => {
          this.wishlistedIds.add(product.id);
          this.showWishlistMsg(product.id, '❤️ Added to wishlist!');
        },
        error: () => {}
      });
    }
  }

  showWishlistMsg(productId: number, msg: string) {
    this.wishlistMessages[productId] = msg;
    setTimeout(() => delete this.wishlistMessages[productId], 2200);
  }

  loadCategories() {
    this.productService.getCategories().subscribe({
      next: (res: any) => {
        this.categories = Array.isArray(res) ? res : (res.data || []);
      },
      error: () => {}
    });
  }

  onSearch() {
    this.currentPage = 0;
    // If the keyword exactly matches a known category name, treat as category filter
    const kw = this.searchKeyword.trim().toLowerCase();
    const matchedCat = this.categories.find(c => c.name.toLowerCase() === kw);
    if (matchedCat) {
      this.selectedCategory = matchedCat.name;
      this.searchKeyword = '';
    }
    this.updateUrl();
    this.loadProducts();
  }

  onCategoryChange() {
    this.currentPage = 0;
    this.updateUrl();
    this.loadProducts();
  }

  updateUrl() {
    const q: any = {};
    if (this.searchKeyword) q['keyword'] = this.searchKeyword;
    if (this.selectedCategory) q['category'] = this.selectedCategory;
    this.router.navigate([], { queryParams: q });
  }

  clearFilters() {
    this.searchKeyword = '';
    this.selectedCategory = '';
    this.currentPage = 0;
    this.router.navigate(['/products']);
    this.loadProducts();
  }

  goToPage(page: number) {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    window.scrollTo(0, 0);
    this.loadProducts();
  }

  addToCart(event: Event, product: Product) {
    event.stopPropagation();
    if (!this.isLoggedIn) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/products' } });
      return;
    }
    this.cartService.addToCart(product.id, 1).subscribe({
      next: () => {
        this.cartMessages[product.id] = 'Added!';
        setTimeout(() => delete this.cartMessages[product.id], 2000);
      },
      error: () => {}
    });
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

  getPagesArray(): number[] {
    const pages = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, this.currentPage + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }
}
