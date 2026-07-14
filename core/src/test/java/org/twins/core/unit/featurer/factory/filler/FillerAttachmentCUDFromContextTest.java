package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.status.StatusType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerAttachmentCUDFromContext;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FillerAttachmentCUDFromContextTest extends BaseUnitTest {

    private final FillerAttachmentCUDFromContext filler = new FillerAttachmentCUDFromContext();

    private FactoryItem buildCreateItem(EntityCUD<TwinAttachmentEntity> cud) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var ctx = new FactoryContext(null, null);
        ctx.setAttachmentCUD(cud);
        return new FactoryItem().setOutput(output).setFactoryContext(ctx);
    }

    private FactoryItem buildUpdateItem(EntityCUD<TwinAttachmentEntity> cud) {
        var dbTwin = new TwinEntity().setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var update = new TwinUpdate();
        update.setDbTwinEntity(dbTwin).setTwinEntity(new TwinEntity());
        var ctx = new FactoryContext(null, null);
        ctx.setAttachmentCUD(cud);
        return new FactoryItem().setOutput(update).setFactoryContext(ctx);
    }

    private EntityCUD<TwinAttachmentEntity> cudWithCreate(TwinAttachmentEntity att) {
        var cud = new EntityCUD<TwinAttachmentEntity>();
        cud.setCreateList(List.of(att));
        return cud;
    }

    @Nested
    class Fill {

        @Test
        void fill_createOutput_setsClonedCreateListOnOutput() throws ServiceException {
            // NAME promises: clone the context AttachmentCUD's createList onto the output TwinCreate.
            var att = new TwinAttachmentEntity();
            var factoryItem = buildCreateItem(cudWithCreate(att));

            filler.fill(new Properties(), List.of(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getAttachmentEntityList());
            assertEquals(1, create.getAttachmentEntityList().size());
            // must be a clone, not the same reference
            assertNotSame(att, create.getAttachmentEntityList().get(0));
        }

        @Test
        void fill_updateOutput_setsClonedCudOnOutput() throws ServiceException {
            var att = new TwinAttachmentEntity();
            var factoryItem = buildUpdateItem(cudWithCreate(att));

            filler.fill(new Properties(), List.of(factoryItem), null, false);

            var update = (TwinUpdate) factoryItem.getOutput();
            assertNotNull(update.getAttachmentCUD());
            assertNotNull(update.getAttachmentCUD().getCreateList());
            assertEquals(1, update.getAttachmentCUD().getCreateList().size());
        }

        @Test
        void fill_nullContextCud_throwsStepError() {
            // No attachments in context -> error.
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var ctx = new FactoryContext(null, null);
            var factoryItem = new FactoryItem().setOutput(output).setFactoryContext(ctx);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
