package com.codzilla.sqlservice.SqlService.kafka;

import com.codzilla.sqlservice.SqlService.Dto.SubmissionKafkaMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public static final String SUBMISSION_TOPIC = "sql.submissions";
    // Партиции > 1 — параллельная обработка посылок разных задач
    // Но порядок внутри одной партиции гарантирован → kafka_offset корректен
    public static final int PARTITIONS = 4;

    // ───────────────────────── Topic ─────────────────────────

    @Bean
    public NewTopic submissionTopic() {
        return TopicBuilder.name(SUBMISSION_TOPIC)
                .partitions(PARTITIONS)
                .replicas(1)           // для dev; в prod → 3
                .build();
    }

    // ───────────────────────── Producer ──────────────────────

    @Bean
    public ProducerFactory<String, SubmissionKafkaMessage> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Гарантия доставки: ждём подтверждения от всех реплик
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Повторная отправка при временных сбоях
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, SubmissionKafkaMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ Consumer ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  ─

    @Bean
    public ConsumerFactory<String, SubmissionKafkaMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sql-judge-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Читаем с последнего подтверждённого offset при старте
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.codzilla.sqlservice.SqlService.kafka"); // Разрешаем наш DTO (иначе JsonDeserializer откажется его читать)
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SubmissionKafkaMessage> kafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, SubmissionKafkaMessage>();
        factory.setConsumerFactory(consumerFactory());
        // MANUAL_IMMEDIATE — коммитим offset сразу после acknowledge()
        factory.getContainerProperties().setAckMode(
            ContainerProperties.AckMode.MANUAL_IMMEDIATE
        );
        factory.setConcurrency(PARTITIONS); // определяет количество независимых потоков (консьюмеров), которые приложение запускает параллельно для чтения сообщений из топиков
        return factory;
    }
}