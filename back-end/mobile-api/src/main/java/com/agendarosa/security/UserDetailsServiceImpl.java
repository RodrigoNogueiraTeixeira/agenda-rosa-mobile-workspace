package com.agendarosa.security;

import com.agendarosa.model.Cliente;
import com.agendarosa.repository.ClienteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementação do UserDetailsService do Spring Security.
 * Localiza o cliente no banco de dados e monta o UserDetails com credenciais e permissões.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public UserDetailsServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado com o e-mail: " + email));

        return new User(
                cliente.getEmail(),
                cliente.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }
}
