
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;

import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication 
{

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Read the json file provided in the argument[0]. The file will be available in the classpath.
  //    1. Use #resolveFileFromResources to get actual file from classpath.
  //    2. Extract stock symbols from the json file with ObjectMapper provided by #getObjectMapper.
  //    3. Return the list of all symbols in the same order as provided in json.

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException 
  {
    List<String> symbols = new ArrayList<>();
    //To parse JSON to java object
    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file,PortfolioTrade[].class);
    
    
    for(PortfolioTrade t : trades)
    {
      symbols.add(t.getSymbol());
    }
     return symbols;
  }


  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "trades.json";
     String toStringOfObjectMapper = "ObjectMapper";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "mainReadFile";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException 
  {
    // To parse JSON to arraylist<PortfolioTrade> 

    ObjectMapper objectMapper = getObjectMapper();
    File file = resolveFileFromResources(args[0]);
    List<PortfolioTrade> trades = objectMapper.readValue(file, new TypeReference<List<PortfolioTrade>>(){});
    
    // To store the response of the tiingo api

    List<TotalReturnsDto> total = new ArrayList<>();
    RestTemplate restTemplate = new RestTemplate();
    for(PortfolioTrade trade : trades)
    {
      String uri = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + 
                   "/prices?startDate="+trade.getPurchaseDate()+"&endDate=" + args[1] + 
                   "&token=874eecb25af6550d3dd21529a8e0a50a88fc9e3d"; 
      TiingoCandle[] result = restTemplate.getForObject(uri,TiingoCandle[].class);
      if(result != null)
      {
        total.add(new TotalReturnsDto(trade.getSymbol(),result[result.length-1].getClose()));
      }
    }

    //Sorting list named total in ascending order using lambda expr in comparator

    Collections.sort(total, (a,b) -> (int) a.getClosingPrice().compareTo(b.getClosingPrice()));

    //Create List and store symbols based on ascending order

    List<String> stock = new ArrayList<>();
    for(TotalReturnsDto tDto : total)
    {
      stock.add(tDto.getSymbol());
    }
    return stock;
     
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException 
      {
            List<AnnualizedReturn> annualReturnList = new ArrayList<>();
            
            File file = resolveFileFromResources(args[0]);
            LocalDate endDate = LocalDate.parse(args[1]);
            
            // To Parse JSON into Java object
            ObjectMapper objectMapper = getObjectMapper();
            PortfolioTrade[] trades = objectMapper.readValue(file,PortfolioTrade[].class);

            //adding values to annualReturnList
            for(PortfolioTrade trade : trades)
            {
              annualReturnList.add(getAnnualizedReturn(trade,endDate));
            }

            //sorting annualReturnList in decending Order
            Collections.sort(annualReturnList ,(a,b) -> (int) b.getAnnualizedReturn().compareTo(a.getAnnualizedReturn()));

            return annualReturnList;
      }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) 
      {
        
        double totalReturn = (sellPrice - buyPrice) / buyPrice;
        double yeardiff = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365;

        double annualized_returns = Math.pow((1 + totalReturn),(1 / yeardiff))-1;

        return new AnnualizedReturn(trade.getSymbol(),annualized_returns,totalReturn);
      }

  public static AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade,LocalDate endDate) 
  {
    if(trade.getPurchaseDate().compareTo(endDate) >= 0)
    {
      throw new RuntimeException();
    }
    // To store the resopnse of tiingo api
    String uri = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + 
                 "/prices?startDate="+trade.getPurchaseDate()+"&endDate=" + endDate + 
                 "&token=874eecb25af6550d3dd21529a8e0a50a88fc9e3d"; 
    
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] result = restTemplate.getForObject(uri,TiingoCandle[].class);

     //To calucalate annualized Return based on TiingoCandle[] result
    if(result == null)
    {
      return new AnnualizedReturn(trade.getSymbol(),Double.NaN,Double.NaN);
    }

    TiingoCandle earlyStock = result[0];
    TiingoCandle latestStock = result[result.length-1];
    Double buyPrice = earlyStock.getOpen();
    Double sellPrice = latestStock.getClose();
    
    AnnualizedReturn annualReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
    return annualReturn;
    
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception 
      {
        RestTemplate restTemplate = new RestTemplate();
        PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager("tiingo",restTemplate);
       //String file = args[0];
        File file = resolveFileFromResources(args[0]);
        LocalDate endDate = LocalDate.parse(args[1]);
        //String contents =  readFileAsString(file);
        ObjectMapper objectMapper = getObjectMapper();
        PortfolioTrade[] trades = objectMapper.readValue(file,PortfolioTrade[].class);
        return portfolioManager.calculateAnnualizedReturn(Arrays.asList(trades), endDate);
      }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

