import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-my-reviews',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-reviews.component.html',
  styleUrl: './my-reviews.component.css'
})
export class MyReviewsComponent implements OnInit {
  reviews: any[] = [];
  loading = true;
  deletingId: number | null = null;

  constructor(private productService: ProductService) {}

  ngOnInit() {
    this.loadReviews();
  }

  loadReviews() {
    this.loading = true;
    this.productService.getMyReviews().subscribe({
      next: (res: any) => {
        const data = res.data || res;
        this.reviews = Array.isArray(data) ? data : (data.content || []);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  deleteReview(id: number) {
    if (!confirm('Delete this review?')) return;
    this.deletingId = id;
    this.productService.deleteReview(id).subscribe({
      next: () => {
        this.reviews = this.reviews.filter(r => r.id !== id);
        this.deletingId = null;
      },
      error: () => { this.deletingId = null; }
    });
  }

  getStars(rating: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }

  getStatusColor(status: string): string {
    const map: any = {
      APPROVED: '#16a34a',
      PENDING: '#f59e0b',
      REJECTED: '#dc2626'
    };
    return map[status] || '#6b7280';
  }
}
