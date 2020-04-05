
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
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
import javax.management.RuntimeErrorException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.util.comparator.Comparators;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Read the json file provided in the argument[0]. The file will be avilable in
  // the classpath.
  // 1. Use #resolveFileFromResources to get actual file from classpath.
  // 2. parse the json file using ObjectMapper provided with #getObjectMapper,
  // and extract symbols provided in every trade.
  // return the list of all symbols in the same order as provided in json.
  // Test the function using gradle commands below
  // ./gradlew run --args="trades.json"
  // Make sure that it prints below String on the console -
  // ["AAPL","MSFT","GOOGL"]
  // Now, run
  // ./gradlew build and make sure that the build passes successfully
  // There can be few unused imports, you will need to fix them to make the build
  // pass.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File file = resolveFileFromResources(args[0]);
    byte[] byteArray = Files.readAllBytes(file.toPath());
    String content = new String(byteArray, "UTF-8");
    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] trade = mapper.readValue(content, PortfolioTrade[].class);

    List<String> symbols = new ArrayList<>();
    for (int i = 0; i < trade.length; i++) {
      symbols.add(trade[i].getSymbol());
    }
    return symbols;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader()
      .getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the
  // correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in
  // PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the
  // output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your
  // reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs0
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the
  // function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5. In the same window, you will see the line number of the function in the
  // stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/"
        + "ankush-kv-ME_QMONEY/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@5a9d6f02";
    String functionNameFromTestFileInStackTrace = "mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "22";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, 
      toStringOfObjectMapper,
        functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }
  // TODO: CRIO_TASK_MODULE_REST_API
  // Copy the relavent code from #mainReadFile to parse the Json into
  // PortfolioTrade list.
  // Now That you have the list of PortfolioTrade already populated in module#1
  // For each stock symbol in the portfolio trades,
  // Call Tiingo api
  // (https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=&endDate=&token=)
  // with
  // 1. ticker = symbol in portfolio_trade
  // 2. startDate = purchaseDate in portfolio_trade.
  // 3. endDate = args[1]
  // Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>
  // Note - You may have to register on Tiingo to get the api_token.
  // Please refer the the module documentation for the steps.
  // Find out the closing price of the stock on the end_date and
  // return the list of all symbols in ascending order by its close value on
  // endDate
  // Test the function using gradle commands below
  // ./gradlew run --args="trades.json 2020-01-01"
  // ./gradlew run --args="trades.json 2019-07-01"
  // ./gradlew run --args="trades.json 2019-12-03"
  // And make sure that its printing correct results.

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    byte[] byteArray = Files.readAllBytes(file.toPath());
    String content = new String(byteArray, "UTF-8");
    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] trade = mapper.readValue(content, PortfolioTrade[].class);
    LocalDate endDate = LocalDate.parse(args[1]);

    String token = "56bbbb5b8742db40dc0fc572dae6789c5546fbd2";
    String uri = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    Double closingprice = 0.0;
    List<TotalReturnsDto> totalReturn = new ArrayList<>();
    for (PortfolioTrade trades : trade) {
      String url = uri.replace("$APIKEY", token).replace("$SYMBOL", trades.getSymbol())
          .replace("$STARTDATE", trades.getPurchaseDate().toString())
            .replace("$ENDDATE", endDate.toString());

      if (trades.getPurchaseDate().compareTo(endDate) > 0) {
        throw new RuntimeErrorException(null);
      }
      TiingoCandle[] tiingoCandles;
      List<LocalDate> date = new ArrayList<>();
      try {
        tiingoCandles = new RestTemplate().getForObject(url, TiingoCandle[].class);
        if (tiingoCandles.length == 0) {
          throw new NullPointerException();
        }
      } catch (NullPointerException e) {
        return null;
      }
      for (TiingoCandle tingoo : tiingoCandles) {
        date.add(tingoo.getDate());
      }
      LocalDate last = date.get(date.size() - 1);
      for (TiingoCandle tingoo : tiingoCandles) {
        if (tingoo.getDate() == last) {
          closingprice = tingoo.getClose();
        }
      }
      TotalReturnsDto totalReturnTemp = new TotalReturnsDto(trades.getSymbol(), closingprice);
      totalReturn.add(totalReturnTemp);
    }
    Collections.sort(totalReturn, Comparator.comparing(TotalReturnsDto::getClosingPrice));
    List<String> answer = new ArrayList<>();
    for (TotalReturnsDto val : totalReturn) {
      answer.add(val.getSymbol());
    }
    return answer;
  }
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Copy the relevant code from #mainReadQuotes to parse the Json into
  // PortfolioTrade list and
  // Get the latest quotes from TIingo.
  // Now That you have the list of PortfolioTrade And their data,
  // With this data, Calculate annualized returns for the stocks provided in the
  // Json
  // Below are the values to be considered for calculations.
  // buy_price = open_price on purchase_date and sell_value = close_price on
  // end_date
  // startDate and endDate are already calculated in module2
  // using the function you just wrote #calculateAnnualizedReturns
  // Return the list of AnnualizedReturns sorted by annualizedReturns in
  // descending order.
  // use gralde command like below to test your code
  // ./gradlew run --args="trades.json 2020-01-01"
  // ./gradlew run --args="trades.json 2019-07-01"
  // ./gradlew run --args="trades.json 2019-12-03"
  // where trades.json is your json file
  
  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) 
      throws IOException, URISyntaxException {
        
    File file = resolveFileFromResources(args[0]);
    byte[] byteArray = Files.readAllBytes(file.toPath());
    String content = new String(byteArray, "UTF-8");
    ObjectMapper mapper = getObjectMapper();
    PortfolioTrade[] trade = mapper.readValue(content, PortfolioTrade[].class);
    LocalDate endDate = LocalDate.parse(args[1]);

    String token = "56bbbb5b8742db40dc0fc572dae6789c5546fbd2";
    String uri = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

    Double closingprice = 0.0; 
    Double openingprice = 0.0;
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for (PortfolioTrade trades : trade) {
      String url = uri.replace("$APIKEY", token).replace("$SYMBOL", trades.getSymbol())
            .replace("$STARTDATE", 
              trades.getPurchaseDate().toString()).replace("$ENDDATE", endDate.toString());

      if (trades.getPurchaseDate().compareTo(endDate) > 0) {
        throw new RuntimeErrorException(null);
      }
      TiingoCandle[] tiingoCandles;
      List<LocalDate> date = new ArrayList<>();
      try {
        tiingoCandles = new RestTemplate().getForObject(url, TiingoCandle[].class);
        if (tiingoCandles.length == 0) {
          throw new NullPointerException();
        }
      } catch (NullPointerException e) {
        return null;
      }
      for (TiingoCandle tingoo : tiingoCandles) {
        date.add(tingoo.getDate());
      }
      LocalDate last = date.get(date.size() - 1);
      LocalDate first = date.get(0);
      for (TiingoCandle tingoo : tiingoCandles) {
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

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // annualized returns should be calculated in two steps -
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value
  // Store the same as totalReturns
  // 2. calculate extrapolated annualized returns by scaling the same in years
  // span. The formula is
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // Store the same as annualized_returns
  // return the populated list of AnnualizedReturn for all stocks,
  // Test the same using below specified command. The build should be successful
  // ./gradlew test --tests
  // PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

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

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));
    printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateSingleReturn(args));

  }
}