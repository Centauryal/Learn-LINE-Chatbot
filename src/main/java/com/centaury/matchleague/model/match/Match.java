package com.centaury.matchleague.model.match;

/**
 * Created by JacksonGenerator on 2/24/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Match {
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("competition")
    private Competition competition;
    @JsonProperty("filters")
    private Filters filters;
    @JsonProperty("matches")
    private List<MatchesItem> matches;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }

    public List<MatchesItem> getMatches() {
        return matches;
    }

    public void setMatches(List<MatchesItem> matches) {
        this.matches = matches;
    }
}