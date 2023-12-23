
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException,StockQuoteServiceException
  {
    List<Candle> stocksList;
    // TODO Auto-generated method stub
      if(from.compareTo(to) >= 0)
      {
            throw new RuntimeException();
      }
      try
      {
        // To build uri based on arguments we got 
        String uri = buildUri(symbol, from, to);

        //store the resopnse of api as string
        String stocks = restTemplate.getForObject(uri,String.class);
        ObjectMapper objectMapper = getObjectMapper();
        TiingoCandle[] result = objectMapper.readValue(stocks,TiingoCandle[].class);

       // converting array to arraylist
       stocksList = Arrays.asList(result); 
      }
      catch(NullPointerException e)
      {
        throw new StockQuoteServiceException("Error occured when requesting response with Tiingo api",e.getCause());
      }
       return stocksList ;
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

  private ObjectMapper getObjectMapper() 
  {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) 
  {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/" + symbol+
                            "/prices?startDate="+ startDate +"&endDate="+ endDate +
                            "&token=874eecb25af6550d3dd21529a8e0a50a88fc9e3d";
      
       return uriTemplate;
  }

}
