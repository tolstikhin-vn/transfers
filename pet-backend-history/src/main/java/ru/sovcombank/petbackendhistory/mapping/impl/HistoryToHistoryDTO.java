package ru.sovcombank.petbackendhistory.mapping.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.sovcombank.petbackendhistory.mapping.builder.Mapper;
import ru.sovcombank.petbackendhistory.model.dto.HistoryDTO;
import ru.sovcombank.petbackendhistory.model.entity.History;

@Component
public class HistoryToHistoryDTO implements Mapper<History, HistoryDTO> {

    private final ModelMapper modelMapper;

    public HistoryToHistoryDTO(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public HistoryDTO map(History history) {
        return modelMapper.map(history, HistoryDTO.class);
    }
}
