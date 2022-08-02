package engineering.everest.lhotse.competitions.domain.events;

import engineering.everest.axon.cryptoshredding.annotations.EncryptedField;
import engineering.everest.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class PhotoEnteredIntoCompetitionEvent {
    private UUID competitionId;
    private UUID photoId;
    @EncryptionKeyIdentifier
    private UUID submittedByUserId;
    private UUID photoOwnerUserId;
    @EncryptedField
    private String submissionNotes;
}
