package org.twins.core.domain;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
public class PostponedTriggers extends ArrayList<PostponedTriggers.PostponedTrigger> {
    public void add(UUID twinId, UUID statusId, UUID triggerId) {
        log.info("Adding postponed trigger[{}] for twin[{}]", triggerId, twinId);
        add(new PostponedTrigger(twinId, statusId, triggerId));
    }

    public record PostponedTrigger(UUID twinId, UUID statusId, UUID triggerId){
    }
}
