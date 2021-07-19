package com.explorer.model;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
public class Sight {
    private String name;
    private String countryName;
    private String cityName;
    private String description;
    private Set<String> tags;
    private String imageLink;
}
