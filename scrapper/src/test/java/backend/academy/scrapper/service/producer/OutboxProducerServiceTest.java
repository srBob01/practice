package backend.academy.scrapper.service.producer;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.config.ScrapperConfig.Kafka;
import backend.academy.scrapper.config.ScrapperConfig.Update.DispatcherType;
import backend.academy.scrapper.config.ScrapperConfig.Update.ProcessorType;
import backend.academy.scrapper.model.db.outbox.OutboxMessage;
import backend.academy.scrapper.service.base.OutboxService;
import backend.academy.scrapper.service.serialization.JsonSerializationService;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("deprecation")
@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OutboxProducerService.class, OutboxProducerServiceTest.TestConfig.class})
public class OutboxProducerServiceTest {

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.1"));

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public ScrapperConfig scrapperConfig() {
            return new ScrapperConfig(
                    "token",
                    new ScrapperConfig.StackOverflowCredentials("key", "token"),
                    "botApiUrl",
                    new ScrapperConfig.Timeout(1000, 1000),
                    new ScrapperConfig.Update(1000, ProcessorType.SEQUENTIAL, DispatcherType.KAFKA, 1, 1, 10),
                    new Kafka((int) Duration.ofSeconds(1).toMillis(), "test-topic", 1, (short) 1));
        }

        @Bean
        public OutboxService outboxService() {
            return mock(OutboxService.class);
        }

        @Bean
        public JsonSerializationService jsonSer() {
            return mock(JsonSerializationService.class);
        }

        @Bean
        public DefaultKafkaProducerFactory<Long, LinkUpdate> producerFactory() {
            Map<String, Object> props = new HashMap<>();
            props.put(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
            props.put(KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
            props.put(VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new org.springframework.kafka.core.DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<Long, LinkUpdate> kafkaTemplate(DefaultKafkaProducerFactory<Long, LinkUpdate> pf) {
            return new KafkaTemplate<>(pf);
        }
    }

    @Autowired
    private OutboxProducerService producerService;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private JsonSerializationService jsonSer;

    @Test
    @DisplayName("должно публиковать непроцессированные сообщения и помечать их обработанными")
    public void testPublishUnprocessed() throws Exception {
        // Arrange: подготовить одно непроцессированное сообщение
        Long messageId = 42L;
        String payload = "{\"id\":123,\"url\":\"http://example.com\",\"description\":\"desc\",\"chatIds\":[1]}";
        OutboxMessage msg = new OutboxMessage();
        msg.id(messageId);
        msg.topic("test-topic");
        msg.payload(payload);

        LinkUpdate linkUpdate = new LinkUpdate(123L, "http://example.com", "desc", List.of(1L));
        when(outboxService.findUnprocessed(anyInt())).thenReturn(List.of(msg));
        when(jsonSer.fromJson(payload, LinkUpdate.class)).thenReturn(linkUpdate);

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroup");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerProps.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.JsonDeserializer.class);
        consumerProps.put(org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES, "*");

        try (Consumer<Long, LinkUpdate> consumer =
                new KafkaConsumer<>(consumerProps, new LongDeserializer(), new JsonDeserializer<>(LinkUpdate.class))) {
            consumer.subscribe(List.of("test-topic"));

            // Act: опубликовать и затем получить сообщение
            producerService.publishUnprocessed();
            var records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(5));

            // Assert: одно сообщение с ожидаемым payload
            assertEquals(1, records.count());
            var record = records.iterator().next();
            assertEquals(linkUpdate.id(), record.value().id());
        }

        // проверка побочного эффекта: пометить сообщение как обработанное
        verify(outboxService, times(1)).markProcessed(messageId);
    }
}
