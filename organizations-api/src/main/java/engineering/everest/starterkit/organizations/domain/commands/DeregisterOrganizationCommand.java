package engineering.everest.starterkit.organizations.domain.commands;

import engineering.everest.starterkit.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeregisterOrganizationCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private UUID organizationId;

    @NotNull
    private UUID requestingUserId;
}
