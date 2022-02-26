package com.logistics.supply.annotation.validator;

import com.logistics.supply.annotation.ValidPassword;
import org.passay.*;
import org.passay.dictionary.WordListDictionary;
import org.passay.dictionary.WordLists;
import org.passay.dictionary.sort.ArraysSort;
import org.springframework.core.io.ClassPathResource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private PasswordValidator validator;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        validator = new PasswordValidator(initWithDictionary());
    }

    private static List<Rule> initWithDictionary() {
        ArrayList<Rule> ruleArrayList = new ArrayList<>();
        // at least 8 characters
        ruleArrayList.add(new LengthRule(8, 1000));
        // at least one upper-case character
        ruleArrayList.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        // no whitespace
        ruleArrayList.add(new WhitespaceRule());

        DictionaryRule dictionaryRule = null;
        try {
            InputStream initialStream = new ClassPathResource("/invalid-password-list.txt").getInputStream();
            File targetFile = new File("invalid-password-pass.tmp");
            java.nio.file.Files.copy(initialStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            dictionaryRule = new DictionaryRule(
                    new WordListDictionary(WordLists.createFromReader(
                            // Reader around the word list file
                            new FileReader[]{
                                    //new FileReader(invalidPasswordList)
                                    new FileReader(targetFile)
                            },
                            // True for case sensitivity, false otherwise
                            false,
                            // Dictionaries must be sorted
                            new ArraysSort()
                    )));
        } catch (IOException e) {
            throw new RuntimeException("could not load word list", e);
        }

        ruleArrayList.add(dictionaryRule);
        return ruleArrayList;
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        RuleResult result = validator.validate(new PasswordData(password));
        if (result.isValid()) {
            return true;
        }

        List<String> messages = validator.getMessages(result);
        String messageTemplate = String.join(",", messages);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation()
                .disableDefaultConstraintViolation();
        return false;
    }

    public static boolean isValidPassword(String password) {
        PasswordValidator validator = new PasswordValidator(initWithDictionary());
        return validator.validate(new PasswordData(password)).isValid();
    }
}
