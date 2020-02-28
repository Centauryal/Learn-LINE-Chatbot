package com.centaury.matchleague.model.team;

/**
 * Created by JacksonGenerator on 2/28/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class SquadItem {
    @JsonProperty("role")
    private String role;
    @JsonProperty("nationality")
    private String nationality;
    @JsonProperty("countryOfBirth")
    private String countryOfBirth;
    @JsonProperty("shirtNumber")
    private String shirtNumber;
    @JsonProperty("name")
    private String name;
    @JsonProperty("dateOfBirth")
    private String dateOfBirth;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("position")
    private String position;
}