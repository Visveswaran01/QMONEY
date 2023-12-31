
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager 
{

  private StockQuotesService stockQuotesService;
  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public PortfolioManagerImpl(StockQuotesService stockQuotesService) 
  {
    this.stockQuotesService = stockQuotesService;
  
  }


public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException 
      {

        return stockQuotesService.getStockQuote(symbol, from, to);
        
      }

  
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws StockQuoteServiceException 
  {
    List<AnnualizedReturn> annualReturnList = new ArrayList<>();

    for(PortfolioTrade trade : portfolioTrades)
    {
       annualReturnList.add(getAnnualizedReturn(trade,endDate));
    }
    //sorting annualReturnList in decending Order
        Collections.sort(annualReturnList ,(a,b) -> (int) b.getAnnualizedReturn().compareTo(a.getAnnualizedReturn()));
    
    return annualReturnList;
  }
  
  public AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade,LocalDate endDate)
      throws StockQuoteServiceException
  {
    AnnualizedReturn annualizedReturn;
    try
    {
      // To extract the stocks between purchase date and end date
      List<Candle> totalStock = getStockQuote(trade.getSymbol(),trade.getPurchaseDate(),endDate);
      
      // To get early and latest srock from totalStock
          Candle earlyStock = totalStock.get(0);
          Candle latestStock = totalStock.get(totalStock.size()-1);

      // to get buy price and sell price
      double buyPrice = earlyStock.getOpen();
      double sellPrice = latestStock.getClose();
      double totalReturn = (sellPrice - buyPrice) / buyPrice;
      double yeardiff = (double) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365;

      double annualized_returns = Math.pow((1 + totalReturn),(1 / yeardiff))-1;

      annualizedReturn = new AnnualizedReturn(trade.getSymbol(),annualized_returns,totalReturn);

    }
    catch(JsonProcessingException e )
    {
      annualizedReturn = new AnnualizedReturn(trade.getSymbol(),Double.NaN,Double.NaN);

    }
    catch(StockQuoteServiceException e)
    {
      annualizedReturn = new AnnualizedReturn(trade.getSymbol(),Double.NaN,Double.NaN);
    }
    return annualizedReturn; 

  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate, int numThreads) throws InterruptedException, StockQuoteServiceException 
      {
          // TODO Auto-generated method stub
         // return null;
          List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
  List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
  final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

  for (int i = 0; i < portfolioTrades.size(); i++) {
    PortfolioTrade trade = portfolioTrades.get(i);
    Callable<AnnualizedReturn> callableTask = () -> {
      return getAnnualizedAndTotalReturns(trade, endDate);
    };
    Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
    futureReturnsList.add(futureReturns);
  }

  for (int i = 0; i < portfolioTrades.size(); i++) {
    Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
    try {
      AnnualizedReturn returns = futureReturns.get();
      annualizedReturns.add(returns);
    } catch (ExecutionException e) {
      throw new StockQuoteServiceException("Error when calling the API", e);

    }
  }
  // Collections.sort(annualizedReturns, Collections.reverseOrder());
   Collections.sort(annualizedReturns ,(a,b) -> (int) b.getAnnualizedReturn().compareTo(a.getAnnualizedReturn()));
    return annualizedReturns;
  }

      private AnnualizedReturn getAnnualizedAndTotalReturns(PortfolioTrade trade, LocalDate endDate)
          throws StockQuoteServiceException 
      {
        LocalDate startDate = trade.getPurchaseDate();
        String symbol = trade.getSymbol(); 
       
        Double buyPrice = 0.0, sellPrice = 0.0;
       
        try {
          LocalDate startLocalDate = trade.getPurchaseDate();
       
          List<Candle> stocksStartToEndFull = getStockQuote(symbol, startLocalDate, endDate);
       
          Collections.sort(stocksStartToEndFull, (candle1, candle2) -> { 
            return candle1.getDate().compareTo(candle2.getDate()); 
          });
          
          Candle stockStartDate = stocksStartToEndFull.get(0);
          Candle stocksLatest = stocksStartToEndFull.get(stocksStartToEndFull.size() - 1);
       
          buyPrice = stockStartDate.getOpen();
          sellPrice = stocksLatest.getClose();
          endDate = stocksLatest.getDate();
       
        } catch (JsonProcessingException e) {
          throw new RuntimeException();
        }
        Double totalReturn = (sellPrice - buyPrice) / buyPrice;
       
        long daysBetweenPurchaseAndSelling = ChronoUnit.DAYS.between(startDate, endDate);
        Double totalYears = (double) (daysBetweenPurchaseAndSelling) / 365;
       
        Double annualizedReturn = Math.pow((1 + totalReturn), (1 / totalYears)) - 1;
        return new AnnualizedReturn(symbol, annualizedReturn, totalReturn);
       
      }
      
  }
    