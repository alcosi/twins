package org.twins.core.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.*;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinCreate extends TwinOperation {
    private List<TwinAttachmentEntity> attachmentEntityList;
    private List<TwinLinkEntity> linksEntityList;

    //    private List<TwinFieldEntity> fieldEntityList;
//    private List<TwinFieldDataListEntity> fieldDataListEntityList;
//
//    public TwinCreate addFields(List<TwinFieldEntity> twinFieldEntityList) {
//        fieldEntityList = CollectionUtils.safeAdd(fieldEntityList, twinFieldEntityList);
//        return this;
//    }
//
//    public TwinCreate addFieldDataList(TwinFieldDataListEntity twinFieldDataListEntity) {
//        fieldDataListEntityList = CollectionUtils.safeAdd(fieldDataListEntityList, twinFieldDataListEntity);
//        return this;
//    }
//
//    public TwinCreate addFieldsDataList(List<TwinFieldDataListEntity> twinFieldDataListEntityList) {
//        fieldDataListEntityList = CollectionUtils.safeAdd(fieldDataListEntityList, twinFieldDataListEntityList);
//        return this;
//    }
}
