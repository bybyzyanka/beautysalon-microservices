package ua.nure.beautysalon.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.beautysalon.client.dto.ClientDTO;
import ua.nure.beautysalon.client.service.ClientService;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@Tag(name = "Client", description = "Client management APIs")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    @Operation(summary = "Get all clients", description = "Retrieve paginated list of clients with optional search")
    public Page<ClientDTO> getAllClients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (search != null && !search.isEmpty()) {
            return clientService.searchClientsByNameOrEmail(search, page, size);
        }

        return clientService.getAllClients(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID", description = "Retrieve a specific client by their ID")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete client", description = "Delete a client by their ID")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClientById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update client", description = "Update an existing client")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @RequestBody ClientDTO clientDTO) {
        return clientService.updateClient(id, clientDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Add client", description = "Create a new client")
    public ResponseEntity<ClientDTO> addClient(@RequestBody ClientDTO clientDTO) {
        ClientDTO createdClient = clientService.addClient(clientDTO);
        return ResponseEntity.ok(createdClient);
    }
}