package ru.sovcombank.petbackendhistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.sovcombank.petbackendhistory.model.api.response.GetTransferHistoryResponse;
import ru.sovcombank.petbackendhistory.service.impl.KafkaMessageHandlerService;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HistoryControllerIntegrationTest {

    private final String BASE_HOST = "http://localhost:";

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaMessageHandlerService kafkaMessageHandlerService;

    @LocalServerPort
    private int port;

    @BeforeAll
    public static void startContainers() {
        postgresContainer.start();
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());
    }

    @AfterAll
    static void stopContainer() {
        postgresContainer.stop();
    }

    @AfterEach
    public void execute() {
        jdbcTemplate.execute("TRUNCATE TABLE history");
    }

    @Test
    @Sql("/sql/insert-history.sql")
    @DisplayName("Получение истории переводов")
    public void getTransferHistorySuccessfully() throws IOException {
        GetTransferHistoryResponse expectedResponse = readFromJson(
                "response/get-transfer-history-response.json",
                GetTransferHistoryResponse.class);

        ResponseEntity<GetTransferHistoryResponse> responseEntity = restTemplate.getForEntity(
                BASE_HOST + port + "/history/1",
                GetTransferHistoryResponse.class);

        GetTransferHistoryResponse actualResponse = responseEntity.getBody();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, responseEntity.getHeaders().getContentType().toString());
        assertEquals(expectedResponse, actualResponse);
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}