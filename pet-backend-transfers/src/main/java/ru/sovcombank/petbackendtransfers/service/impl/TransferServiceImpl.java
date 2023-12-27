package ru.sovcombank.petbackendtransfers.service.impl;

import org.springframework.stereotype.Service;
import ru.sovcombank.petbackendtransfers.exception.AccountNotFoundException;
import ru.sovcombank.petbackendtransfers.exception.BadRequestException;
import ru.sovcombank.petbackendtransfers.mapping.impl.MapToMakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.mapping.impl.MapToMakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.mapping.impl.TransferToGetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByAccountRequest;
import ru.sovcombank.petbackendtransfers.model.api.request.MakeTransferByPhoneRequest;
import ru.sovcombank.petbackendtransfers.model.api.response.GetTransferResponse;
import ru.sovcombank.petbackendtransfers.model.api.response.MakeTransferResponse;
import ru.sovcombank.petbackendtransfers.model.entity.Transfer;
import ru.sovcombank.petbackendtransfers.model.enums.RequestTypeEnum;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;
import ru.sovcombank.petbackendtransfers.repository.TransferRepository;
import ru.sovcombank.petbackendtransfers.service.TransferService;

import java.util.Map;
import java.util.UUID;

@Service
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final MapToMakeTransferByAccountRequest mapToMakeTransferByAccountRequest;
    private final MapToMakeTransferByPhoneRequest mapToMakeTransferByPhoneRequest;
    private final TransferToGetTransferResponse transferToGetTransferResponse;
    private final TransferByAccountNumberService transferByAccountNumber;
    private final TransferByPhoneNumberService transferByPhoneNumber;

    public TransferServiceImpl(
            TransferRepository transferRepository,
            MapToMakeTransferByAccountRequest mapToMakeTransferByAccountRequest,
            MapToMakeTransferByPhoneRequest mapToMakeTransferByPhoneRequest,
            TransferToGetTransferResponse transferToGetTransferResponse,
            TransferByAccountNumberService transferByAccountNumber,
            TransferByPhoneNumberService transferByPhoneNumber
    ) {
        this.transferRepository = transferRepository;
        this.mapToMakeTransferByAccountRequest = mapToMakeTransferByAccountRequest;
        this.mapToMakeTransferByPhoneRequest = mapToMakeTransferByPhoneRequest;
        this.transferToGetTransferResponse = transferToGetTransferResponse;
        this.transferByAccountNumber = transferByAccountNumber;
        this.transferByPhoneNumber = transferByPhoneNumber;
    }

    /**
     * Совершает перевод денежных средств.
     *
     * @param requestMap Запрос на перевод.
     * @return Ответ с сообщением о выполнении перевода.
     */
    @Override
    public MakeTransferResponse makeTransfer(Map<String, Object> requestMap) {
        String requestType = (String) requestMap.get("requestType");

        // Если перевод происходит по номеру счета
        if (RequestTypeEnum.ACCOUNT.getRequestType().equals(requestType)) {
            MakeTransferByAccountRequest makeTransferByAccountRequest =
                    mapToMakeTransferByAccountRequest.map(requestMap);
            return transferByAccountNumber.makeTransferByAccount(makeTransferByAccountRequest);
            // Если перевод происходит по номеру телефона
        } else if (RequestTypeEnum.PHONE.getRequestType().equals(requestType)) {
            MakeTransferByPhoneRequest makeTransferByPhoneRequest =
                    mapToMakeTransferByPhoneRequest.map(requestMap);
            return transferByPhoneNumber.makeTransferByPhone(makeTransferByPhoneRequest);
        } else {
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
                        TransferResponseMessagesEnum.TRANSFER_NOT_FOUND.getMessage())
                );
        return transferToGetTransferResponse.map(transfer);
    }
}