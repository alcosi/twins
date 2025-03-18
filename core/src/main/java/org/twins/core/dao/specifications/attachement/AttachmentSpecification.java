package org.twins.core.dao.specifications.attachement;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.cambium.common.util.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.specifications.CommonSpecification;

import java.util.Collection;
import java.util.List;

import static org.cambium.common.util.SpecificationUtils.getPredicate;

public class AttachmentSpecification extends CommonSpecification<TwinAttachmentEntity> {

}
