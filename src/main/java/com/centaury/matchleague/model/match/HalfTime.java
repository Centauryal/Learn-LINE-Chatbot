package com.centaury.matchleague.model.match;

/**
 * Created by JacksonGenerator on 2/28/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class HalfTime {
    @JsonProperty("awayTeam")
    private String awayTeam;
    @JsonProperty("homeTeam")
    private String homeTeam;
}