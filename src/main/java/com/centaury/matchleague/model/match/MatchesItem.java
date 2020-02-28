package com.centaury.matchleague.model.match;

/**
 * Created by JacksonGenerator on 2/28/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class MatchesItem {
    @JsonProperty("matchday")
    private Integer matchday;
    @JsonProperty("awayTeam")
    private AwayTeam awayTeam;
    @JsonProperty("utcDate")
    private String utcDate;
    @JsonProperty("lastUpdated")
    private String lastUpdated;
    @JsonProperty("score")
    private Score score;
    @JsonProperty("stage")
    private String stage;
    @JsonProperty("odds")
    private Odds odds;
    @JsonProperty("season")
    private Season season;
    @JsonProperty("homeTeam")
    private HomeTeam homeTeam;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("referees")
    private List referees;
    @JsonProperty("status")
    private String status;
    @JsonProperty("group")
    private String group;

    public Integer getMatchday() {
        return matchday;
    }

    public void setMatchday(Integer matchday) {
        this.matchday = matchday;
    }

    public AwayTeam getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(AwayTeam awayTeam) {
        this.awayTeam = awayTeam;
    }

    public String getUtcDate() {
        return utcDate;
    }

    public void setUtcDate(String utcDate) {
        this.utcDate = utcDate;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Odds getOdds() {
        return odds;
    }

    public void setOdds(Odds odds) {
        this.odds = odds;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public HomeTeam getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(HomeTeam homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List getReferees() {
        return referees;
    }

    public void setReferees(List referees) {
        this.referees = referees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}