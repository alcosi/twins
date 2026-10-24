package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.domain.DomainVersionStatus;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainVersionV1")
public class DomainVersionDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "version label: 'draft' for the open window, final label for a release, '(import) 1.2' for an import", example = "1.2")
    public String version;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "status: OPEN — mutable draft window, RELEASED — immutable snapshot", example = "RELEASED")
    public DomainVersionStatus status;

    @Schema(description = "monotonic per-domain counter assigned at release/import; null for the open window", example = "3")
    public Long order;

    @Schema(description = "SHA-256 of the full snapshot (always set for RELEASED)")
    public String hash;

    @Schema(description = "SHA-256 of the forward delta (prev release -> this release)")
    public String deltaHash;

    @Schema(description = "SHA-256 of the reverse delta (this release -> prev release)")
    public String rollbackDeltaHash;

    @Schema(description = "previous version in the release chain", example = DTOExamples.UUID_ID)
    public UUID previousDomainVersionId;

    @Schema(description = "window start", example = DTOExamples.INSTANT)
    public Timestamp startedAt;

    @Schema(description = "window start duration (ms)", example = "120")
    public Long startDuration;

    @Schema(description = "release timestamp; null while the window is open", example = DTOExamples.INSTANT)
    public Timestamp releasedAt;

    @Schema(description = "release duration (ms)", example = "3500")
    public Long releaseDuration;

    @Schema(description = "full snapshot of the configuration state (DETAILED mode only)")
    public Map<String, Object> jsonFile;

    @Schema(description = "forward delta: prev release -> this release, for cross-env import (DETAILED mode only)")
    public Map<String, Object> deltaFile;

    @Schema(description = "reverse delta: this release -> prev release, for local restore (DETAILED mode only)")
    public Map<String, Object> rollbackDeltaFile;
}
