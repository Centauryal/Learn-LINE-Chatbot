package com.centaury.matchleague.model.match;

/**
 * Created by JacksonGenerator on 2/28/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class Score {
    @JsonProperty("duration")
    private String duration;
    @JsonProperty("winner")
    private String winner;
    @JsonProperty("penalties")
    private Penalties penalties;
    @JsonProperty("halfTime")
    private HalfTime halfTime;
    @JsonProperty("fullTime")
    private FullTime fullTime;
    @JsonProperty("extraTime")
    private ExtraTime extraTime;
}