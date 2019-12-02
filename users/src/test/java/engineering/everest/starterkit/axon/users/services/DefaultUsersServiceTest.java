package engineering.everest.starterkit.axon.users.services;

import engineering.everest.starterkit.axon.HazelcastCommandGateway;
import engineering.everest.starterkit.axon.common.PasswordEncoder;
import engineering.everest.starterkit.axon.common.RandomFieldsGenerator;
import engineering.everest.starterkit.axon.users.domain.commands.CreateUserCommand;
import engineering.everest.starterkit.axon.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.starterkit.axon.users.domain.commands.UpdateUserDetailsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUsersServiceTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final UUID PROFILE_PHOTO_FILE_ID = randomUUID();
    private static final String NEW_USER_EMAIL = "new-user-email";
    private static final String NEW_USER_DISPLAY_NAME = "new-user-display-name";

    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;
    @Mock
    private HazelcastCommandGateway commandGateway;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DefaultUsersService defaultUsersService;

    @BeforeEach
    void setUp() {
        defaultUsersService = new DefaultUsersService(commandGateway, randomFieldsGenerator, passwordEncoder);
    }

    @Test
    void updateUserDetails_WillSendCommandAndWaitForCompletion() {
        when(passwordEncoder.encode("password-change")).thenReturn("encoded-password-change");

        defaultUsersService.updateUser(ADMIN_ID, USER_ID, "email-change",
                "display-name-change", "password-change");

        verify(commandGateway).sendAndWait(new UpdateUserDetailsCommand(USER_ID, "email-change",
                "display-name-change", "encoded-password-change", ADMIN_ID));
    }

    @Test
    void createNewUser_WillSendCommandAndWaitForCompletion() {
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");

        when(randomFieldsGenerator.genRandomUUID()).thenReturn(USER_ID);

        defaultUsersService.createUser(ADMIN_ID, ORGANIZATION_ID, NEW_USER_EMAIL, NEW_USER_DISPLAY_NAME, "raw-password");

        verify(commandGateway).sendAndWait(new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, NEW_USER_EMAIL, "encoded-password", NEW_USER_DISPLAY_NAME));
    }

    @Test
    void storeProfilePhoto_WillSendCommandAndWaitForCompletion() {
        defaultUsersService.storeProfilePhoto(USER_ID, PROFILE_PHOTO_FILE_ID);
        verify(commandGateway).sendAndWait(new RegisterUploadedUserProfilePhotoCommand(USER_ID, PROFILE_PHOTO_FILE_ID));
    }
}
