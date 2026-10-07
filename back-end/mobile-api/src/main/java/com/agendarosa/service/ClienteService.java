package com.agendarosa.service;

import com.agendarosa.dto.ClienteResponseDTO;
import com.agendarosa.model.Cliente;
import com.agendarosa.repository.ClienteRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de gerenciamento e consulta de informações de Clientes.
 */
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Busca o perfil do cliente pelo e-mail e retorna um DTO seguro.
     */
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPerfilPorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado com o e-mail: " + email));

        return ClienteResponseDTO.fromEntity(cliente);
    }
}
