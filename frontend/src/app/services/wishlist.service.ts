import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {

  private apiUrl = 'http://localhost:8099/api/v1/wishlist';

  constructor(private http: HttpClient) {}

  getWishlist(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  addToWishlist(productId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/items`, { productId });
  }

  removeFromWishlist(productId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/items/${productId}`);
  }

  checkInWishlist(productId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/check/${productId}`);
  }
}
