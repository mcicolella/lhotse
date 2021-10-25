package engineering.everest.lhotse.users.services;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.axon.exceptions.RemoteCommandExecutionException;
import engineering.everest.lhotse.axon.common.domain.UserAttribute;
import engineering.everest.lhotse.axon.common.exceptions.KeycloakSynchronizationException;
import engineering.everest.lhotse.axon.common.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.users.domain.commands.CreateUserCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserRolesCommand;
import engineering.everest.lhotse.axon.common.domain.Role;

import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;

@Service
@Log4j2
public class DefaultUsersService implements UsersService {

    private final HazelcastCommandGateway commandGateway;
    private final KeycloakSynchronizationService keycloakSynchronizationService;

    public DefaultUsersService(HazelcastCommandGateway commandGateway, KeycloakSynchronizationService keycloakSynchronizationService) {
        this.commandGateway = commandGateway;
        this.keycloakSynchronizationService = keycloakSynchronizationService;
    }

    @Override
    public void updateUser(UUID requestingUserId, UUID userId, String emailChange, String displayNameChange) {
        commandGateway.sendAndWait(new UpdateUserDetailsCommand(userId, emailChange, displayNameChange, requestingUserId));
    }

    @Override
    public void updateUserRoles(UUID requestingUserId, UUID userId, Set<Role> roles) {
        commandGateway.sendAndWait(new UpdateUserRolesCommand(userId, roles, requestingUserId));
    }

    @Override
    public UUID createUser(UUID requestingUserId, UUID organizationId, String username, String displayName) {
        try {
            // Create user in the keycloak first so that we can get a newly created user id and map it in our app db.
            keycloakSynchronizationService.createUser(
                    Map.of("username", username,
                            "email", username,
                            "enabled", true,
                            "attributes", new UserAttribute(organizationId, Set.of(Role.ORG_USER), "Guest"),
                            "credentials", List.of(
                                    Map.of("type", "password",
                                            "value", "changeme",
                                            "temporary", true))));

            return commandGateway.sendAndWait(new CreateUserCommand(getUserId(username), organizationId,
                    requestingUserId, username, displayName));
        } catch (RemoteCommandExecutionException tex) {
            LOGGER.info("Keycloak createUser error: " + tex);
            return commandGateway.sendAndWait(new CreateUserCommand(getUserId(username), organizationId,
                    requestingUserId, username, displayName));
        } catch (Exception e) {
            throw (KeycloakSynchronizationException)new KeycloakSynchronizationException(e.getMessage()).initCause(e);
        }
    }

    @Override
    public void storeProfilePhoto(UUID requestingUserId, UUID profilePhotoFileId) {
        commandGateway.sendAndWait(new RegisterUploadedUserProfilePhotoCommand(requestingUserId, profilePhotoFileId));
    }

    @Override
    public void deleteAndForget(UUID requestingUserId, UUID userId, String requestReason) {
        commandGateway.sendAndWait(new DeleteAndForgetUserCommand(userId, requestingUserId, requestReason));
    }

    private UUID getUserId(String username) {
        return UUID.fromString(
                new JSONArray(keycloakSynchronizationService.getUsers(Map.of("username", username)))
                        .getJSONObject(0).getString("id"));
    }
}
