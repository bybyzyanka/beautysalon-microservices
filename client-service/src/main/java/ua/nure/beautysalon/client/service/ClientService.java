package ua.nure.beautysalon.client.service;

import org.springframework.data.domain.Page;
import ua.nure.beautysalon.client.dto.ClientDTO;

import java.util.Optional;

public interface ClientService {
    ClientDTO addClient(ClientDTO clientDTO);
    void deleteClientById(Long id);
    Optional<ClientDTO> updateClient(Long id, ClientDTO clientDTO);
    Optional<ClientDTO> getClientById(Long id);
    Page<ClientDTO> getAllClients(int page, int size);
    Page<ClientDTO> searchClientsByNameOrEmail(String searchTerm, int page, int size);
}