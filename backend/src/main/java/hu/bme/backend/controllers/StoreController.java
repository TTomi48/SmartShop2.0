package hu.bme.backend.controllers;

import hu.bme.backend.dto.CreateStoreRequest;
import hu.bme.backend.dto.NamedResponse;
import hu.bme.backend.services.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @GetMapping
    public List<NamedResponse> getAllStores() {
        return storeService.getAllStores().stream().map(NamedResponse::from).toList();
    }

    @GetMapping("/{id}")
    public NamedResponse getById(@PathVariable Long id) {
        return NamedResponse.from(storeService.getStoreById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NamedResponse create(@Valid @RequestBody CreateStoreRequest request) {
        return NamedResponse.from(storeService.createStore(request.name()));
    }
}
