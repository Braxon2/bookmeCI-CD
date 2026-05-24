package com.dusanbranovic.bookme.repository;

import com.dusanbranovic.bookme.models.BookingAddonItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAddonItemRepository extends JpaRepository<BookingAddonItem,Long> {
}
