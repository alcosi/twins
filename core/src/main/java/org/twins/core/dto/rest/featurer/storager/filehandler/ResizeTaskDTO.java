package org.twins.core.dto.rest.featurer.storager.filehandler;

public record ResizeTaskDTO(int width, int height, String type,
                            String format, String id, boolean keepAspectRatio) {}
