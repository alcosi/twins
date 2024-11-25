package org.twins.core.domain.attachment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class AttachmentQuotas {
    public Long quotaCount;
    public Long quotaSize;
    public Long usedCount;
    public Long usedSize;
}
