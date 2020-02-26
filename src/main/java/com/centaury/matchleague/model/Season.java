package com.centaury.matchleague.model;

/**
 * Created by JacksonGenerator on 2/24/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class Season {
    @JsonProperty("currentMatchday")
    private Integer currentMatchday;
    @JsonProperty("endDate")
    private String endDate;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("startDate")
    private String startDate;
}