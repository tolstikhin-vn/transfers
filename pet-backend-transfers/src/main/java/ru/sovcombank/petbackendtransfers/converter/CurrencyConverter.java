package ru.sovcombank.petbackendtransfers.converter;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.sovcombank.petbackendtransfers.exception.BadRequestException;
import ru.sovcombank.petbackendtransfers.exception.InternalServerErrorException;
import ru.sovcombank.petbackendtransfers.model.enums.TransferResponseMessagesEnum;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Класс для получения курса валют.
 */
@Component
public class CurrencyConverter {

    private static final String CBR_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    private final RestTemplate restTemplate;

    public CurrencyConverter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Получает текущий курс валюты.
     *
     * @param cur Код валюты.
     * @return Текущий курс валюты.
     * @throws InternalServerErrorException Если произошла внутренняя ошибка при выполнении запроса или парсинге данных.
     */
    public double getCurrentRate(String cur) {
        try {
            return parseExchangeRate(getXmlResponse(), cur);
        } catch (Exception ex) {
            throw new InternalServerErrorException();
        }
    }

    /**
     * Получает XML-ответ от сервера Центрального банка.
     *
     * @return XML-ответ от сервера Центрального банка.
     * @throws InternalServerErrorException Если произошла внутренняя ошибка при выполнении запроса.
     */
    private String getXmlResponse() {
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(CBR_URL, String.class);

        try {
            return responseEntity.getBody();
        } catch (Exception ex) {
            throw new InternalServerErrorException();
        }
    }

    /**
     * Парсит XML-данные и возвращает курс валюты.
     *
     * @param xml          XML-данные от сервера Центрального банка.
     * @param currencyCode Код валюты для поиска курса.
     * @return Курс валюты.
     * @throws BadRequestException          Если код валюты не найден в XML-данных.
     * @throws InternalServerErrorException Если произошла внутренняя ошибка при парсинге данных.
     */
    private double parseExchangeRate(String xml, String currencyCode) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            NodeList valuteList = document.getElementsByTagName("Valute");

            for (int i = 0; i < valuteList.getLength(); ++i) {
                Element valuteElement = (Element) valuteList.item(i);
                String code = valuteElement.getElementsByTagName("NumCode").item(0).getTextContent();

                // Если искомый код валюты совпадает с найденным, получаем ткущий курс из тега Value
                if (code.equals(currencyCode)) {
                    String value = valuteElement.getElementsByTagName("Value").item(0).getTextContent();
                    return Double.parseDouble(value.replace(',', '.'));
                }
            }
            throw new BadRequestException(TransferResponseMessagesEnum.BAD_REQUEST_FOR_CUR.getMessage());
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new InternalServerErrorException();
        }
    }
}
