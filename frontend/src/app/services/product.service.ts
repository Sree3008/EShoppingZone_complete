import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, Product, ProductPage, Review, ReviewSummary } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = 'http://localhost:8099/api/v1';

  constructor(private http: HttpClient) {}

  // get list of products with optional filters
  getProducts(page: number = 0, size: number = 12, category?: string, keyword?: string): Observable<ProductPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (category) {
      params = params.set('category', category);
    }
    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<ProductPage>(`${this.apiUrl}/products`, { params });
  }

  // get single product details
  getProductById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/products/${id}`);
  }

  // get all categories
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/products/categories`);
  }

  // get reviews for a product
  getReviews(productId: number, page: number = 0): Observable<any> {
    let params = new HttpParams().set('page', page.toString()).set('size', '5');
    return this.http.get<any>(`${this.apiUrl}/reviews/product/${productId}`, { params });
  }

  // get rating summary
  getReviewSummary(productId: number): Observable<ReviewSummary> {
    return this.http.get<ReviewSummary>(`${this.apiUrl}/reviews/product/${productId}/summary`);
  }

  // post a review (needs login)
  submitReview(data: { productId: number, rating: number, title: string, comment: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/reviews`, data);
  }

  // get inventory for product
  getInventory(productId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/inventory/product/${productId}`);
  }

  getMyReviews(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/reviews/my`);
  }

  deleteReview(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/reviews/${id}`);
  }
}
