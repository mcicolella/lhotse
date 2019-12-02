package engineering.everest.starterkit.axon.users.services;

import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.axon.filehandling.FileService;
import engineering.everest.starterkit.axon.media.thumbnails.ThumbnailService;
import engineering.everest.starterkit.axon.users.persistence.PersistableUser;
import engineering.everest.starterkit.axon.users.persistence.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Component
public class DefaultUsersReadService implements UsersReadService {

    private final UsersRepository usersRepository;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;

    @Autowired
    public DefaultUsersReadService(UsersRepository usersRepository,
                                   FileService fileService,
                                   ThumbnailService thumbnailService) {
        this.usersRepository = usersRepository;
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public User getById(UUID id) {
        return convert(usersRepository.findById(id).orElseThrow());
    }

    @Override
    public List<User> getUsers() {
        return usersRepository.findAll().stream()
                .map(this::convert)
                .collect(toList());
    }

    @Override
    public List<User> getUsersForOrganization(UUID organizationId) {
        return usersRepository.findByOrganizationId(organizationId).stream()
                .map(this::convert)
                .collect(toList());
    }

    @Override
    public boolean exists(UUID userId) {
        return usersRepository.existsById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return convert(usersRepository.findByEmailIgnoreCase(username).orElseThrow());
    }

    @Override
    public boolean hasUserWithEmail(String email) {
        return usersRepository.findByEmailIgnoreCase(email).isPresent();
    }

    @Override
    public List<User> getAdmins() {
        return usersRepository.findAdmins().stream()
                .map(this::convert)
                .collect(toList());
    }

    @Override
    public List<User> getAdminsForOrganization(UUID organizationId) {
        return usersRepository.findOrganizationAdmins(organizationId).stream()
                .map(this::convert)
                .collect(toList());
    }

    @Override
    public InputStream getProfilePhotoThumbnailStream(UUID userId, int width, int height) throws IOException {
        PersistableUser persistableUser = usersRepository.findById(userId).orElseThrow();
        UUID profilePhotoFileId = persistableUser.getProfilePhotoFileId();
        if (profilePhotoFileId == null) {
            throw new RuntimeException("Profile photo not present");
        }
        return thumbnailService.streamThumbnailForOriginalFile(profilePhotoFileId, width, height);
    }

    @Override
    public InputStream getProfilePhotoStream(UUID id) throws IOException {
        PersistableUser persistableUser = usersRepository.findById(id).orElseThrow();
        UUID profilePhotoFileId = persistableUser.getProfilePhotoFileId();
        if (profilePhotoFileId == null) {
            throw new RuntimeException("Profile photo not present");
        }
        return fileService.stream(profilePhotoFileId);
    }

    private User convert(PersistableUser persistableUser) {
        return new User(persistableUser.getId(), persistableUser.getOrganizationId(), persistableUser.getUsername(),
                persistableUser.getDisplayName(), persistableUser.getEmail(),
                persistableUser.isDisabled(), persistableUser.getRoles());
    }
}
