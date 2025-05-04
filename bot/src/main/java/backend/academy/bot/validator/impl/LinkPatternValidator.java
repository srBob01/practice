package backend.academy.bot.validator.impl;

import backend.academy.bot.validator.LinkValidator;
import backend.academy.bot.validator.ValidatorPriority;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LinkPatternValidator implements LinkValidator {

    private static final Pattern GITHUB_PATTERN = Pattern.compile("https://github.com/.*");
    private static final Pattern STACKOVERFLOW_PATTERN = Pattern.compile("https://stackoverflow.com/.*");

    @Override
    public boolean isValidLink(String url) {
        return url != null && (isGithubLink(url) || isStackOverflowLink(url));
    }

    @Override
    public ValidatorPriority getPriority() {
        return ValidatorPriority.FIRST;
    }

    private boolean isGithubLink(String url) {
        return GITHUB_PATTERN.matcher(url).matches();
    }

    private boolean isStackOverflowLink(String url) {
        return STACKOVERFLOW_PATTERN.matcher(url).matches();
    }
}
