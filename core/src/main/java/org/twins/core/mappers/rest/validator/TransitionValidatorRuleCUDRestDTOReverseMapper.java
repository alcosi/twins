//package org.twins.core.mappers.rest.validator;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.twins.core.dao.validator.TwinflowTransitionValidatorRuleEntity;
//import org.twins.core.domain.EntityCUD;
//import org.twins.core.dto.rest.validator.cud.TransitionValidatorRuleCudDTOv1;
//import org.twins.core.mappers.rest.RestSimpleDTOMapper;
//import org.twins.core.mappers.rest.mappercontext.MapperContext;
//import org.twins.core.service.twinflow.TwinflowTransitionValidatorService;
//
//
//@Component
//@RequiredArgsConstructor
//public class TransitionValidatorRuleCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionValidatorRuleCudDTOv1, EntityCUD<TwinflowTransitionValidatorRuleEntity>> {
//
//    //todo think about cud logic
//
//    private final TransitionValidatorRuleCreateRestDTOReverseMapper transitionValidatorRuleCreateRestDTOReverseMapper;
//    private final TransitionValidatorRuleUpdateRestDTOReverseMapper validatorUpdateRestDTOReverseMapper;
//    private final TwinflowTransitionValidatorService twinflowTransitionValidatorService;
//
//    @Override
//    public void map(TransitionValidatorRuleCudDTOv1 src, EntityCUD<TwinflowTransitionValidatorRuleEntity> dst, MapperContext mapperContext) throws Exception {
//        dst
//                .setUpdateList(validatorUpdateRestDTOReverseMapper.convertCollection(src.getUpdate()))
//                .setCreateList(transitionValidatorRuleCreateRestDTOReverseMapper.convertCollection(src.getCreate()))
//                .setDeleteList(twinflowTransitionValidatorService.findEntitiesSafe(src.getDelete()).getList());
//    }
//}
