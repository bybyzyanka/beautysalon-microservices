package ua.nure.beautysalon.client.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

import jakarta.validation.ConstraintViolationException;
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
        try {
            // Validate required fields
            if (clientDTO.getName() == null || clientDTO.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (clientDTO.getEmail() == null || clientDTO.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (clientDTO.getPhone() == null || clientDTO.getPhone().trim().isEmpty()) {
                throw new IllegalArgumentException("Phone number is required");
            }

            if (clientRepository.existsByPhoneOrEmail(clientDTO.getPhone(), clientDTO.getEmail())) {
                throw new IllegalArgumentException("A client with this email or phone number already exists");
            }

            Client clientEntity = clientMapper.toEntity(clientDTO);
            Client savedClient = clientRepository.save(clientEntity);
            return clientMapper.toDTO(savedClient);

        } catch (ConstraintViolationException ex) {
            // Extract the first constraint violation message
            String message = ex.getConstraintViolations()
                    .stream()
                    .findFirst()
                    .map(violation -> violation.getMessage())
                    .orElse("Validation failed");
            throw new IllegalArgumentException(message);

        } catch (DataIntegrityViolationException ex) {
            // Handle database constraint violations (unique constraints, etc.)
            if (ex.getMessage().contains("email")) {
                throw new IllegalArgumentException("A client with this email already exists");
            } else if (ex.getMessage().contains("phone")) {
                throw new IllegalArgumentException("A client with this phone number already exists");
            } else {
                throw new IllegalArgumentException("A client with this information already exists");
            }
        } catch (IllegalArgumentException ex) {
            // Re-throw our custom validation messages
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to create client: " + ex.getMessage());
        }
    }

    @Override
    public void deleteClientById(Long id) {
        try {
            if (!clientRepository.existsById(id)) {
                throw new IllegalArgumentException("Client not found with id: " + id);
            }

            scheduleServiceClient.deleteByClientId(id);
        } catch (Exception e) {
            // Log error but continue with deletion if it's a Feign client issue
            if (e.getMessage() != null && !e.getMessage().contains("Client not found")) {
                // Continue with deletion even if schedule service is unavailable
            } else {
                throw e; // Re-throw if it's our "not found" exception
            }
        }

        clientRepository.deleteById(id);
    }

    @Override
    public Optional<ClientDTO> updateClient(Long id, ClientDTO clientDTO) {
        return clientRepository.findById(id).map(existingClient -> {
            try {
                // Validate required fields
                if (clientDTO.getName() == null || clientDTO.getName().trim().isEmpty()) {
                    throw new IllegalArgumentException("Name is required");
                }
                if (clientDTO.getEmail() == null || clientDTO.getEmail().trim().isEmpty()) {
                    throw new IllegalArgumentException("Email is required");
                }
                if (clientDTO.getPhone() == null || clientDTO.getPhone().trim().isEmpty()) {
                    throw new IllegalArgumentException("Phone number is required");
                }

                // Check if email/phone already exists (excluding current client)
                boolean emailExists = clientRepository.findAll().stream()
                        .anyMatch(client -> !client.getId().equals(id) &&
                                client.getEmail().equalsIgnoreCase(clientDTO.getEmail()));
                boolean phoneExists = clientRepository.findAll().stream()
                        .anyMatch(client -> !client.getId().equals(id) &&
                                client.getPhone().equals(clientDTO.getPhone()));

                if (emailExists) {
                    throw new IllegalArgumentException("A client with this email already exists");
                }
                if (phoneExists) {
                    throw new IllegalArgumentException("A client with this phone number already exists");
                }

                existingClient.setName(clientDTO.getName());
                existingClient.setEmail(clientDTO.getEmail());
                existingClient.setPhone(clientDTO.getPhone());

                clientRepository.save(existingClient);
                return clientMapper.toDTO(existingClient);

            } catch (ConstraintViolationException ex) {
                String message = ex.getConstraintViolations()
                        .stream()
                        .findFirst()
                        .map(violation -> violation.getMessage())
                        .orElse("Validation failed");
                throw new IllegalArgumentException(message);

            } catch (DataIntegrityViolationException ex) {
                if (ex.getMessage().contains("email")) {
                    throw new IllegalArgumentException("A client with this email already exists");
                } else if (ex.getMessage().contains("phone")) {
                    throw new IllegalArgumentException("A client with this phone number already exists");
                } else {
                    throw new IllegalArgumentException("A client with this information already exists");
                }
            } catch (IllegalArgumentException ex) {
                // Re-throw our custom validation messages
                throw ex;
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed to update client: " + ex.getMessage());
            }
        });
    }

    @Override
    public Optional<ClientDTO> getClientById(Long id) {
        try {
            return clientRepository.findById(id)
                    .map(clientMapper::toDTO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve client: " + ex.getMessage());
        }
    }

    @Override
    public Page<ClientDTO> getAllClients(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return clientRepository.findAll(pageable)
                    .map(clientMapper::toDTO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to retrieve clients: " + ex.getMessage());
        }
    }

    @Override
    public Page<ClientDTO> searchClientsByNameOrEmail(String searchTerm, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return clientRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable)
                    .map(clientMapper::toDTO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to search clients: " + ex.getMessage());
        }
    }
}