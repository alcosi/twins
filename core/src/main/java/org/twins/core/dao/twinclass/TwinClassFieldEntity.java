package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_field")
@FieldNameConstants
public class TwinClassFieldEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "field_typer_featurer_id")
    private Integer fieldTyperFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_typer_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldTyperParams;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "edit_permission_id")
    private UUID editPermissionId;

    @Column(name = "required")
    private Boolean required;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
//    private I18nEntity descriptionI18n;

    @FeaturerList(type = FieldTyper.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field_typer_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity fieldTyperFeaturer;

//    @ManyToOne
//    @JoinColumn(name = "view_permission_id", insertable = false, updatable = false)
//    private PermissionEntity viewPermission;
//
//    @ManyToOne
//    @JoinColumn(name = "edit_permission_id", insertable = false, updatable = false)
//    private PermissionEntity editPermission;

    public String easyLog(Level level) {
        return "twinClassField[id:" + id + ", key:" + key + "]";
    }
}
