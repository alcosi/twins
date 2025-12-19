package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.StorageType;

public record FileHandlerSaveRqDTO(String id, String type, byte[] fileBytes, StorageType storageType, String storageDir) {}
