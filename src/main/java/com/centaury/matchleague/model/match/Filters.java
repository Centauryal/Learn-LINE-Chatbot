package com.centaury.matchleague.model.match;

/**
 * Created by JacksonGenerator on 2/28/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Filters {
    @JsonProperty("status")
    private List<String> status;

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }
}