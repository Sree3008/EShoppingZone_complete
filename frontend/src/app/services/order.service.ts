import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private apiUrl = 'http://localhost:8099/api/v1';

  constructor(private http: HttpClient) {}

  placeOrder(data: { addressId?: number, paymentMethod: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/orders`, data);
  }

  getMyOrders(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/orders`);
  }

  getOrderById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/orders/${id}`);
  }

  cancelOrder(id: number, reason?: string): Observable<any> {
    let url = `${this.apiUrl}/orders/${id}/cancel`;
    if (reason) {
      url += `?reason=${encodeURIComponent(reason)}`;
    }
    return this.http.put<any>(url, {});
  }

  getMyAddresses(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/profiles/addresses`);
  }

  addAddress(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/profiles/addresses`, data);
  }

  getMyProfile(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/profiles/me`);
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/profiles/me`, data);
  }

  getWallet(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/wallet`);
  }

  topupWallet(amount: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/wallet/topup`, { amount });
  }

  getDelivery(orderId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/deliveries/order/${orderId}`);
  }

  createReturn(data: { orderId: number; orderItemId?: number; quantity?: number; reason: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/returns`, data);
  }

  getMyReturns(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/returns/my`);
  }

  cancelReturn(id: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/returns/${id}/cancel`, {});
  }

  getWalletTransactions(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/wallet/transactions`);
  }

  getMyRefunds(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/refunds/my`);
  }

  deleteAddress(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/profiles/addresses/${id}`);
  }

  updateAddress(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/profiles/addresses/${id}`, data);
  }

  setDefaultAddress(id: number): Observable<any> {
    return this.http.patch<any>(`${this.apiUrl}/profiles/addresses/${id}/default`, {});
  }
}
