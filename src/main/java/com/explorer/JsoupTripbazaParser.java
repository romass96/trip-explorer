package com.explorer;


import com.explorer.model.Country;
import com.explorer.model.Sight;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.explorer.HtmlConstants.*;

@Component
@Slf4j
public class JsoupTripbazaParser implements Parser
{
    private static final String SITE_URI = "https://www.tripzaza.com";
    private static final String EUROPE_DESTINATIONS_PAGE = SITE_URI + "/ru/destinations/category/evropa";
    private static final Set<String> exceptedTags = Set.of("Достопримечательности", "Европа");

    private final CountryLoader countryLoader = new CountryLoader();
    private Map<String, Country> countryMap;

    @Override
    @SneakyThrows
    public Set<Sight> parse() {
        loadCountries();
        Document document = parseUtf8Document(EUROPE_DESTINATIONS_PAGE);
        Elements articles = document.select("div.category-article");

        log.info("{} articles are found", articles.size());

        return articles.stream()
            .map(article -> article.select("h2.category-article-title a"))
            .map(articleTitleAnchor -> SITE_URI + articleTitleAnchor.attr(HREF_ATTRIBUTE))
            .flatMap(this::parseArticleSights)
//            .limit(100)
            .collect(Collectors.toSet());
    }

    @SneakyThrows
    private Stream<Sight> parseArticleSights(String articleUri) {
        log.info("Parsing {}", articleUri);
        Document articleDocument = parseUtf8Document(articleUri);

        Elements sightHeaders = articleDocument.select(H3_ELEMENT);
        log.info("{} headers found", sightHeaders.size());

        Set<String> tags = getTags(articleDocument);
        Optional<String> countryName = findCountryByTags(tags);
        Optional<String> cityName = countryName.map(country -> findCityByTags(country, tags)).orElseThrow();

        return sightHeaders.stream()
            .map(sightHeader -> {
                Sight sight = new Sight();
                sight.setName(sightHeader.text());
                sight.setTags(tags);

                findNextSiblingAfter(sightHeader, PARAGRAPH_ELEMENT)
                    .map(Element::text)
                    .ifPresent(sight::setDescription);

                findNextSiblingAfter(sightHeader, FIGURE_ELEMENT)
                    .map(element -> element.selectFirst(IMG_ELEMENT))
                    .map(element -> element.attr(SRC_ATTRIBUTE))
                    .ifPresent(sight::setImageLink);

                countryName.ifPresent(sight::setCountryName);
                cityName.ifPresent(sight::setCityName);

                return sight;
            });
    }

    private void loadCountries() {
        this.countryMap =  countryLoader.loadFromJson().stream()
            .collect(Collectors.toMap(Country::getName, Function.identity(), (first, second) -> second));
    }

    private Optional<Element> findNextSiblingAfter(Element headerElement, String desiredElementType) {
        Element nextElement = headerElement.nextElementSibling();
        while (nextElement != null) {
            if (nextElement.is(desiredElementType)) {
                return Optional.of(nextElement);
            }
            nextElement = nextElement.nextElementSibling();
        }
        return Optional.empty();
    }

    private Set<String> getTags(Document articleDocument) {
        Elements anchors = articleDocument.select("ul.post-tags li a");
        return anchors.stream().map(Element::text).collect(Collectors.toSet());
    }

    private Optional<String> findCountryByTags(Set<String> tags) {
        if (tags.contains("Балаклава")
            || tags.contains("Бахчисарай")
            || tags.contains("Евпатория")) {
            return Optional.of("Россия");
        }

        if (tags.isEmpty()) {
            log.info("Without tags");
            return Optional.of("Англия");
        }

        Set<String> countryNames = countryMap.keySet();

        Set<String> tagsForSearch = new HashSet<>(tags);
        tagsForSearch.removeAll(exceptedTags);
        tagsForSearch.retainAll(countryNames);

        return tagsForSearch.stream().findFirst();
    }

    private Optional<String> findCityByTags(String countryName, Set<String> tags) {
        Set<String> tagsForSearch = new HashSet<>(tags);
        tagsForSearch.removeAll(exceptedTags);
        tagsForSearch.removeAll(Set.of(countryName));

        return tagsForSearch.stream().findFirst();
    }

    private Document parseUtf8Document(String url) throws IOException {
//        return Jsoup.connect(url).get();
        return Jsoup.parse(new URL(url).openStream(), "UTF-8", url );
    }


    public static void main(String[] args) throws URISyntaxException, IOException
    {
        JsoupTripbazaParser parser = new JsoupTripbazaParser();
        Set<Sight> sights = parser.parse();

        sights.forEach(System.out::println);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("result.txt"), sights);


    }

}

