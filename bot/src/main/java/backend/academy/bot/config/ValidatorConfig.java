package backend.academy.bot.config;

import backend.academy.bot.validator.LinkValidator;
import backend.academy.bot.validator.ValidatorPriority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Bean
    public List<LinkValidator> customLinkValidators(List<LinkValidator> list) {
        Map<ValidatorPriority, List<LinkValidator>> map = new EnumMap<>(ValidatorPriority.class);

        for (LinkValidator linkValidator : list) {
            ValidatorPriority priority = linkValidator.getPriority();
            map.computeIfAbsent(priority, key -> new ArrayList<>()).add(linkValidator);
        }

        List<LinkValidator> linkValidators = new ArrayList<>(map.values().size());
        for (ValidatorPriority priority : ValidatorPriority.values()) {
            List<LinkValidator> validators = map.get(priority);
            if (validators != null) {
                linkValidators.addAll(validators);
            }
        }

        return linkValidators;
    }

    @Bean
    public Integer maxCostPriority() {
        return Arrays.stream(ValidatorPriority.values())
                .max(Comparator.comparingInt(ValidatorPriority::cost))
                .map(ValidatorPriority::cost)
                .orElseThrow();
    }

    @Bean
    public Integer simpleCostPriority() {
        return ValidatorPriority.FIRST.cost();
    }
}
