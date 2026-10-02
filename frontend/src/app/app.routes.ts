import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { ProductsComponent } from './components/products/products.component';
import { ProductDetailComponent } from './components/product-detail/product-detail.component';
import { CartComponent } from './components/cart/cart.component';
import { CheckoutComponent } from './components/checkout/checkout.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { OrdersComponent } from './components/orders/orders.component';
import { OrderDetailComponent } from './components/order-detail/order-detail.component';
import { ProfileComponent } from './components/profile/profile.component';
import { WishlistComponent } from './components/wishlist/wishlist.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { MyReturnsComponent } from './components/my-returns/my-returns.component';
import { WalletTransactionsComponent } from './components/wallet-transactions/wallet-transactions.component';
import { MyRefundsComponent } from './components/my-refunds/my-refunds.component';
import { MyReviewsComponent } from './components/my-reviews/my-reviews.component';
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { AdminLayoutComponent } from './components/admin/admin-layout/admin-layout.component';
import { AdminDashboardComponent } from './components/admin/admin-dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './components/admin/admin-users/admin-users.component';
import { AdminProductsComponent } from './components/admin/admin-products/admin-products.component';
import { AdminOrdersComponent } from './components/admin/admin-orders/admin-orders.component';
import { AdminReturnsComponent } from './components/admin/admin-returns/admin-returns.component';
import { AdminDeliveriesComponent } from './components/admin/admin-deliveries/admin-deliveries.component';
import { AdminReviewsComponent } from './components/admin/admin-reviews/admin-reviews.component';
import { AdminRefundsComponent } from './components/admin/admin-refunds/admin-refund.component';
import { AdminAuditLogsComponent } from './components/admin/admin-audit-logs/admin-audit-logs.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'products', component: ProductsComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'cart', component: CartComponent, canActivate: [AuthGuard] },
  { path: 'checkout', component: CheckoutComponent, canActivate: [AuthGuard] },
  { path: 'orders', component: OrdersComponent, canActivate: [AuthGuard] },
  { path: 'orders/:id', component: OrderDetailComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'wishlist', component: WishlistComponent, canActivate: [AuthGuard] },
  { path: 'returns', component: MyReturnsComponent, canActivate: [AuthGuard] },
  { path: 'wallet-transactions', component: WalletTransactionsComponent, canActivate: [AuthGuard] },
  { path: 'my-refunds', component: MyRefundsComponent, canActivate: [AuthGuard] },
  { path: 'my-reviews', component: MyReviewsComponent, canActivate: [AuthGuard] },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AdminGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'products', component: AdminProductsComponent },
      { path: 'orders', component: AdminOrdersComponent },
      { path: 'returns', component: AdminReturnsComponent },
      { path: 'deliveries', component: AdminDeliveriesComponent },
      { path: 'reviews', component: AdminReviewsComponent },
      { path: 'refunds', component: AdminRefundsComponent },
      { path: 'audit-logs', component: AdminAuditLogsComponent },
    ]
  },
  { path: '**', redirectTo: '' }
];
