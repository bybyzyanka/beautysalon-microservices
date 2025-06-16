package ua.nure.beautysalon.client.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nure.beautysalon.client.dto.ClientDTO;
import ua.nure.beautysalon.client.entity.Client;
import ua.nure.beautysalon.client.feign.ScheduleServiceClient;
import ua.nure.beautysalon.client.mapper.ClientMapper;
import ua.nure.beautysalon.client.repository.ClientRepository;
import ua.nure.beautysalon.client.service.ClientService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final ScheduleServiceClient scheduleServiceClient;

    @Override
    public ClientDTO addClient(ClientDTO clientDTO) {
        if (clientRepository.existsByPhoneOrEmail(clientDTO.getPhone(), clientDTO.getEmail())) {
            throw new IllegalArgumentException("Client with this email or phone number already exists");
        }

        Client clientEntity = clientMapper.toEntity(clientDTO);
        Client savedClient = clientRepository.save(clientEntity);
        return clientMapper.toDTO(savedClient);
    }

    @Override
    public void deleteClientById(Long id) {
        try {
            scheduleServiceClient.deleteByClientId(id);
        } catch (Exception e) {
            // Log error but continue with deletion
        }
        clientRepository.deleteById(id);
    }

    @Override
    public Optional<ClientDTO> updateClient(Long id, ClientDTO clientDTO) {
        return clientRepository.findById(id).map(existingClient -> {
            existingClient.setName(clientDTO.getName());
            existingClient.setEmail(clientDTO.getEmail());
            existingClient.setPhone(clientDTO.getPhone());

            clientRepository.save(existingClient);
            return clientMapper.toDTO(existingClient);
        });
    }

    @Override
    public Optional<ClientDTO> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(clientMapper::toDTO);
    }

    @Override
    public Page<ClientDTO> getAllClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findAll(pageable)
                .map(clientMapper::toDTO);
    }

    @Override
    public Page<ClientDTO> searchClientsByNameOrEmail(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable)
                .map(clientMapper::toDTO);
    }
}
