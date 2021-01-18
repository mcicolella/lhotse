package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.TranslatingValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class EmailAddressValidator implements Validates<EmailAddressValidatableCommand> {

    @Override
    public void validate(EmailAddressValidatableCommand validatable) {
        if (validatable.getEmailAddress() == null) {
            return;
        }
        boolean emailValid = EmailValidator.getInstance(false).isValid(validatable.getEmailAddress());
        TranslatingValidator.isTrue(emailValid, "EMAIL_ADDRESS_MALFORMED");
    }
}