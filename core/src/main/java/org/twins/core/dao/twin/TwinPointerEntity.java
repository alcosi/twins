package org.twins.core.dao.twin;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "twin_pointer")
public class TwinPointerEntity implements EasyLoggable {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "pointer_featurer_id")
    private Integer pointerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "pointer_params")
    private HashMap<String, String> pointerParams;

    @Column(name = "name")
    private String name;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinPointer[" + id + "]";
            default:
                return "twinPointer[id:" + id + ", twinClassId:" + twinClassId + "]";
        }
    }
}
