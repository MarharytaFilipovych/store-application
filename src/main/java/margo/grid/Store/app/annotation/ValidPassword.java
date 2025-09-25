package margo.grid.Store.app.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import margo.grid.Store.app.utils.MyPasswordValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MyPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Your password does not meet our security requirements :)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}