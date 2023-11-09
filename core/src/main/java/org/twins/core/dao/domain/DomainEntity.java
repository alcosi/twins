package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.businessaccount.initiator.BusinessAccountInitiator;
import org.twins.core.featurer.tokenhandler.TokenHandler;
import org.twins.core.featurer.usergroup.manager.UserGroupManager;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Entity
@Table(name = "domain")
@DynamicUpdate
@Data
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

    @FeaturerList(type = BusinessAccountInitiator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "business_account_initiator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity businessAccountInitiatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "business_account_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> businessAccountInitiatorParams;

    @FeaturerList(type = TokenHandler.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "token_handler_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity tokenHandlerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "token_handler_params", columnDefinition = "hstore")
    private HashMap<String, String> tokenHandlerParams;

    @FeaturerList(type = UserGroupManager.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_group_manager_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity userGroupManagerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "user_group_manager_params", columnDefinition = "hstore")
    private HashMap<String, String> userGroupManagerParams;

    public String easyLog(Level level) {
        return "domain[id:" + id + ", key:" + key + "]";
    }
}
