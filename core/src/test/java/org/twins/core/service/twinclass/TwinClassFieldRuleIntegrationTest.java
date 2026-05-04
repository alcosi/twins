package org.twins.core.service.twinclass;

import jakarta.persistence.EntityManager;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.idp.IdentityProviderRepository;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.domain.DomainStatus;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.twin.*;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserService;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.jpa.show-sql=true",
        "api.unsecured.enable=false",
        "api.key.header=X-Twins-Api-Key",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class TwinClassFieldRuleIntegrationTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("init_db.sql");

    @Autowired
    private TwinClassFieldRuleMapService ruleMapService;

    @Autowired
    private TwinClassFieldService fieldService;

    @MockBean
    private org.twins.core.service.MapperModesResolveService mapperModesResolveService;

    @MockBean
    private TwinClassService twinClassService;

    @MockBean
    private TwinService twinService;

    @Autowired
    private PermissionSchemaRepository permissionSchemaRepository;

    @MockBean
    private BusinessAccountRepository businessAccountRepository;

    @MockBean
    private SystemEntityService systemEntityService;

    @MockBean
    private AuthService authService;

    @MockBean
    private FeaturerService featurerService;

    @MockBean
    private TwinSearchService twinSearchService;

    @MockBean
    private TwinflowService twinflowService;

    @MockBean
    private DomainService domainService;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private TwinStatusService twinStatusService;

    @MockBean
    private UserService userService;

    @MockBean
    private I18nService i18nService;

    @MockBean
    private TwinClassFreezeService twinClassFreezeService;

    @MockBean
    private TwinMarkerService twinMarkerService;

    @MockBean
    private TwinTagService twinTagService;

    @MockBean
    private TwinChangesService twinChangesService;

    @MockBean
    private TwinLinkService twinLinkService;

    @MockBean
    private HistoryService historyService;

    @MockBean
    private DataListService dataListService;

    @MockBean
    private TwinAliasService twinAliasService;

    @MockBean
    private TwinFieldAttributeService twinFieldAttributeService;

    @MockBean
    private TwinStatusTriggerService twinStatusTriggerService;

    @Autowired
    private TwinClassFieldRuleRepository ruleRepository;

    @Autowired
    private TwinClassFieldRepository fieldRepository;

    @Autowired
    private TwinClassFieldRuleMapRepository ruleMapRepository;

    @Autowired
    private TwinClassRepository twinClassRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private IdentityProviderRepository identityProviderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwinflowSchemaRepository twinflowSchemaRepository;

    @Autowired
    private TwinClassSchemaRepository twinClassSchemaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TaskScheduler taskScheduler; // TaskScheduler can stay real or be mocked. If it causes issues, I'll mock it.

    private UUID domainId;
    private TwinClassEntity twinClass;
    private TwinClassFieldEntity field;
    private TwinClassFieldRuleEntity rule;

    // Helper to stub ApiUser
    private void stubApiUser(UUID domainId, UserEntity user) throws ServiceException {
        ApiUser apiUser = Mockito.mock(ApiUser.class);
        Mockito.when(apiUser.getDomain()).thenReturn(new DomainEntity().setId(domainId));
        Mockito.when(apiUser.getUser()).thenReturn(user);
        Mockito.when(apiUser.getDomainId()).thenReturn(domainId);
        Mockito.when(authService.getApiUser()).thenReturn(apiUser);
    }

    @BeforeEach
    public void setupData() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity()
                .setId(userId)
                .setName("Test User")
                .setEmail("test_" + userId + "@example.com")
                .setUserStatusId(UserStatus.ACTIVE);
        userRepository.save(user);

        UUID idpId = UUID.randomUUID();
        IdentityProviderEntity idp = new IdentityProviderEntity()
                .setId(idpId)
                .setName("Test IDP")
                .setStatus(IdentityProviderEntity.IdentityProviderStatus.ACTIVE)
                .setTrustorFeaturerId(3501);
        identityProviderRepository.save(idp);

        // Standard system schemas
        UUID permSchemaId = UUID.fromString("00000000-0000-0000-0012-000000000001");
        permissionSchemaRepository.save(new PermissionSchemaEntity().setId(permSchemaId).setCreatedByUserId(userId).setName("System Perm"));

        UUID flowSchemaId = UUID.fromString("00000000-0000-0000-0013-000000000001");
        twinflowSchemaRepository.save(new TwinflowSchemaEntity().setId(flowSchemaId).setCreatedByUserId(userId).setName("System Flow"));

        UUID classSchemaId = UUID.fromString("00000000-0000-0000-0014-000000000001");
        twinClassSchemaRepository.save(new TwinClassSchemaEntity().setId(classSchemaId).setCreatedByUserId(userId).setName("System Class Schema"));

        domainId = UUID.randomUUID();

        // Step 1: Create twin class with domainId = null to break circularity
        twinClass = new TwinClassEntity()
                .setId(UUID.randomUUID())
                .setDomainId(null) // Break circularity
                .setKey("TEST_CLASS")
                .setOwnerType(OwnerType.SYSTEM)
                .setCreatedByUserId(userId)
                .setAssigneeRequired(false)
                .setSegment(false)
                .setAbstractt(false)
                .setHasSegment(false)
                .setHasDynamicMarkers(false)
                .setUniqueName(false)
                .setHeadHierarchyCounterDirectChildren(0)
                .setExtendsHierarchyCounterDirectChildren(0)
                .setTwinCounter(0)
                .setPermissionSchemaSpace(false)
                .setTwinflowSchemaSpace(false)
                .setTwinClassSchemaSpace(false)
                .setAliasSpace(false);
        twinClassRepository.save(twinClass);
        entityManager.flush(); // Ensure it's in DB

        // Step 2: Create domain using the twin class ID
        DomainEntity domain = new DomainEntity()
                .setId(domainId)
                .setKey("TEST_DOMAIN_" + domainId)
                .setName("Test Domain")
                .setDomainType(DomainType.basic)
                .setDomainStatusId(DomainStatus.ACTIVE)
                .setIdentityProviderId(idpId)
                .setPermissionSchemaId(permSchemaId)
                .setTwinflowSchemaId(flowSchemaId)
                .setTwinClassSchemaId(classSchemaId)
                .setAncestorTwinClassId(twinClass.getId())
                .setAttachmentsStorageUsedCount(0L)
                .setAttachmentsStorageUsedSize(0L)
                .setDomainUserInitiatorFeaturerId(3401)
                .setBusinessAccountInitiatorFeaturerId(3401);
        domainRepository.save(domain);
        entityManager.flush(); // Ensure it's in DB

        // Step 3: Update twin class with the real domainId
        twinClass.setDomainId(domainId);
        twinClassRepository.save(twinClass);

        // Mocking user in auth service
        try {
            stubApiUser(domainId, user);
            Mockito.when(twinClassService.isOwnerSystemType(Mockito.any())).thenReturn(false);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        field = new TwinClassFieldEntity()
                .setId(UUID.randomUUID())
                .setTwinClassId(twinClass.getId())
                .setKey("test_field")
                .setFieldTyperFeaturerId(1301)
                .setTwinSorterFeaturerId(2601)
                .setFieldInitializerFeaturerId(2601)
                .setRequired(false)
                .setSystem(false)
                .setDependentField(false)
                .setHasDependentFields(false)
                .setProjectionField(false)
                .setHasProjectedFields(false);
        fieldRepository.save(field);

        rule = new TwinClassFieldRuleEntity()
                .setId(UUID.randomUUID())
                .setFieldOverwriterFeaturerId(1301)
                .setOverwrittenValue("overwritten")
                .setOverwrittenRequired(true)
                .setRulePriority(1);
        ruleRepository.save(rule);

        TwinClassFieldRuleMapEntity ruleMap = new TwinClassFieldRuleMapEntity()
                .setId(UUID.randomUUID())
                .setTwinClassFieldId(field.getId())
                .setTwinClassFieldRuleId(rule.getId());
        ruleMapRepository.save(ruleMap);

        entityManager.flush();
    }

    @AfterEach
    public void cleanAuth() {
        authService.removeThreadLocalApiUser();
    }

    @Test
    public void testLoadRules_ResolutionOfLazyInitializationException() throws ServiceException {
        // entityManager.clear() to simulate detached state
        entityManager.clear();

        // We fetch the field from DB again to ensure it's fresh
        TwinClassFieldEntity fieldFromDb = fieldRepository.findById(field.getId()).orElseThrow();

        // This is the call we refactored
        ruleMapService.loadRules(Collections.singletonList(fieldFromDb));

        assertNotNull(fieldFromDb.getRuleKit());
        assertEquals(1, fieldFromDb.getRuleKit().size());

        TwinClassFieldRuleEntity loadedRule = fieldFromDb.getRuleKit().get(rule.getId());
        assertNotNull(loadedRule);

        // Check that it's NOT a proxy OR it is at least initialized.
        assertTrue(org.hibernate.Hibernate.isInitialized(loadedRule), "Rule should be fully initialized");
        assertFalse(loadedRule instanceof org.hibernate.proxy.HibernateProxy, "Rule should not be a proxy");

        assertEquals("overwritten", loadedRule.getOverwrittenValue());
    }

    @Test
    public void testLoadRuleFields_ResolutionOfLazyInitializationException() throws ServiceException {
        // entityManager.clear() to simulate detached state
        entityManager.clear();

        TwinClassFieldRuleEntity ruleFromDb = ruleRepository.findById(rule.getId()).orElseThrow();

        // This is the call we refactored for the reverse direction
        fieldService.loadRuleFields(Collections.singletonList(ruleFromDb));

        assertNotNull(ruleFromDb.getFieldKit());
        assertEquals(1, ruleFromDb.getFieldKit().size());

        TwinClassFieldEntity loadedField = ruleFromDb.getFieldKit().get(field.getId());
        assertNotNull(loadedField);

        assertTrue(org.hibernate.Hibernate.isInitialized(loadedField), "Field should be fully initialized");
        assertFalse(loadedField instanceof org.hibernate.proxy.HibernateProxy, "Field should not be a proxy");

        assertEquals("test_field", loadedField.getKey());
    }
}
