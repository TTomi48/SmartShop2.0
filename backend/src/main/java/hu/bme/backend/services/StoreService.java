package hu.bme.backend.services;


import hu.bme.backend.entities.Store;
import hu.bme.backend.exceptions.DuplicateResourcesException;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public List<Store> getAllStores() {
        return storeRepository.findAllByOrderByNameAsc();
    }

    public Store getStoreById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Store with id null");
        }

        var store = storeRepository.findById(id).orElse(null);
        if (store == null) {
            throw new ResourceNotFoundException("Store not found with ID:" + id);
        }
        return store;
    }

    @Transactional
    public Store createStore(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Store name is null or empty");
        }

        String normalizedName = name.trim();

        if (storeRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourcesException("Store with name " + name + " already exists");
        }

        Store store = new Store();
        store.setName(normalizedName);

        return storeRepository.save(store);
    }
}

