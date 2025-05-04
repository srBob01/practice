package backend.academy.scrapper.parser.postfix.impl;

import backend.academy.scrapper.model.db.link.LinkType;
import backend.academy.scrapper.model.db.link.impl.StackOverflowLink;
import backend.academy.scrapper.parser.postfix.abs.AbstractStackOverflowSubPostfixParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class StackOverflowQuestionPostfixParser extends AbstractStackOverflowSubPostfixParser {

    // Ожидается постфикс вида "questions/12345" с возможным дополнительным суффиксом
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^questions/(\\d+)(/.*)?$");

    @Override
    protected boolean supportsPostfix(String postfix) {
        return QUESTION_PATTERN.matcher(postfix).matches();
    }

    @Override
    protected StackOverflowLink parsePostfix(String postfix) {
        Matcher matcher = QUESTION_PATTERN.matcher(postfix);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid StackOverflow question postfix: " + postfix);
        }
        String questionId = matcher.group(1);
        String fullUrl = LinkType.STACKOVERFLOW.prefix() + postfix;
        return new StackOverflowLink(fullUrl, questionId);
    }
}
