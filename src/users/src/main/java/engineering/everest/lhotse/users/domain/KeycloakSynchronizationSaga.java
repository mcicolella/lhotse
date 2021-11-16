package engineering.everest.lhotse.users.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.axon.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.axon.common.domain.UserAttribute;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesUpdatedByAdminEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;

import java.util.Map;
import java.util.concurrent.Callable;

@Saga
@Revision("0")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class KeycloakSynchronizationSaga {
    private static final String USER_ID_PROPERTY = "userId";
    private static final String DELETED_USER_ID_PROPERTY = "deletedUserId";

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = USER_ID_PROPERTY)
    public void on(UserRolesUpdatedByAdminEvent event,
                   UsersReadService usersReadService,
                   KeycloakSynchronizationService keycloakSynchronizationService) throws Exception {
        var user = usersReadService.getById(event.getUserId());

        waitForTheProjectionUpdate(() -> user.getRoles().equals(event.getRoles()),
                "user roles projection update");

        keycloakSynchronizationService.updateUserAttributes(event.getUserId(),
                Map.of("attributes", new UserAttribute(user.getOrganizationId(), event.getRoles(), user.getDisplayName())));
    }

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = USER_ID_PROPERTY)
    public void on(UserDetailsUpdatedByAdminEvent event,
                   UsersReadService usersReadService,
                   KeycloakSynchronizationService keycloakSynchronizationService) throws Exception {
        var user = usersReadService.getById(event.getUserId());

        waitForTheProjectionUpdate(() -> user.getEmail()
                        .equals(event.getEmailChange()) || user.getDisplayName().equals(event.getDisplayNameChange()),
                "user email or displayName projection update");

        keycloakSynchronizationService.updateUserAttributes(event.getUserId(),
                Map.of("attributes", new UserAttribute(user.getOrganizationId(), user.getRoles(), event.getDisplayNameChange()),
                        "email", event.getEmailChange()));
    }

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = DELETED_USER_ID_PROPERTY)
    public void on(UserDeletedAndForgottenEvent event,
                   UsersReadService usersReadService,
                   KeycloakSynchronizationService keycloakSynchronizationService) throws Exception {
        waitForTheProjectionUpdate(() -> !usersReadService.exists(event.getDeletedUserId()),
                "user deletion projection update");

        keycloakSynchronizationService.deleteUser(event.getDeletedUserId());
    }

    // Failure here will result in the saga not completing.
    // Rollback has not been implemented in this example.
    private void waitForTheProjectionUpdate(Callable<Boolean> condition, String message) throws Exception {
        RetryWithExponentialBackoff.oneMinuteWaiter().waitOrThrow(condition, message);
    }
}
