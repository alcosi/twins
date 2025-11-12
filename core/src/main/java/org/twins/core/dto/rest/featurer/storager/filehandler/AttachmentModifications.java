package org.twins.core.dto.rest.featurer.storager.filehandler;

import java.util.UUID;

public record AttachmentModifications(UUID twinAttachmentId, String modificationType, String storageFileKey) {
}
