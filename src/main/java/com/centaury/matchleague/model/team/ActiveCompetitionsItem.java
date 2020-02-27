package com.centaury.matchleague.model.team;

/**
 * Created by JacksonGenerator on 2/27/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class ActiveCompetitionsItem {
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