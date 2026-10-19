package org.twins.core.featurer.storager;

import org.twins.core.dto.rest.featurer.storager.filehandler.AttachmentModification;

import java.util.List;

public record AddedFileKey(String fileKey, long fileSize, List<AttachmentModification> modifications) {
}
