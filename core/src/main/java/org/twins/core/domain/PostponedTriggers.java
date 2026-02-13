package org.twins.core.domain;

import java.util.HashSet;
import java.util.UUID;

public class PostponedTriggers extends HashSet<PostponedTriggers.PostponedTrigger> {
    public void add(UUID twinId, UUID statusId, UUID triggerId) {
        add(new PostponedTrigger(twinId, statusId, triggerId));
    }

    public record PostponedTrigger(UUID twinId, UUID statusId, UUID triggerId){
    }
}
