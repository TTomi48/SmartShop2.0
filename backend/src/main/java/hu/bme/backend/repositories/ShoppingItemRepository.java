
package hu.bme.backend.repositories;

import hu.bme.backend.entities.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingItemRepository
        extends JpaRepository<ShoppingItem, Long> {

    List<ShoppingItem> findAllByOrderByCreatedAtAscIdAsc();

    List<ShoppingItem> findByPurchasedFalseOrderByCreatedAtAscIdAsc();

    List<ShoppingItem> findByPurchasedTrueOrderByCreatedAtAscIdAsc();

    long deleteByPurchasedTrue();
}
