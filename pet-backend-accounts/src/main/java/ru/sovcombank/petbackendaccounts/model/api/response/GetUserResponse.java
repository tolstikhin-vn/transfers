package ru.sovcombank.petbackendaccounts.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetUserResponse {

    private int id;

    private String phoneNumber;

    @JsonProperty(value = "isActive")
    private boolean isActive;

    @JsonProperty(value = "isDeleted")
    private boolean isDeleted;
}
