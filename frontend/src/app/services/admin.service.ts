import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private baseUrl = 'http://localhost:8099';

  constructor(private http: HttpClient) {}

  // ─── Users ───────────────────────────────────────────────────────────────

  getUsers(role?: string, status?: string): Observable<any> {
    let params = new HttpParams();
    if (role) params = params.set('role', role);
    if (status) params = params.set('status', status);
    return this.http.get(`${this.baseUrl}/api/v1/auth/admin/users`, { params });
  }

  getUserById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/auth/admin/users/${id}`);
  }

  updateUserStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/auth/admin/users/${id}/status`, { status });
  }

  createStaffUser(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/auth/admin/users`, data);
  }

  // ─── Products ─────────────────────────────────────────────────────────────

  getAllProducts(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/products`);
  }

  getPendingProducts(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/products/pending`);
  }

  approveProduct(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/products/${id}/approve`, {});
  }

  rejectProduct(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/products/${id}/reject`, {});
  }

  deactivateProduct(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/products/${id}/deactivate`, {});
  }

  getAdminCategories(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/products/categories`);
  }

  createCategory(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/admin/products/categories`, data);
  }

  updateCategory(id: number, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/products/categories/${id}`, data);
  }

  // ─── Orders ───────────────────────────────────────────────────────────────

  getAllOrders(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/orders`);
  }

  updateOrderStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/orders/${id}/status`, { status });
  }

  // ─── Returns ──────────────────────────────────────────────────────────────

  getAllReturns(status?: string): Observable<any> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get(`${this.baseUrl}/api/v1/admin/returns`, { params });
  }

  approveReturn(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/returns/${id}/approve`, {});
  }

  rejectReturn(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/returns/${id}/reject`, { reason });
  }

  // ─── Deliveries ───────────────────────────────────────────────────────────

  getAllDeliveries(status?: string): Observable<any> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get(`${this.baseUrl}/api/v1/admin/deliveries`, { params });
  }

  assignDelivery(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/admin/deliveries/assign`, data);
  }

  // ─── Reviews ──────────────────────────────────────────────────────────────

  getPendingReviews(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/reviews/pending`);
  }

  getAllReviews(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/reviews`);
  }

  approveReview(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/reviews/${id}/approve`, {});
  }

  rejectReview(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/reviews/${id}/reject`, { reason });
  }

  hideReview(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/reviews/${id}/hide`, {});
  }

  // ─── Refunds ──────────────────────────────────────────────────────────────

  getPendingRefunds(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/refunds/pending`);
  }

  approveRefund(id: number): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/refunds/${id}/approve`, {});
  }

  rejectRefund(id: number, reason: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/refunds/${id}/reject`, { reason });
  }

  // ─── Audit Logs ───────────────────────────────────────────────────────────

  getAuditLogs(params?: any): Observable<any> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(k => {
        if (params[k] !== null && params[k] !== undefined && params[k] !== '') {
          httpParams = httpParams.set(k, params[k]);
        }
      });
    }
    return this.http.get(`${this.baseUrl}/api/v1/audit-logs`, { params: httpParams });
  }

  // ─── Returns (extra) ─────────────────────────────────────────────────────

  getReturnById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/admin/returns/${id}`);
  }

  updateReturnStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/api/v1/admin/returns/${id}/status`, { status });
  }

  retryRestock(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/admin/returns/${id}/retry-restock`, {});
  }

  retryRefund(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/admin/returns/${id}/retry-refund`, {});
  }

  // ─── Settlements ──────────────────────────────────────────────────────────

  runWeeklySettlement(): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/settlements/weekly/run`, {});
  }

  confirmCodReceipt(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/settlements/cod/confirm`, data);
  }

  // ─── Notifications ────────────────────────────────────────────────────────

  getUserNotifications(userId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/notifications/user/${userId}`);
  }

  // ─── Inventory ────────────────────────────────────────────────────────────

  getInventory(productId: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/v1/inventory/product/${productId}/movements`);
  }

  addStock(productId: number, quantity: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/v1/inventory/product/${productId}/add`, { quantity });
  }
}
