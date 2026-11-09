package hu.bme.backend.repositories;

import hu.bme.backend.entities.StoreCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreCategoryRepository
        extends JpaRepository<StoreCategory, Long> {

    List<StoreCategory> findByStoreIdOrderByPositionAsc(Long storeId);

    Optional<StoreCategory> findByStoreIdAndCategoryId(
            Long storeId,
            Long categoryId
    );

    boolean existsByStoreIdAndCategoryId(
            Long storeId,
            Long categoryId
    );
}