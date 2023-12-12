package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.EasyLoggableImpl;
import org.twins.core.dao.twin.TwinEntity;

import java.util.UUID;

@Entity
@Table(name = "twin_factory_pipeline")
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class TwinFactoryPipelineEntity extends EasyLoggableImpl {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;
    
    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;
    
    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;
    
    @Column(name = "draft")
    private boolean draft;
    
    @Column(name = "next_twin_factory_id")
    private UUID nextTwinFactoryId;

    @Column(name = "template_twin_id")
    private UUID templateTwinId;

    @ManyToOne
    @JoinColumn(name = "template_twin_id", insertable = false, updatable = false, nullable = true)
    private TwinEntity templateTwin;

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinFactoryPipeline[id:" + id + "]";
            default:
                return "twinFactoryPipeline[id:" + id + ", twinFactoryId:" + twinFactoryId + ", inputTwinClassId:" + inputTwinClassId + "]";
        }

    }
}
