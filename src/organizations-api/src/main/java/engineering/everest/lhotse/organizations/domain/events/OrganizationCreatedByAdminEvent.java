package engineering.everest.lhotse.organizations.domain.events;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class OrganizationCreatedByAdminEvent implements ValidatableCommand {
    private UUID organizationId;
    private UUID registeringUserId;
    private String organizationName;
    private String websiteUrl;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String contactName;
    private String contactPhoneNumber;
    private String contactEmail;
}
