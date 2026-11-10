package hu.bme.backend.controllers;

import hu.bme.backend.dto.SelectedStoreResponse;
import hu.bme.backend.dto.SelectStoreRequest;
import hu.bme.backend.services.AppSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class AppSettingsController {
    private final AppSettingsService appSettingsService;

    @GetMapping("/selected-store")
    public SelectedStoreResponse getSelectedStore() {
        return new SelectedStoreResponse(appSettingsService.getSelectedStoreId());
    }

    @PutMapping("/selected-store")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectStore(@Valid @RequestBody SelectStoreRequest request) {
        appSettingsService.selectStore(request.storeId());
    }
}
