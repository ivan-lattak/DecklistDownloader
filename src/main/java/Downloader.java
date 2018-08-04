import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Downloader {
    private static final String GECKO_PROPERTY = "webdriver.gecko.driver";
    private static final String GECKO_LOCATION = "C:\\Program Files (x86)\\Firefox\\gecko\\geckodriver.exe";
    private static final int PAGE_LOAD_TIMEOUT_SECS = 10;
    private static final Function<WebDriver, Boolean> PAGE_IS_LOADED = webDriver ->
            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete");
    private static final String GET_DOCUMENT_HTML = "return document.documentElement.outerHTML";

    static void download(String decklistURL, String decklistTitle, String outputFile) {
        String fileHeader = decklistTitle + System.lineSeparator() +
                Strings.repeat("#", decklistTitle.length()) + System.lineSeparator();

        String fileContents = Jsoup
                .parse(getHtmlAfterJS(decklistURL))
                .select(".deck-view-title-bar")
                .stream()
                .filter(decklistTitleBar -> {
                    try {
                        return decklistTitleBar.selectFirst(".deck-view-title").ownText().equals(decklistTitle);
                    } catch (NullPointerException ex) {
                        throw new RuntimeException("Invalid page format: .deck-view-title-bar does not contain " +
                        " a .deck-view-title descendant", ex);
                    }
                })
                .map(element -> {
                    while (!element.hasClass("deck-view-deck-wrapper")) {
                        element = element.nextElementSibling();
                    }
                    return element;
                })
                .map(decklist -> Objects.requireNonNull(
                        decklist.selectFirst(".deck-view-deck-table > tbody"),
                        "Invalid page format: .deck-view-deck-wrapper does not contain " +
                                " a .deck-view-deck-table descendant with immediate tbody child"))
                .limit(1) // should always be true
                .flatMap(decklist -> decklist.select("tr").stream())
                .flatMap(row -> {
                    Element header = row.selectFirst(".deck-header");
                    if (header != null) {
                        return Stream.of("", header.ownText());
                    }
                    else {
                        String cardQuantity = row.selectFirst(".deck-col-qty").ownText();
                        String cardName = row.selectFirst(".deck-col-card > a").ownText();
                        return Stream.of(format(cardQuantity, cardName));
                    }
                })
                .collect(Collectors.joining(
                        System.lineSeparator(),
                        fileHeader,
                        System.lineSeparator()));

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))) {
            writer.write(fileContents);

        } catch (FileNotFoundException ex) {
            throw new RuntimeException("File cannot be created: " + outputFile, ex);
        } catch (IOException ex) {
            throw new RuntimeException("Error encountered when writing to file", ex);
        }
    }

    private static String format(String cardQuantity, String cardName) {
        return Strings.repeat(" ", 3 - cardQuantity.length()) +
                cardQuantity +
                " " +
                cardName;
    }

    private static String getHtmlAfterJS(String decklistUrl) {
        System.setProperty(GECKO_PROPERTY, GECKO_LOCATION);
        WebDriver driver = new FirefoxDriver();

        try {
            driver.get(decklistUrl);
            new WebDriverWait(driver, PAGE_LOAD_TIMEOUT_SECS).until(PAGE_IS_LOADED);
            return (String) ((JavascriptExecutor) driver).executeScript(GET_DOCUMENT_HTML);
        }
        finally {
            driver.quit();
        }
    }
}
