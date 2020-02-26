package com.centaury.matchleague.model;

public class League {

    public Integer leagueId;
    public String name;
    public String desc;

    public League(Integer leagueId, String name, String desc) {
        this.leagueId = leagueId;
        this.name = name;
        this.desc = desc;
    }

    public League() {
    }

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
