package com.explorer;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.logging.Level;

@Component
@Slf4j
public class Parser {
    private static final String EUROPE_DESTINATIONS_PAGE = "https://www.tripzaza.com/ru/destinations/category/evropa";
    private WebClient webClient;


//    @PostConstuct
    public void initWebClient() {
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
        webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
//        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
    }


    public void parse() throws IOException {
        HtmlPage htmlPage = webClient.getPage(EUROPE_DESTINATIONS_PAGE);
        DomNodeList<DomNode> articles = htmlPage.querySelectorAll("div.category-article");
        log.info("{} articles are found", articles.size());

        articles.forEach(article -> {
            DomNode articleTitleAnchor = article.querySelector("h2.category-article-title a");
        });

    }

    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        parser.initWebClient();
        parser.parse();
        parser.closeWebClient();
    }


    public void closeWebClient() {
        webClient.close();
    }
}
