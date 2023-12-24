package ru.sovcombank.petbackendhistory.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ru.sovcombank.petbackendhistory.exception.InternalServerErrorException;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) {
        String dateAsString;
        try {
            dateAsString = p.getValueAsString();
        } catch (IOException ex) {
            throw new InternalServerErrorException(ex);
        }
        if (dateAsString == null || dateAsString.equalsIgnoreCase("null")) {
            return null;
        }
        return LocalDateTime.parse(dateAsString);
    }
}
