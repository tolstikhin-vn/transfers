package ru.sovcombank.petbackendhistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.sovcombank.petbackendhistory.model.entity.History;
import ru.sovcombank.petbackendhistory.repository.HistoryRepository;
import ru.sovcombank.petbackendhistory.service.impl.KafkaMessageHandlerService;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "transfers-history-transaction", ports = 29092)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class KafkaMessageHandlerServiceIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName
            .parse("confluentinc/cp-kafka:latest")).withKraft();

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer::getBootstrapServers);
        registry.add(ProducerConfig.CLIENT_ID_CONFIG, () -> "test-id");
        registry.add(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class::getName);
        registry.add(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class::getName);
    }

    @Autowired
    private KafkaMessageHandlerService messageHandlerService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true, "transfers-history-transaction");

    private static CountDownLatch latch;

    @BeforeAll
    static void setup() {
        postgresContainer.start();
        System.setProperty("spring.datasource.url", postgresContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresContainer.getPassword());

        kafkaContainer.start();
        System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
        latch = new CountDownLatch(1);
    }

    @AfterAll
    static void teardown() {
        postgresContainer.stop();
        kafkaContainer.stop();
    }

    @AfterEach
    public void execute() {
        jdbcTemplate.execute("TRUNCATE TABLE history");
    }

    @Test
    void testHandleMessage() throws InterruptedException, IOException {
        String jsonMessage = "{\"uuid\":\"87e91d0e-752f-43bc-800b-6b0d992a8c81\",\"clientIdFrom\":\"1\",\"clientIdTo\":\"2\","
                + "\"accountNumberFrom\":\"4200933666961739\",\"accountNumberTo\":\"4200810666632677\",\"amount\":\"100.00\","
                + "\"cur\":\"810\",\"transactionDateTime\":\"2023-12-13T18:25:25\"}";

        messageHandlerService.handleMessage(jsonMessage);
        latch.await(5, TimeUnit.SECONDS);

        assertEquals(1, historyRepository.count());
        History actualHistory = historyRepository.findAll().iterator().next();

        History expectedHistory = readFromJson(
                "entity/make-history-entity.json",
                History.class);

        assertEquals(expectedHistory, actualHistory);
    }

    private <T> T readFromJson(String jsonFileName, Class<T> requestClass) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("json/" + jsonFileName);
        return objectMapper.readValue(inputStream, requestClass);
    }
}
