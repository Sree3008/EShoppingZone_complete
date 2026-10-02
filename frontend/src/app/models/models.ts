export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  status: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string;
  categoryId: number;
  categoryName: string;
  merchantId: number;
  status: string;
  sku: string;
  createdAt: string;
}

export interface Category {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
}

export interface ProductPage {
  content: Product[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  productImage?: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  priceAtAddition?: number;
  totalPrice?: number;
}

export interface Cart {
  id: number;
  customerId: number;
  items: CartItem[];
  totalAmount?: number;
  totalPrice?: number;
  updatedAt: string;
}

export interface Address {
  id: number;
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface Order {
  id: number;
  customerId: number;
  status: string;
  items: OrderItem[];
  totalPrice: number;
  paymentMethod: string;
  shippingStreet: string;
  shippingCity: string;
  shippingState: string;
  shippingPostalCode: string;
  shippingCountry: string;
  createdAt: string;
}

export interface WishlistItem {
  id: number;
  productId: number;
  productName: string;
  productImage: string;
  productPrice: number;
  addedAt: string;
}

export interface Wishlist {
  id: number;
  customerId: number;
  items: WishlistItem[];
}

export interface Review {
  id: number;
  productId: number;
  customerId: number;
  customerName: string;
  rating: number;
  title: string;
  comment: string;
  createdAt: string;
}

export interface ReviewSummary {
  productId: number;
  averageRating: number;
  totalReviews: number;
  ratingDistribution: { [key: number]: number };
}

export interface Wallet {
  id: number;
  userId: number;
  balance: number;
}
