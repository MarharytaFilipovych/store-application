package margo.grid.Store.app.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import margo.grid.Store.app.annotation.ValidPassword;
import margo.grid.Store.app.config.PasswordSettings;
import org.passay.*;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordListDictionary;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyPasswordValidator implements ConstraintValidator<ValidPassword, String> {
    @Autowired
    private PasswordSettings properties;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if(password == null || password.isBlank())return false;
        List<Rule> rules = buildPasswordRules();
        PasswordValidator validator = new PasswordValidator(rules);
        RuleResult result = validator.validate(new PasswordData(password));
        if(result.isValid())return true;
        context.disableDefaultConstraintViolation();
        validator.getMessages(result).forEach(m ->
                context.buildConstraintViolationWithTemplate(m)
                        .addConstraintViolation());
        return false;
    }

    private List<Rule> buildPasswordRules(){
        List<Rule> rules = new ArrayList<>();
        rules.add(new LengthRule(properties.getMinLength(), properties.getMaxLength()));
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, properties.getMinUppercase()));
        rules.add(new CharacterRule(EnglishCharacterData.LowerCase, properties.getMinLowercase()));
        rules.add(new CharacterRule(EnglishCharacterData.Digit, properties.getMinDigits()));
        rules.add(new CharacterRule(EnglishCharacterData.Special, properties.getMinSpecial()));
        int maxSequence = properties.getMaxSequenceLength();
        rules.add(new IllegalSequenceRule(EnglishSequenceData.Alphabetical, maxSequence, false));
        rules.add(new IllegalSequenceRule(EnglishSequenceData.Numerical, maxSequence, false));
        rules.add(new IllegalSequenceRule(EnglishSequenceData.USQwerty, maxSequence, false));
        rules.add(new RepeatCharacterRegexRule(properties.getMaxRepeatChars() + 1));
        if (properties.getCommonPasswords().length != 0) {
            String[] commonPasswords = properties.getCommonPasswords().clone();
            Arrays.sort(commonPasswords);
            rules.add(new DictionaryRule(new WordListDictionary(new ArrayWordList(commonPasswords))));
        }
        return rules;
    }
}