package org.twins.core.dto.rest.featurer.storager.filehandler;

import java.util.UUID;

public record AttachmentModification(UUID twinAttachmentId, String modificationType, String storageFileKey) {}
