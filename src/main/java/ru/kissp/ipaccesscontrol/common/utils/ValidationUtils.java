package ru.kissp.ipaccesscontrol.common.utils;

import org.springframework.validation.Validator;
import org.springframework.web.server.ServerWebInputException;

public class ValidationUtils {
    public static void validate(Validator validator, Object object) {
        var errors = validator.validateObject(object);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.getAllErrors().toString());
        }
    }
}
