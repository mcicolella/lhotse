package engineering.everest.starterkit.axon.users.domain.commands;

import engineering.everest.starterkit.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.UserUniqueEmailValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDetailsCommand implements EmailAddressValidatableCommand, UserUniqueEmailValidatableCommand {

    @TargetAggregateIdentifier
    private UUID userId;
    private String emailChange;
    private String displayNameChange;
    private String passwordChange;

    @NotNull
    private UUID requestingUserId;

    @Override
    public String getEmailAddress() {
        return emailChange;
    }

}
