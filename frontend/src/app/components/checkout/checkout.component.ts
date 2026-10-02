import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { CartService } from '../../services/cart.service';
import { Address } from '../../models/models';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent implements OnInit {

  addresses: Address[] = [];
  selectedAddressId: number | null = null;
  paymentMethod = 'COD';
  loading = false;
  addressLoading = true;
  orderPlaced = false;
  orderId: number | null = null;
  errorMsg = '';
  walletBalance = 0;

  // for adding new address
  showAddressForm = false;
  newAddress = {
    street: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'India',
    isDefault: false
  };

  constructor(private orderService: OrderService, private cartService: CartService, private router: Router) {}

  ngOnInit() {
    this.loadAddresses();
    this.loadWallet();
  }

  loadAddresses() {
    this.addressLoading = true;
    this.orderService.getMyAddresses().subscribe({
      next: (res: any) => {
        this.addressLoading = false;
        this.addresses = Array.isArray(res) ? res : (res.data || []);
        // auto select default address
        const defaultAddr = this.addresses.find(a => a.isDefault);
        if (defaultAddr) {
          this.selectedAddressId = defaultAddr.id;
        } else if (this.addresses.length > 0) {
          this.selectedAddressId = this.addresses[0].id;
        }
      },
      error: () => {
        this.addressLoading = false;
      }
    });
  }

  loadWallet() {
    this.orderService.getWallet().subscribe({
      next: (res: any) => {
        const wallet = res.data || res;
        this.walletBalance = wallet.balance || 0;
      },
      error: () => {}
    });
  }

  saveAddress() {
    this.orderService.addAddress(this.newAddress).subscribe({
      next: (res: any) => {
        this.showAddressForm = false;
        this.loadAddresses();
        this.newAddress = { street: '', city: '', state: '', postalCode: '', country: 'India', isDefault: false };
      },
      error: () => {}
    });
  }

  placeOrder() {
    if (!this.selectedAddressId) {
      this.errorMsg = 'Please select a delivery address';
      return;
    }

    this.loading = true;
    this.errorMsg = '';

    this.orderService.placeOrder({
      addressId: this.selectedAddressId,
      paymentMethod: this.paymentMethod
    }).subscribe({
      next: (res: any) => {
        this.loading = false;
        const order = res.data || res;
        this.orderId = order.id;
        this.orderPlaced = true;
        this.cartService.setCartCount(0);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err.error?.message || 'Failed to place order. Please try again.';
      }
    });
  }

  formatPrice(price: number): string {
    return '₹' + price.toLocaleString('en-IN');
  }
}
