import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(): boolean {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        const role: string = user.role || '';
        if (role === 'ROLE_ADMIN' || role === 'ADMIN') {
          return true;
        }
      } catch (e) {}
    }
    this.router.navigate(['/']);
    return false;
  }
}
