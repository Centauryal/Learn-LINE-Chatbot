package com.centaury.matchleague.model;

/**
 * Created by JacksonGenerator on 2/24/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Filters {
    @JsonProperty("status")
    private List<String> status;
}