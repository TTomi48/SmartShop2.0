package hu.bme.backend.services;

import hu.bme.backend.entities.AppSettings;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.AppSettingsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppSettingsService {
    private final AppSettingsRepository appSettingsRepository;
    private final StoreService storeService;

    public Long getSelectedStoreId() {
        var appSettings = appSettingsRepository
                .findFirstByOrderByIdAsc()
                .orElse(null);

        if (appSettings == null || appSettings.getSelectedStore() == null) {
            return null;
        }

        return appSettings.getSelectedStore().getId();
    }

    @Transactional
    public void selectStore(Long storeId) {
        if (storeId == null) {
            throw new IllegalArgumentException("Store ID cannot be null");
        }

        var store = storeService.getStoreById(storeId);

        var appSettings = appSettingsRepository
                .findFirstByOrderByIdAsc()
                .orElseGet(AppSettings::new);

        appSettings.setSelectedStore(store);

        appSettingsRepository.save(appSettings);
    }

}
