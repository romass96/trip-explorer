package com.explorer;

import com.explorer.model.Sight;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TripbazaParser implements Parser
{
    private static final String SITE_URI = "https://www.tripzaza.com";
    private static final String EUROPE_DESTINATIONS_PAGE = SITE_URI + "/ru/destinations/category/evropa";
    private WebClient webClient;


//    @PostConstuct
    public void initWebClient() {
        disableHtmlUnitLogging();
        webClient = new WebClient(BrowserVersion.EDGE);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }

    private void disableHtmlUnitLogging() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
    }

    @Override
    @SneakyThrows
    public Set<Sight> parse() {
        HtmlPage htmlPage = webClient.getPage(EUROPE_DESTINATIONS_PAGE);
        DomNodeList<DomNode> articles = htmlPage.querySelectorAll("div.category-article");
        log.info("{} articles are found", articles.size());


        for (int i = 0; i < 1; i++) {
            DomNode article = articles.get(i);
            HtmlAnchor articleTitleAnchor = article.querySelector("h2.category-article-title a");
            parseArticleSights(SITE_URI + articleTitleAnchor.getHrefAttribute());
        }


//        articles.stream().peek(article -> {
//            HtmlAnchor articleTitleAnchor = article.querySelector("h2.category-article-title a");
//            parseArticleSights(SITE_URI + articleTitleAnchor.getHrefAttribute());
//        }).limit(1);


        return Collections.emptySet();
    }

    @SneakyThrows
    private Set<Sight> parseArticleSights(String articleUri) {
        log.info("Parsing {}", articleUri);
        HtmlPage articlePage = webClient.getPage(articleUri);

        DomNodeList<DomNode> sightHeaders = articlePage.querySelectorAll("h3");
        return sightHeaders.stream()
            .map(sightHeader -> {
                log.info("{} is found", sightHeader.getTextContent());

                Sight sight = new Sight();
                sight.setName(sightHeader.getTextContent());

                return sight;
            })
            .collect(Collectors.toSet());
    }

    public static void main(String[] args) {
        TripbazaParser parser = new TripbazaParser();
        parser.initWebClient();
        parser.parse();
        parser.closeWebClient();
    }


    public void closeWebClient() {
        webClient.close();
    }
}
