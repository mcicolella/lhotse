package engineering.everest.starterkit.axon.command.validators;

import engineering.everest.starterkit.axon.command.validation.OrganizationStatusValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.Validates;
import engineering.everest.starterkit.organizations.Organization;
import engineering.everest.starterkit.organizations.services.OrganizationsReadService;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
public class OrganizationStatusValidator implements Validates<OrganizationStatusValidatableCommand> {

    private final OrganizationsReadService organizationsReadService;

    public OrganizationStatusValidator(OrganizationsReadService organizationsReadService) {
        this.organizationsReadService = organizationsReadService;
    }

    @Override
    public void validate(OrganizationStatusValidatableCommand command) {
        Organization organization;
        try {
            organization = organizationsReadService.getById(command.getOrganizationId());
        } catch (NoSuchElementException e) {
            throw new IllegalStateException(String.format("Organization %s does not exist", command.getOrganizationId()), e);
        }
        Validate.validState(!organization.isDeregistered(), "Organization %s is de-registered", command.getOrganizationId());
    }
}
