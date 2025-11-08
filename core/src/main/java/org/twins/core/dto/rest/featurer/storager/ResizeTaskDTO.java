package org.twins.core.dto.rest.featurer.storager;

public record ResizeTaskDTO(int width, int height, String type,
                            String format, String id, boolean keepAspectRatio) {}
