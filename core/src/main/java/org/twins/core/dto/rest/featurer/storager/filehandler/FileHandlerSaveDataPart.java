package org.twins.core.dto.rest.featurer.storager.filehandler;

import org.twins.core.enums.featurer.storager.StorageType;

public record FileHandlerSaveDataPart(String id, String fileName, String type, StorageType storageType, String storageDir) {}
