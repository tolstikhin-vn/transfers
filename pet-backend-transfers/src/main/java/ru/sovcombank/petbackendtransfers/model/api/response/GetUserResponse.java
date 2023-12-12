package ru.sovcombank.petbackendtransfers.model.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserResponse {

    private int id;
    private String phoneNumber;
    private boolean isActive;
    private boolean isDeleted;
}
