package engineering.everest.lhotse.photos.services;

import java.util.UUID;

public interface PhotosService {

    UUID registerUploadedPhoto(UUID requestingUserId, UUID backingFileId, String filename);
}
