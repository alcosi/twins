package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.i18n.dao.I18nEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_field")
public class TwinClassFieldEntity {
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
    private int fieldTyperFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_typer_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldTyperParams;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "edit_permission_id")
    private UUID editPermissionId;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @ManyToOne
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @FeaturerList(type = FieldTyper.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field_typer_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity featurerByFieldTyperFeaturerId;

    @ManyToOne
    @JoinColumn(name = "view_permission_id", insertable = false, updatable = false)
    private PermissionEntity permissionByViewPermissionId;

    @ManyToOne
    @JoinColumn(name = "edit_permission_id", insertable = false, updatable = false)
    private PermissionEntity permissionByEditPermissionId;
}
