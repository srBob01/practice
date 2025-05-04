package backend.academy.bot.config;

import backend.academy.bot.entity.State;
import backend.academy.bot.state.StateHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateConfig {
    @Bean
    Map<String, State> commandDefinedToStateMap() {
        return Arrays.stream(State.values())
                .filter(State::isCommandDefined)
                .filter(s -> s.command() != null)
                .collect(Collectors.toMap(State::command, Function.identity()));
    }

    @Bean
    Map<State, StateHandler> commandDefinedToStateHandlerMap(List<StateHandler> stateHandlers) {
        return stateHandlers.stream().collect(Collectors.toMap(StateHandler::getProcessedState, Function.identity()));
    }
}
