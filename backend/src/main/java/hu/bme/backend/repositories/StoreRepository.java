package hu.bme.backend.repositories;

import hu.bme.backend.entities.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository
        extends JpaRepository<Store, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Store> findByNameIgnoreCase(String name);

    List<Store> findAllByOrderByNameAsc();
}