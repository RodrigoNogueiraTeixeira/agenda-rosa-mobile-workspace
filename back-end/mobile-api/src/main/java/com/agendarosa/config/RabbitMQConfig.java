package com.agendarosa.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da infraestrutura de mensageria assíncrona com RabbitMQ.
 * Define filas, exchanges, bindings e serialização JSON para processamento não bloqueante de reservas.
 */
@Configuration
public class RabbitMQConfig {

    public static final String FILA_AGENDAMENTOS = "fila.agendamentos.reserva";
    public static final String EXCHANGE_AGENDAMENTOS = "exchange.agendamentos";
    public static final String ROUTING_KEY_AGENDAMENTO = "agendamento.reservar";

    /**
     * Declara a fila durável onde os pedidos de reserva aguardam processamento.
     * Fila durável garante que mensagens não se percam em caso de reinício do broker.
     */
    @Bean
    public Queue filaAgendamentos() {
        return QueueBuilder.durable(FILA_AGENDAMENTOS).build();
    }

    /**
     * Declara a Topic Exchange que direciona os pedidos com base na routing key.
     */
    @Bean
    public TopicExchange exchangeAgendamentos() {
        return new TopicExchange(EXCHANGE_AGENDAMENTOS);
    }

    /**
     * Liga a fila de agendamentos à exchange através da routing key 'agendamento.reservar'.
     */
    @Bean
    public Binding bindingAgendamentos(Queue filaAgendamentos, TopicExchange exchangeAgendamentos) {
        return BindingBuilder.bind(filaAgendamentos)
                .to(exchangeAgendamentos)
                .with(ROUTING_KEY_AGENDAMENTO);
    }

    /**
     * Serializador JSON para converter automaticamente os DTOs Java ao trafegar mensagens.
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurado com o conversor JSON para publicar mensagens estruturadas.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
