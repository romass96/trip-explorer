package com.explorer;

import com.explorer.model.Country;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CountryLoader
{
    public static void main(String[] args)
    {
        CountryLoader loader = new CountryLoader();
        loader.loadFromJson().forEach(System.out::println);
    }

    @SneakyThrows
    public Set<Country> loadFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        try(InputStream jsonInputStream = getClass().getClassLoader().getResourceAsStream("countries.json")) {
            Map<Long, String> countryMap = objectMapper.readValue(jsonInputStream, new TypeReference<HashMap<Long,String>>() {});
            return countryMap.entrySet().stream()
                .map(countryEntrySet -> new Country(countryEntrySet.getKey(), countryEntrySet.getValue().trim()))
                .collect(Collectors.toSet());
        }
    }

//    public Set<Country> loadThroughApi() {
        //        RestTemplate restTemplate = new RestTemplateBuilder().build();
        //        restTemplate.getMessageConverters()
        //            .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        //        //        ResponseEntity<String> forEntity = restTemplate
        //        //            .getForEntity("https://namaztimes.kz/ru/api/country", String.class);
        //        //        System.out.println(forEntity.getBody());
        //        HttpHeaders headers = new HttpHeaders();
        //        headers.setAcceptCharset(List.of(StandardCharsets.UTF_8));
        //        ParameterizedTypeReference<HashMap<Long, String>> responseType =
        //            new ParameterizedTypeReference<HashMap<Long, String>>() {};
        //        RequestEntity<Void> request = RequestEntity.get("https://namaztimes.kz/ru/api/country")
        //            .accept(MediaType.APPLICATION_JSON)
        //            .headers(headers)
        //            .build();
        //        HashMap<Long, String> map = restTemplate.exchange(request, responseType).getBody();
        //        map.values().forEach(System.out::println);
//    }
}
