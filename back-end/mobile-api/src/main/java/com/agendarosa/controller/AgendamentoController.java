package com.agendarosa.controller;

import com.agendarosa.dto.AgendamentoEnfileiradoResponseDTO;
import com.agendarosa.dto.AgendamentoMensagemDTO;
import com.agendarosa.dto.AgendamentoResponseDTO;
import com.agendarosa.dto.CriarAgendamentoRequestDTO;
import com.agendarosa.producer.AgendamentoProducer;
import com.agendarosa.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para criação e consulta de agendamentos.
 * Utiliza RabbitMQ para processar reservas de forma assíncrona e não bloqueante.
 * Todas as rotas exigem autenticação via Bearer Token JWT.
 */
@RestController
@RequestMapping("/api/v1/agendamentos")
public class AgendamentoController {

    private final AgendamentoProducer agendamentoProducer;
    private final AgendamentoService agendamentoService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AgendamentoController(AgendamentoProducer agendamentoProducer, AgendamentoService agendamentoService) {
        this.agendamentoProducer = agendamentoProducer;
        this.agendamentoService = agendamentoService;
    }

    /**
     * Endpoint padrão de agendamento assíncrono via RabbitMQ.
     * Recebe a solicitação, valida formato/data futura e enfileira no broker sem travar o servidor HTTP.
     * Retorna HTTP 202 Accepted imediatamente com protocolo para rastreio.
     */
    @PostMapping
    public ResponseEntity<AgendamentoEnfileiradoResponseDTO> enfileirarAgendamento(
            Authentication authentication,
            @Valid @RequestBody CriarAgendamentoRequestDTO dto
    ) {
        String emailCliente = authentication.getName();

        // Validação fail-fast: impede que agendamentos retroativos entrem na fila
        LocalDate dataAgendamento = LocalDate.parse(dto.data());
        LocalTime horarioInicio = LocalTime.parse(dto.horarioInicio(), TIME_FORMATTER);
        LocalDateTime dataHoraCompleta = LocalDateTime.of(dataAgendamento, horarioInicio);

        if (dataHoraCompleta.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data e horário do agendamento devem ser futuros.");
        }

        String protocolo = UUID.randomUUID().toString();
        String agora = LocalDateTime.now().toString();

        AgendamentoMensagemDTO mensagem = new AgendamentoMensagemDTO(
                protocolo,
                emailCliente,
                dto,
                agora
        );

        // Despacha para a fila do RabbitMQ
        agendamentoProducer.enfileirarReserva(mensagem);

        AgendamentoEnfileiradoResponseDTO resposta = new AgendamentoEnfileiradoResponseDTO(
                protocolo,
                "PROCESSANDO",
                "Pedido de reserva recebido com sucesso e enfileirado no RabbitMQ para processamento.",
                emailCliente,
                dto.data(),
                dto.horarioInicio(),
                agora
        );

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resposta);
    }

    /**
     * Endpoint síncrono alternativo para testes diretos ou integrações sem mensageria.
     * Retorna HTTP 201 Created.
     */
    @PostMapping("/sincrono")
    public ResponseEntity<AgendamentoResponseDTO> criarAgendamentoSincrono(
            Authentication authentication,
            @Valid @RequestBody CriarAgendamentoRequestDTO dto
    ) {
        String emailCliente = authentication.getName();
        AgendamentoResponseDTO response = agendamentoService.criarAgendamento(emailCliente, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para o cliente autenticado consultar seus próprios agendamentos.
     * Retorna HTTP 200 OK.
     */
    @GetMapping("/me")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarMeusAgendamentos(
            Authentication authentication
    ) {
        String emailCliente = authentication.getName();
        List<AgendamentoResponseDTO> agendamentos = agendamentoService.listarAgendamentosDoCliente(emailCliente);
        return ResponseEntity.ok(agendamentos);
    }
}
