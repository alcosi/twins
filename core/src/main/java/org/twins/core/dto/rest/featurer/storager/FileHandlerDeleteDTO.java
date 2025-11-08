package org.twins.core.dto.rest.featurer.storager;

import static org.twins.core.featurer.storager.filehandler.StoragerFileHandlerController.StorageType;

public record FileHandlerDeleteDTO(String[] dirs, StorageType storageType) {}
