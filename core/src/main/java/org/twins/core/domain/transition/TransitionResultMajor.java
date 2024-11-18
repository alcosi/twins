package org.twins.core.domain.transition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.draft.DraftEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TransitionResultMajor extends TransitionResult {
    private DraftEntity commitedDraftEntity;
}
