package org.twins.bootstrap;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nRepository;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.i18n.I18nTranslationRepository;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.permission.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.twinclass.OwnerType;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.cambium.common.util.LTreeUtils.convertToLTreeFormat;
import static org.twins.bootstrap.SystemEntityBootstrapData.*;

/**
 * Spring service that persists all system entities (schemes, system user, TwinClasses,
 * TwinStatuses, TwinClassFields, Links, DataLists, template Twins) at app startup via
 * {@link PostConstruct}.
 *
 * <p>All declarations live in {@link SystemEntityBootstrapData} — this class only contains
 * persistence logic. {@code postConstruct()} delegates to six private methods, one per
 * entity category, so each can be read and edited in isolation.
 *
 * <p>Gated by {@code twins.system.bootstrap.enabled} (default {@code true}). Set to
 * {@code false} in test profiles to skip the bootstrap (e.g. when the DB is pre-seeded
 * by fixtures or Flyway alone).
 */
@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "twins.system-entity.bootstrap.enabled", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SystemEntityBootstrapService {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinStatusRepository twinStatusRepository;
    final UserRepository userRepository;
    final TwinClassFieldRepository twinClassFieldRepository;
    final EntitySmartService entitySmartService;
    private final I18nRepository i18nRepository;
    private final I18nTranslationRepository i18nTranslationRepository;
    private final TwinClassSchemaRepository twinClassSchemaRepository;
    private final PermissionSchemaRepository permissionSchemaRepository;
    private final TwinflowSchemaRepository twinflowSchemaRepository;
    private final LinkRepository linkRepository;
    private final DataListRepository dataListRepository;
    private final DataListOptionRepository dataListOptionRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionRepository permissionRepository;

    @PostConstruct
    void postConstruct() throws ServiceException {
        bootstrapSchemes();
        bootstrapSystemUser();
        bootstrapSystemClasses();      // also seeds i18n for fields/statuses/link names
        bootstrapSystemLinks();
        bootstrapSystemDataLists();
        bootstrapSystemPermissions();  // also seeds i18n for permission name/description
        bootstrapTemplates();
    }

    private void bootstrapSchemes() throws ServiceException {
        PermissionSchemaEntity permissionSchema = new PermissionSchemaEntity()
                .setId(SystemIds.PermissionScheme.DEFAULT)
                .setName("System permission schema")
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.PermissionScheme.DEFAULT, permissionSchema, permissionSchemaRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinClassSchemaEntity twinClassSchemaEntity = new TwinClassSchemaEntity()
                .setId(SystemIds.TwinClassScheme.DEFAULT)
                .setName("System twinclass schema")
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.TwinClassScheme.DEFAULT, twinClassSchemaEntity, twinClassSchemaRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinflowSchemaEntity twinflowSchemaEntity = new TwinflowSchemaEntity()
                .setId(SystemIds.TwinflowScheme.DEFAULT)
                .setName("System twinflow schema")
                .setCreatedByUserId(SystemIds.User.SYSTEM)
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.TwinflowScheme.DEFAULT, twinflowSchemaEntity, twinflowSchemaRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
    }

    private void bootstrapSystemUser() throws ServiceException {
        UserEntity systemUser = new UserEntity()
                .setId(SystemIds.User.SYSTEM)
                .setName("SYSTEM")
                .setCreatedAt(Timestamp.from(Instant.now()));
        entitySmartService.save(SystemIds.User.SYSTEM, systemUser, userRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
    }

    private void bootstrapSystemClasses() throws ServiceException {
        List<I18nEntity> i18nEntities = new ArrayList<>();
        List<I18nTranslationEntity> i18nTranslationEntities = new ArrayList<>();
        List<TwinStatusEntity> statusEntities = new ArrayList<>();
        List<TwinClassFieldEntity> fieldEntities = new ArrayList<>();

        for (SystemClass systemClass : SYSTEM_CLASSES) {
            TwinClassEntity twinClassEntity = new TwinClassEntity()
                    .setId(systemClass.id())
                    .setKey(systemClass.key())
                    .setOwnerType(OwnerType.SYSTEM)
                    .setCreatedByUserId(SystemIds.User.SYSTEM)
                    .setAbstractt(systemClass.abstractt())
                    .setExtendsHierarchyTree(convertToLTreeFormat(systemClass.id()))
                    .setAssigneeRequired(systemClass.assigneeRequired())
                    .setSegment(false)
                    .setHasSegment(false)
                    .setUniqueName(false)
                    .setHasDynamicMarkers(false)
                    .setHeadHierarchyCounterDirectChildren(0)
                    .setExtendsHierarchyCounterDirectChildren(0)
                    .setTwinCounter(0)
                    .setCreatedAt(Timestamp.from(Instant.now()));
            entitySmartService.save(twinClassEntity.getId(), twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndLogOnException);

            for (SystemStatus status : systemClass.statuses()) {
                collectI18n(status.name(),        I18nType.TWIN_STATUS_NAME,        i18nEntities, i18nTranslationEntities);
                collectI18n(status.description(), I18nType.TWIN_STATUS_DESCRIPTION, i18nEntities, i18nTranslationEntities);
                statusEntities.add(new TwinStatusEntity()
                        .setId(status.id())
                        .setNameI18nId(status.name().i18nId())
                        .setDescriptionI18nId(status.description().i18nId())
                        .setTwinClassId(status.twinClassId())
                        .setInheritable(status.inheritable())
                        .setType(status.type()));
            }

            for (SystemField field : systemClass.fields()) {
                collectI18n(field.name(),        I18nType.TWIN_CLASS_FIELD_NAME,        i18nEntities, i18nTranslationEntities);
                collectI18n(field.description(), I18nType.TWIN_CLASS_FIELD_DESCRIPTION, i18nEntities, i18nTranslationEntities);
                fieldEntities.add(new TwinClassFieldEntity()
                        .setId(field.id())
                        .setTwinClassId(field.twinClassId())
                        .setKey(field.fieldKey())
                        .setNameI18nId(field.name() != null ? field.name().i18nId() : null)
                        .setDescriptionI18nId(field.description() != null ? field.description().i18nId() : null)
                        .setFieldTyperFeaturerId(field.fieldTyperId())
                        .setFieldInitializerFeaturerId(field.fieldInitializerFeaturerId())
                        .setTwinSorterFeaturerId(field.twinSorterFeaturerId())
                        .setRequired(field.required())
                        .setSystem(field.system())
                        .setInheritable(field.inheritable())
                        .setDependentField(false)
                        .setHasDependentFields(false)
                        .setProjectionField(false)
                        .setHasProjectedFields(false));
            }
        }

        // Link forward/backward i18n ride the same bulk save as class field/status i18n.
        for (SystemLink systemLink : SYSTEM_LINKS) {
            collectI18n(systemLink.forwardName(),  I18nType.LINK_FORWARD_NAME,  i18nEntities, i18nTranslationEntities);
            collectI18n(systemLink.backwardName(), I18nType.LINK_BACKWARD_NAME, i18nEntities, i18nTranslationEntities);
        }

        entitySmartService.saveAllAndLog(i18nEntities, i18nRepository);
        entitySmartService.saveAllAndLog(i18nTranslationEntities, i18nTranslationRepository);
        entitySmartService.saveAllAndLog(fieldEntities, twinClassFieldRepository);
        entitySmartService.saveAllAndLog(statusEntities, twinStatusRepository);
    }

    private void collectI18n(I18n i18n, I18nType type,
                             List<I18nEntity> i18nEntities,
                             List<I18nTranslationEntity> i18nTranslationEntities) {
        if (i18n == null) return;
        i18nEntities.add(new I18nEntity().setId(i18n.i18nId()).setType(type));
        i18nTranslationEntities.add(new I18nTranslationEntity()
                .setI18nId(i18n.i18nId())
                .setLocale(Locale.ENGLISH)
                .setTranslation(i18n.translation()));
    }

    private void bootstrapSystemLinks() {
        List<LinkEntity> linkEntities = new ArrayList<>();
        for (SystemLink systemLink : SYSTEM_LINKS) {
            linkEntities.add(new LinkEntity()
                    .setId(systemLink.id())
                    .setSrcTwinClassId(systemLink.srcTwinClassId())
                    .setDstTwinClassId(systemLink.dstTwinClassId())
                    .setForwardNameI18NId(systemLink.forwardName() != null ? systemLink.forwardName().i18nId() : null)
                    .setBackwardNameI18NId(systemLink.backwardName() != null ? systemLink.backwardName().i18nId() : null)
                    .setType(systemLink.type())
                    .setLinkStrengthId(systemLink.strength())
                    .setCreatedByUserId(SystemIds.User.SYSTEM));
        }
        entitySmartService.saveAllAndLog(linkEntities, linkRepository);
    }

    private void bootstrapSystemDataLists() {
        List<DataListEntity> dataListEntities = new ArrayList<>();
        List<DataListOptionEntity> dataListOptionEntities = new ArrayList<>();
        for (SystemDataList systemDataList : SYSTEM_DATA_LISTS) {
            dataListEntities.add(new DataListEntity()
                    .setId(systemDataList.id())
                    .setKey(systemDataList.key())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setUpdatedAt(Timestamp.from(Instant.now())));
            for (SystemDataListOption option : systemDataList.options()) {
                dataListOptionEntities.add(new DataListOptionEntity()
                        .setId(option.id())
                        .setDataListId(systemDataList.id())
                        .setOption(option.option())
                        .setStatus(option.status())
                        .setOrder(option.order())
                        .setCreatedAt(Timestamp.from(Instant.now())));
            }
        }
        entitySmartService.saveAllAndLog(dataListEntities, dataListRepository);
        entitySmartService.saveAllAndLog(dataListOptionEntities, dataListOptionRepository);
    }

    private void bootstrapSystemPermissions() throws ServiceException {
        // System permission group — all system permissions reference this as permission_group_id.
        PermissionGroupEntity permissionGroup = new PermissionGroupEntity()
                .setId(SystemIds.Permission.PERMISSION_GROUP_DEFAULT)
                .setKey("TWINS_GLOBAL_PERMISSIONS")
                .setName("Twins global permissions");
        entitySmartService.save(permissionGroup.getId(), permissionGroup, permissionGroupRepository, EntitySmartService.SaveMode.saveAndLogOnException);

        List<I18nEntity> i18nEntities = new ArrayList<>();
        List<I18nTranslationEntity> i18nTranslationEntities = new ArrayList<>();
        List<PermissionEntity> permissionEntities = new ArrayList<>();
        for (SystemPermission systemPermission : SYSTEM_PERMISSIONS) {
            collectI18n(systemPermission.name(),        I18nType.PERMISSION_NAME,        i18nEntities, i18nTranslationEntities);
            collectI18n(systemPermission.description(), I18nType.PERMISSION_DESCRIPTION, i18nEntities, i18nTranslationEntities);
            permissionEntities.add(new PermissionEntity()
                    .setId(systemPermission.id())
                    .setKey(systemPermission.key())
                    .setPermissionGroupId(SystemIds.Permission.PERMISSION_GROUP_DEFAULT)
                    .setNameI18NId(systemPermission.name() != null ? systemPermission.name().i18nId() : null)
                    .setDescriptionI18NId(systemPermission.description() != null ? systemPermission.description().i18nId() : null));
        }
        entitySmartService.saveAllAndLog(i18nEntities, i18nRepository);
        entitySmartService.saveAllAndLog(i18nTranslationEntities, i18nTranslationRepository);
        entitySmartService.saveAllAndLog(permissionEntities, permissionRepository);
    }

    private void bootstrapTemplates() throws ServiceException {
        TwinEntity userTemplate = new TwinEntity()
                .setId(SystemIds.TwinTemplate.USER)
                .setName("User")
                .setTwinClassId(SystemIds.TwinClass.USER)
                .setTwinStatusId(SystemIds.TwinStatus.User.INIT)
                .setCreatedByUserId(SystemIds.User.SYSTEM);
        entitySmartService.save(userTemplate.getId(), userTemplate, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);

        TwinEntity baTemplate = new TwinEntity()
                .setId(SystemIds.TwinTemplate.BUSINESS_ACCOUNT)
                .setName("Business account")
                .setTwinClassId(SystemIds.TwinClass.BUSINESS_ACCOUNT)
                .setTwinStatusId(SystemIds.TwinStatus.BusinessAccount.INIT)
                .setCreatedByUserId(SystemIds.User.SYSTEM);
        entitySmartService.save(baTemplate.getId(), baTemplate, twinRepository, EntitySmartService.SaveMode.ifNotPresentCreate);
    }
}
