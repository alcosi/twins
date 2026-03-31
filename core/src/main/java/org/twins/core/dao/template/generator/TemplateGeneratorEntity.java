package org.twins.core.dao.template.generator;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "template_generator")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class TemplateGeneratorEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "templater_featurer_id")
    private Integer templaterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "templater_params", columnDefinition = "hstore")
    private HashMap<String, String> templaterParams;

    @Column(name = "active")
    private boolean active;

    public String easyLog(Level level) {
        return "templateGenerator[id:" + id + "]";
    }

}
