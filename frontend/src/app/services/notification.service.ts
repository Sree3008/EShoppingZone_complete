import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private apiUrl = 'http://localhost:8099/api/v1';

  constructor(private http: HttpClient) {}

  getMyNotifications(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/notifications/my`);
  }

  markRead(id: number): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/notifications/${id}/read`, {});
  }

  markAllRead(): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/notifications/read-all`, {});
  }
}
