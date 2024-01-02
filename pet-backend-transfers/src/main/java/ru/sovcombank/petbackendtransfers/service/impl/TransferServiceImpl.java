package ru.sovcombank.petbackendtransfers.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.exception.BadRequestException;
import ru.sovcombank.petbackendtransfers.mapping.impl.TransferToGetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.RequestTypeEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.repository.TransferRepository;
import ru.sovcombank.petbackendtransfers.service.TransferService;
import ru.sovcombank.petbackendtransfers.service.TransferStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;

    private final TransferToGetTransferResponse transferToGetTransferResponse;

    private final Map<String, TransferStrategy> transferStrategies;

    private static final String REQUEST_TYPE_STR = "requestType";

    public TransferServiceImpl(
            TransferRepository transferRepository,
            TransferToGetTransferResponse transferToGetTransferResponse,
            TransferByAccountNumberService transferByAccountNumber,
            TransferByPhoneNumberService transferByPhoneNumber
    ) {
        this.transferRepository = transferRepository;
        this.transferToGetTransferResponse = transferToGetTransferResponse;
        this.transferStrategies = new HashMap<>();
        this.transferStrategies.put(RequestTypeEnum.ACCOUNT.getRequestType(), transferByAccountNumber);
        this.transferStrategies.put(RequestTypeEnum.PHONE.getRequestType(), transferByPhoneNumber);
    }

    /**
     * Совершает перевод денежных средств.
     *
     * @param requestMap Запрос на перевод.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Override
    public MakeTransferResponse makeTransfer(Map<String, Object> requestMap) {
        String requestType = (String) requestMap.get(REQUEST_TYPE_STR);
        TransferStrategy transferStrategy = transferStrategies.get(requestType);

        if (transferStrategy != null) {
            return transferStrategy.makeTransfer(requestMap);
        } else {
            log.error("BadRequestException occurred: {}", TransferResponseMessagesEnum.BAD_REQUEST_FOR_REQUEST_TYPE.getMessage());
            throw new BadRequestException(TransferResponseMessagesEnum.BAD_REQUEST_FOR_REQUEST_TYPE.getMessage());
        }
    }

    /**
     * Получить информацию о транзакции по uuid.
     *
     * @param uuid uuid транзакции.
     * @return Ответ с информацией о транзакции.
     */
    @Override
    public GetTransferResponse getTransfers(String uuid) {
        Transfer transfer = transferRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new AccountNotFoundException(
                        TransferResponseMessagesEnum.TRANSFER_NOT_FOUND.getMessage()));
        return transferToGetTransferResponse.map(transfer);
    }
}