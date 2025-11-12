package org.twins.core.dto.rest.featurer.storager.filehandler;

import java.util.List;

import static org.twins.core.featurer.storager.filehandler.StoragerFileHandlerController.StorageType;

public record FileHandlerDeleteRqDTO(List<String> dirs, StorageType storageType) {}
