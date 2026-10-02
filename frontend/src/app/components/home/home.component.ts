import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { WishlistService } from '../../services/wishlist.service';
import { Product, Category } from '../../models/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy {

  featuredProducts: Product[] = [];
  recommendedProducts: any[] = [];
  categories: Category[] = [];
  loading = true;
  isLoggedIn = false;
  cartMessages: { [key: number]: string } = {};
  wishlistedIds = new Set<number>();
  wishlistMessages: { [key: number]: string } = {};

  currentSlide = 0;
  slideTimer: any;
  heroFading = false;

  banners = [
    {
      title: 'Upgrade Your Style,',
      highlight: 'Everyday',
      desc: 'Discover the latest trends, best deals and premium quality products — all in one place.',
      image: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1600&q=90'
    },
    {
      title: 'Flash Sale —',
      highlight: 'Up to 60% OFF',
      desc: 'Limited time offers on premium fashion, electronics and accessories. Shop before it ends!',
      image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1600&q=90'
    },
    {
      title: 'New Season,',
      highlight: 'New Collection',
      desc: 'Fresh styles arriving daily. Be the first to grab the newest looks across all categories.',
      image: 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=1600&q=90'
    },
    {
      title: 'Festive Season,',
      highlight: 'Best Deals',
      desc: 'Celebrate in style with exclusive festive offers on ethnic wear, gifts and home decor.',
      image: 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1600&q=90'
    },
    {
      title: 'Active Life,',
      highlight: 'Active Style',
      desc: 'High-performance sportswear for gym, running, yoga and everything in between.',
      image: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=1600&q=90'
    }
  ];

  categoryIcons: { [key: string]: string } = {
    'Mobiles': '📱', 'Laptops': '💻', 'Electronics': '🎧',
    'Fashion': '👗', 'Home & Living': '🏠', 'Beauty & Health': '💄',
    'Sports': '👟', 'Toys & Kids': '🧸', 'Books & Stationery': '📚',
    'Groceries': '🥦', "Men's Wear": '👔', "Women's Wear": '👗',
    'Kids': '🧒', 'Ethnic Wear': '🥻', 'Sportswear': '🏃',
    'Winter Wear': '🧥', 'Formals': '💼', 'Accessories': '👜'
  };

  categoryColors: string[] = [
    '#f3e8ff', '#e0f2fe', '#fce7f3', '#fef3c7',
    '#d1fae5', '#fee2e2', '#e0e7ff', '#fef9c3',
    '#ede9ff', '#dcfce7'
  ];

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private wishlistService: WishlistService,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    if (this.isLoggedIn) this.loadWishlist();
    this.authService.isLoggedIn$.subscribe(v => {
      this.isLoggedIn = v;
      if (v) this.loadWishlist();
    });
    this.loadCategories();
    this.loadFeaturedProducts();
    this.loadRecommendedProducts();
    this.startSlider();
  }

  ngOnDestroy() { clearInterval(this.slideTimer); }

  pauseSlider() { clearInterval(this.slideTimer); }

  startSlider() {
    clearInterval(this.slideTimer);
    this.slideTimer = setInterval(() => this.nextSlide(), 4500);
  }

  prevSlide() {
    clearInterval(this.slideTimer);
    this.changeSlide((this.currentSlide - 1 + this.banners.length) % this.banners.length);
    this.startSlider();
  }

  nextSlide() {
    clearInterval(this.slideTimer);
    this.changeSlide((this.currentSlide + 1) % this.banners.length);
    this.startSlider();
  }

  setSlide(i: number) {
    clearInterval(this.slideTimer);
    this.changeSlide(i);
    this.startSlider();
  }

  changeSlide(n: number) {
    this.heroFading = true;
    setTimeout(() => {
      this.currentSlide = n;
      this.heroFading = false;
    }, 200);
  }

  loadCategories() {
    this.productService.getCategories().subscribe({
      next: (res: any) => {
        this.categories = Array.isArray(res) ? res : (res.data || []);
      },
      error: () => {}
    });
  }

  loadFeaturedProducts() {
    this.productService.getProducts(0, 20).subscribe({
      next: (res: any) => {
        this.loading = false;
        const page = res.data || res;
        this.featuredProducts = page.content || [];
      },
      error: () => { this.loading = false; }
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
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/' } });
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

  loadRecommendedProducts() {
    try {
      const raw = localStorage.getItem('esz_product_views');
      if (!raw) return;
      const views = JSON.parse(raw);
      this.recommendedProducts = Object.entries(views)
        .filter(([, v]: any) => v.count >= 3)
        .sort((a: any, b: any) => b[1].count - a[1].count)
        .slice(0, 8)
        .map(([id, v]: any) => ({ id: +id, ...v }));
    } catch {}
  }

  goToCategory(name: string) {
    this.router.navigate(['/products'], { queryParams: { category: name } });
  }

  addToCart(event: Event, product: Product) {
    event.stopPropagation();
    if (!this.isLoggedIn) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/' } });
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

  getCategoryIcon(name: string): string { return this.categoryIcons[name] || '🛍️'; }
  getCategoryColor(i: number): string { return this.categoryColors[i % this.categoryColors.length]; }

  getOriginalPrice(price: number): number { return Math.round(price * 1.15); }
  getDiscountPct(price: number): number {
    const orig = this.getOriginalPrice(price);
    return Math.round(((orig - price) / orig) * 100);
  }
  formatPrice(price: number): string { return '₹' + price.toLocaleString('en-IN'); }
}
