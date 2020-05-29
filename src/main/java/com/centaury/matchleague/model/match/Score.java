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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Penalties getPenalties() {
        return penalties;
    }

    public void setPenalties(Penalties penalties) {
        this.penalties = penalties;
    }

    public HalfTime getHalfTime() {
        return halfTime;
    }

    public void setHalfTime(HalfTime halfTime) {
        this.halfTime = halfTime;
    }

    public FullTime getFullTime() {
        return fullTime;
    }

    public void setFullTime(FullTime fullTime) {
        this.fullTime = fullTime;
    }

    public ExtraTime getExtraTime() {
        return extraTime;
    }

    public void setExtraTime(ExtraTime extraTime) {
        this.extraTime = extraTime;
    }
}