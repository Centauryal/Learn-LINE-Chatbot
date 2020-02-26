package com.centaury.matchleague.model;

/**
 * Created by JacksonGenerator on 2/24/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class Competition {
    @JsonProperty("area")
    private Area area;
    @JsonProperty("lastUpdated")
    private String lastUpdated;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("plan")
    private String plan;
}