package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_transition_alias")
public class TwinflowTransitionAliasEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "alias")
    private String alias;

    @Override
    public String easyLog(Level level) {
        return "twinflowTransition[domain id:" + domainId + ", alias:" + alias + "]";
    }
}
