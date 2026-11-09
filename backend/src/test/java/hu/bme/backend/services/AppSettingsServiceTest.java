package hu.bme.backend.services;

import hu.bme.backend.entities.AppSettings;
import hu.bme.backend.entities.Store;
import hu.bme.backend.exceptions.ResourceNotFoundException;
import hu.bme.backend.repositories.AppSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppSettingsServiceTest {

    @Mock
    private AppSettingsRepository appSettingsRepository;

    @Mock
    private StoreService storeService;

    @InjectMocks
    private AppSettingsService service;

    @Test
    void getSelectedStoreIdReturnsNullWhenSettingsDoNotExist() {
        when(appSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertNull(service.getSelectedStoreId());
    }

    @Test
    void getSelectedStoreIdReturnsNullWhenNoStoreIsSelected() {
        when(appSettingsRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(new AppSettings()));

        assertNull(service.getSelectedStoreId());
    }

    @Test
    void getSelectedStoreIdReturnsTheSavedSelection() {
        var settings = new AppSettings();
        settings.setSelectedStore(store(1L));
        when(appSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));

        assertEquals(1L, service.getSelectedStoreId());
    }

    @Test
    void selectStoreCreatesSettingsWhenTheyDoNotExist() {
        var store = store(1L);
        when(storeService.getStoreById(1L)).thenReturn(store);
        when(appSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        service.selectStore(1L);

        var saved = ArgumentCaptor.forClass(AppSettings.class);
        verify(appSettingsRepository).save(saved.capture());
        assertSame(store, saved.getValue().getSelectedStore());
    }

    @Test
    void selectStoreUpdatesTheExistingSettings() {
        var settings = new AppSettings();
        settings.setId(10L);
        settings.setSelectedStore(store(1L));
        var nextStore = store(2L);
        when(storeService.getStoreById(2L)).thenReturn(nextStore);
        when(appSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));

        service.selectStore(2L);

        assertEquals(10L, settings.getId());
        assertSame(nextStore, settings.getSelectedStore());
        verify(appSettingsRepository).save(settings);
    }

    @Test
    void selectStoreAllowsSelectingTheCurrentStoreAgain() {
        var store = store(1L);
        var settings = new AppSettings();
        settings.setSelectedStore(store);
        when(storeService.getStoreById(1L)).thenReturn(store);
        when(appSettingsRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(settings));

        service.selectStore(1L);

        assertSame(store, settings.getSelectedStore());
        verify(appSettingsRepository).save(settings);
    }

    @Test
    void selectStoreRejectsNullId() {
        assertThrows(IllegalArgumentException.class, () -> service.selectStore(null));

        verifyNoInteractions(storeService, appSettingsRepository);
    }

    @Test
    void selectStoreRejectsUnknownStoreWithoutSavingSettings() {
        when(storeService.getStoreById(99L))
                .thenThrow(new ResourceNotFoundException("Store not found"));

        assertThrows(ResourceNotFoundException.class, () -> service.selectStore(99L));

        verifyNoInteractions(appSettingsRepository);
    }

    private static Store store(Long id) {
        var store = new Store();
        store.setId(id);
        store.setName("Store " + id);
        return store;
    }
}
