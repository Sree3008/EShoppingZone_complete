import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Cart } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private apiUrl = 'http://localhost:8099/api/v1/cart';

  // we use this to show cart count in navbar
  private cartCount = new BehaviorSubject<number>(0);
  cartCount$ = this.cartCount.asObservable();

  constructor(private http: HttpClient) {}

  getCart(): Observable<any> {
    return this.http.get<any>(this.apiUrl).pipe(
      tap(res => {
        if (res.data && res.data.items) {
          this.cartCount.next(res.data.items.length);
        }
      })
    );
  }

  addToCart(productId: number, quantity: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/items`, { productId, quantity }).pipe(
      tap(res => {
        if (res.data && res.data.items) {
          this.cartCount.next(res.data.items.length);
        }
      })
    );
  }

  updateQuantity(itemId: number, quantity: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/items/${itemId}`, { quantity });
  }

  removeItem(itemId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/items/${itemId}`).pipe(
      tap(res => {
        if (res.data && res.data.items) {
          this.cartCount.next(res.data.items.length);
        }
      })
    );
  }

  clearCart(): Observable<any> {
    return this.http.delete<any>(this.apiUrl).pipe(
      tap(() => this.cartCount.next(0))
    );
  }

  setCartCount(count: number) {
    this.cartCount.next(count);
  }
}
