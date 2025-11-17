package org.twins.core.dto.rest.featurer.storager.filehandler;

import java.util.UUID;

public record FileHandlerResizeSaveRsDTO(UUID id, String objectLink, String type,
                                         String dir, FileHandlerErrorDTO error) {}
