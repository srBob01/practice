package backend.academy.bot.config;

import backend.academy.dto.LinkUpdate;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<Long, LinkUpdate> consumerFactory(KafkaProperties props) {
        return new DefaultKafkaConsumerFactory<>(props.buildConsumerProperties());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate> kafkaListenerContainerFactory(
            ConsumerFactory<Long, LinkUpdate> cf, BotConfig botConfig) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Long, LinkUpdate>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(botConfig.kafka().partitions());

        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setAckMode(ContainerProperties.AckMode.MANUAL);
        containerProps.setMissingTopicsFatal(false);

        return factory;
    }

    @Bean
    public RetryTopicConfiguration retryTopicConfiguration(
            KafkaTemplate<Long, LinkUpdate> kafkaTemplate, BotConfig botConfig) {
        return RetryTopicConfigurationBuilder.newInstance()
                .maxAttempts(botConfig.kafka().retryMaxAttempts())
                .fixedBackOff(botConfig.kafka().retryBackoffMs())
                .retryTopicSuffix(".retry")
                .dltSuffix(".dlq")
                .create(kafkaTemplate);
    }
}
