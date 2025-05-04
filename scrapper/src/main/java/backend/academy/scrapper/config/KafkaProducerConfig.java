package backend.academy.scrapper.config;

import backend.academy.dto.LinkUpdate;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<Long, LinkUpdate> producerFactory(KafkaProperties props) {
        return new DefaultKafkaProducerFactory<>(props.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<Long, LinkUpdate> kafkaTemplate(ProducerFactory<Long, LinkUpdate> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public NewTopic updateTopic(ScrapperConfig cfg) {
        return TopicBuilder.name(cfg.kafka().name())
                .partitions(cfg.kafka().partitions())
                .replicas(cfg.kafka().replicas())
                .configs(Map.of("cleanup.policy", "delete"))
                .build();
    }
}
