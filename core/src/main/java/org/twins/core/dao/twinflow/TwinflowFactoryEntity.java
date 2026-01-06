package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.enums.factory.FactoryLauncher;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow_factory")
@FieldNameConstants
public class TwinflowFactoryEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_id")
    private UUID twinflowId;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_factory_launcher_id")
    private FactoryLauncher twinFactoryLauncher;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_id", insertable = false, updatable = false, nullable = false)
    private TwinflowEntity twinflow;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactory;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinflowFactory[" + id + "]";
            default -> "twinflowFactory[id:" + id + ", twinflowId:" + twinflowId + ", twinFactoryId:" + twinFactoryId + ", twinFactoryLauncher:" + twinFactoryLauncher + "]";
        };
    }
}
