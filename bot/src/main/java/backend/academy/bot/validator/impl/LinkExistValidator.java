package backend.academy.bot.validator.impl;

import backend.academy.bot.validator.LinkValidator;
import backend.academy.bot.validator.ValidatorPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LinkExistValidator implements LinkValidator {

    private final RestClient restClient;

    @Override
    public boolean isValidLink(String url) {
        try {
            var response = restClient.get().uri(url).retrieve().toBodilessEntity();
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ValidatorPriority getPriority() {
        return ValidatorPriority.SECOND;
    }
}
