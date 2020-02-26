package com.centaury.matchleague.model;

/**
 * Created by JacksonGenerator on 2/24/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class AwayTeam {
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private Integer id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}