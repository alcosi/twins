package org.twins.core.dto.rest.featurer.storager.filehandler;

import java.util.List;

public record ResizeRsDTO(String originalUrl, List<ModificationDTO> modifications) {}
