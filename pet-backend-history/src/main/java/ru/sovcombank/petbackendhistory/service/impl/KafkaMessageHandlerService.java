package ru.sovcombank.petbackendhistory.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendhistory.exception.InternalServerErrorException;
import ru.sovcombank.petbackendhistory.model.entity.History;
import ru.sovcombank.petbackendhistory.repository.HistoryRepository;

@Slf4j
@Service
public class KafkaMessageHandlerService {

    private final HistoryRepository historyRepository;
    private final ObjectMapper mapper;

    public KafkaMessageHandlerService(HistoryRepository historyRepository, ObjectMapper mapper) {
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${kafka.topic.transfers-history-transaction}", groupId = "${kafka.topic.history-group}")
    public void handleMessage(String jsonMessage) {
        try {
            History history = mapper.readValue(jsonMessage, History.class);
            historyRepository.save(history);
            log.info("The transfer from account {} to account{} in the amount of {} is saved in the history",
                    history.getAccountNumberFrom(), history.getAccountNumberTo(), history.getAmount());
        } catch (JsonProcessingException ex) {
            throw new InternalServerErrorException(ex);
        }
    }
}
