package org.twins.core.service.card;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.twins.core.dao.card.*;
import org.twins.core.domain.ApiUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class CardService {
    final CardRepository cardRepository;
    final CardWidgetRepository cardWidgetRepository;
    final CardAccessRepository cardAccessRepository;

    public List<CardEntity> findCards(ApiUser apiUser, UUID twinClassId) {
        List<CardAccessEntity> cardAccessEntityList = cardAccessRepository.findByTwinClassIdOrderByOrder(twinClassId);
        List<CardEntity> ret = new ArrayList<>();
        for (CardAccessEntity cardAccessEntity : cardAccessEntityList)
            ret.add(cardAccessEntity.getCard());
        return ret;
    }

    public List<CardWidgetEntity> findCardWidgets(UUID cardId) {
        List<CardWidgetEntity> cardWidgetEntityList = cardWidgetRepository.findByCardId(cardId);
        return cardWidgetEntityList;
    }
}

