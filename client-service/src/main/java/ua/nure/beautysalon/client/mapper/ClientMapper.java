package ua.nure.beautysalon.client.mapper;

import org.springframework.stereotype.Component;
import ua.nure.beautysalon.client.dto.ClientDTO;
import ua.nure.beautysalon.client.entity.Client;

@Component
public class ClientMapper {

    public ClientDTO toDTO(Client client) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(client.getId());
        clientDTO.setName(client.getName());
        clientDTO.setEmail(client.getEmail());
        clientDTO.setPhone(client.getPhone());
        return clientDTO;
    }

    public Client toEntity(ClientDTO clientDTO) {
        Client client = new Client();
        client.setId(clientDTO.getId());
        client.setName(clientDTO.getName());
        client.setEmail(clientDTO.getEmail());
        client.setPhone(clientDTO.getPhone());
        return client;
    }
}