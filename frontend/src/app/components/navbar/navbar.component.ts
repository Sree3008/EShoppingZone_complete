import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { CartService } from '../../services/cart.service';
import { ProductService } from '../../services/product.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit, OnDestroy {

  isLoggedIn = false;
  isAdmin = false;
  username = '';
  cartCount = 0;
  searchQuery = '';
  showDropdown = false;
  showCategories = false;
  suggestions: any[] = [];
  showSuggestions = false;
  searchSubject = new Subject<string>();

  notifications: any[] = [];
  unreadCount = 0;
  showNotifications = false;

  private pollInterval: any = null;

  categories: string[] = [
    'Mobiles', 'Laptops', 'Electronics', 'Fashion', 'Home & Living',
    'Beauty & Health', 'Sports', 'Toys & Kids', 'Books & Stationery', 'Groceries'
  ];

  categoryIcons: { [key: string]: string } = {
    'Mobiles': '📱', 'Laptops': '💻', 'Electronics': '🎧',
    'Fashion': '👗', 'Home & Living': '🏠', 'Beauty & Health': '💄',
    'Sports': '👟', 'Toys & Kids': '🧸', 'Books & Stationery': '📚',
    'Groceries': '🥦'
  };

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private productService: ProductService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    // Live search with 300ms debounce
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => q.length >= 2
        ? this.productService.getProducts(0, 6, undefined, q)
        : of(null)
      )
    ).subscribe({
      next: (res: any) => {
        if (!res) { this.suggestions = []; return; }
        const page = (res as any).data || res;
        this.suggestions = page.content || [];
        this.showSuggestions = this.suggestions.length > 0;
      },
      error: () => { this.suggestions = []; }
    });

    this.authService.isLoggedIn$.subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        const user = this.authService.getUser();
        this.username = user?.fullName || user?.username || '';
        const role = user?.role || '';
        this.isAdmin = role === 'ROLE_ADMIN' || role === 'ADMIN';
        this.cartService.getCart().subscribe();
        this.loadNotifications();
        if (!this.pollInterval) {
          this.pollInterval = setInterval(() => this.loadNotifications(), 60000);
        }
      } else {
        this.cartCount = 0;
        this.notifications = [];
        this.unreadCount = 0;
        if (this.pollInterval) {
          clearInterval(this.pollInterval);
          this.pollInterval = null;
        }
      }
    });

    this.cartService.cartCount$.subscribe(count => {
      this.cartCount = count;
    });

    this.productService.getCategories().subscribe({
      next: (res: any) => {
        const cats = Array.isArray(res) ? res : (res.data || []);
        if (cats.length > 0) this.categories = cats.map((c: any) => c.name);
      },
      error: () => {}
    });
  }

  ngOnDestroy() {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  loadNotifications() {
    this.notificationService.getMyNotifications().subscribe({
      next: (res: any) => {
        const list = res.data || res;
        this.notifications = Array.isArray(list) ? list : (list.content || []);
        this.unreadCount = this.notifications.filter((n: any) => !n.read).length;
      },
      error: () => {}
    });
  }

  markRead(id: number) {
    this.notificationService.markRead(id).subscribe({
      next: () => this.loadNotifications(),
      error: () => {}
    });
  }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => this.loadNotifications(),
      error: () => {}
    });
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    this.showDropdown = false;
    this.showCategories = false;
  }

  timeAgo(date: string | Date): string {
    if (!date) return '';
    const now = new Date();
    const then = new Date(date);
    const diffMs = now.getTime() - then.getTime();
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHr = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHr / 24);
    if (diffDay > 0) return `${diffDay}d ago`;
    if (diffHr > 0) return `${diffHr}h ago`;
    if (diffMin > 0) return `${diffMin}m ago`;
    return 'just now';
  }

  goToCategory(cat: string) {
    this.showCategories = false;
    this.router.navigate(['/products'], { queryParams: { category: cat } });
  }

  onSearchInput() {
    this.searchSubject.next(this.searchQuery.trim());
    if (!this.searchQuery.trim()) this.showSuggestions = false;
  }

  selectSuggestion(product: any) {
    this.showSuggestions = false;
    this.searchQuery = '';
    this.router.navigate(['/products', product.id]);
  }

  search() {
    const q = this.searchQuery.trim();
    this.showSuggestions = false;
    if (!q) return;
    const matchedCat = this.categories.find(c => c.toLowerCase() === q.toLowerCase());
    if (matchedCat) {
      this.router.navigate(['/products'], { queryParams: { category: matchedCat } });
    } else {
      this.router.navigate(['/products'], { queryParams: { keyword: q } });
    }
    this.searchQuery = '';
  }

  clearSuggestions() {
    // slight delay so click on suggestion registers first
    setTimeout(() => { this.showSuggestions = false; }, 180);
  }

  logout() {
    this.authService.logout();
    this.showDropdown = false;
  }

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
    this.showNotifications = false;
  }

  @HostListener('document:keydown.escape')
  closeAll() {
    this.showDropdown = false;
    this.showCategories = false;
    this.showSuggestions = false;
    this.showNotifications = false;
  }
}
