package backend.academy.bot.config;

import backend.academy.bot.entity.State;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyboardConfig {

    @Bean
    public List<String> buttonNoSessionList() {
        return Collections.singletonList(State.START.command());
    }

    @Bean
    public List<String> buttonSessionList() {
        return Arrays.stream(State.values())
                .filter(State::isCommandToDisplay)
                .map(State::command)
                .toList();
    }

    @Bean
    public List<String> buttonInProcessList() {
        return Collections.singletonList(State.END.command());
    }
}
