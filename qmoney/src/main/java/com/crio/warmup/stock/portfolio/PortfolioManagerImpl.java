package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {


  protected RestTemplate restTemplate;  
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  //CHECKSTYLE:OFF
  
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,LocalDate endDate)
  {
    Double closingprice = 0.0; 
    Double openingprice = 0.0;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for (PortfolioTrade trades : portfolioTrades) {
      if (trades.getPurchaseDate().compareTo(endDate) > 0) {
        throw new RuntimeErrorException(null);
      }
      List<Candle> candles;
      List<LocalDate> date = new ArrayList<>();
      try {
        candles = getStockQuote(trades.getSymbol(), trades.getPurchaseDate(), endDate);
        if (candles.size() == 0) {
          throw new NullPointerException();
        }
      } catch (NullPointerException e) {
        return null;
      } catch (JsonProcessingException j) {
        return null;
      }
      for (Candle tingoo : candles) {
        date.add(tingoo.getDate());
      }
      LocalDate last = date.get(date.size() - 1);
      LocalDate first = date.get(0);
      for (Candle tingoo : candles) {
        if (tingoo.getDate() == last) {
          closingprice = tingoo.getClose();
        }
        if (tingoo.getDate() == first) {
          openingprice = tingoo.getOpen();
        }
      }
      annualizedReturns.add(calculateAnnualizedReturns(last,
          trades, openingprice, closingprice));
    }
    Collections.sort(annualizedReturns, 
    Collections.reverseOrder(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn)));
    return annualizedReturns;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    
    if (trade.getPurchaseDate().compareTo(endDate) > 0) {
      throw new RuntimeErrorException(null);
    }
    double totalreturn = (sellPrice - buyPrice) / buyPrice;
    double days = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
    days /= 365.4;
    double annualizedreturn = Math.pow((1.0 + totalreturn), 1.0 / days) - 1.0;
    return new AnnualizedReturn(trade.getSymbol(), annualizedreturn, totalreturn);
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo thirdparty APIs to a separate function.
  //  It should be split into fto parts.
  //  Part#1 - Prepare the Url to call Tiingo based on a template constant,
  //  by replacing the placeholders.
  //  Constant should look like
  //  https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  //  Where ? are replaced with something similar to <ticker> and then actual url produced by
  //  replacing the placeholders with actual parameters.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
      String uri = buildUri(symbol, from, to);
      Candle[] candles;
      try {
        candles = restTemplate.getForObject(uri, Candle[].class);
        if (candles.length == 0) {
          throw new NullPointerException();
        }
      } catch (NullPointerException e) {
        return null;
      }
     return Arrays.asList(candles);
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uri = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
       String token = "56bbbb5b8742db40dc0fc572dae6789c5546fbd2";

       String url = uri.replace("$APIKEY", token).replace("$SYMBOL", symbol)
            .replace("$STARTDATE", startDate.toString())
              .replace("$ENDDATE", endDate.toString());
        return url;
  }
}
