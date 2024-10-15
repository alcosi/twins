package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.i18n.dao.LocaleConverter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.featurer.tokenhandler.TokenHandler;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "domain")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class DomainEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "description")
    private String description;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "business_account_template_twin_id")
    private UUID businessAccountTemplateTwinId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "default_i18n_locale_id")
    @Convert(converter = LocaleConverter.class)
    private Locale defaultI18nLocaleId;

    @Column(name = "ancestor_twin_class_id")
    private UUID ancestorTwinClassId;

    @Column(name = "business_account_initiator_featurer_id")
    private Integer businessAccountInitiatorFeaturerId;

    @FeaturerList(type = BusinessAccountInitiator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_account_initiator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity businessAccountInitiatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "business_account_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> businessAccountInitiatorParams;

    @Column(name = "token_handler_featurer_id")
    private Integer tokenHandlerFeaturerId;

    @FeaturerList(type = TokenHandler.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "token_handler_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity tokenHandlerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "token_handler_params", columnDefinition = "hstore")
    private HashMap<String, String> tokenHandlerParams;

    @Column(name = "user_group_manager_featurer_id")
    private Integer userGroupManagerFeaturerId;

    @FeaturerList(type = UserGroupManager.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_group_manager_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity userGroupManagerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "user_group_manager_params", columnDefinition = "hstore")
    private HashMap<String, String> userGroupManagerParams;

    @Column(name = "domain_type_id")
    @Convert(converter = DomainTypeConverter.class)
    private DomainType domainType;

    // needed for specification
    @Deprecated
    @OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<DomainBusinessAccountEntity> domainBusinessAccounts;

    @Transient
    @EqualsAndHashCode.Exclude
    private DomainTypeEntity domainTypeEntity;

    @Transient
    @EqualsAndHashCode.Exclude
    private PermissionSchemaEntity permissionSchema;

    public String easyLog(Level level) {
        return "domain[id:" + id + ", key:" + key + "]";
    }
}
