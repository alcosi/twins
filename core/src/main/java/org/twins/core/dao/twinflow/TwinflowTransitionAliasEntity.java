package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
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
        switch (level) {
            case SHORT:
                return "twinflowTransitionAlias[" + id + "]";
            default:
                return "twinflowTransitionAlias[domain id:" + domainId + ", alias:" + alias + "]";
        }

    }
}
