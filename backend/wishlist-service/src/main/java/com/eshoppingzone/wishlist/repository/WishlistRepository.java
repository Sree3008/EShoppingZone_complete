package com.eshoppingzone.wishlist.repository;

import com.eshoppingzone.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
}
