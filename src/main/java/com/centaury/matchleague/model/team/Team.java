package com.centaury.matchleague.model.team;

/**
 * Created by JacksonGenerator on 2/28/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Team {
    @JsonProperty("area")
    private Area area;
    @JsonProperty("venue")
    private String venue;
    @JsonProperty("website")
    private String website;
    @JsonProperty("address")
    private String address;
    @JsonProperty("crestUrl")
    private String crestUrl;
    @JsonProperty("tla")
    private String tla;
    @JsonProperty("founded")
    private Integer founded;
    @JsonProperty("lastUpdated")
    private String lastUpdated;
    @JsonProperty("clubColors")
    private String clubColors;
    @JsonProperty("squad")
    private List<SquadItem> squad;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("name")
    private String name;
    @JsonProperty("activeCompetitions")
    private List<ActiveCompetitionsItem> activeCompetitions;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("shortName")
    private String shortName;
    @JsonProperty("email")
    private String email;

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCrestUrl() {
        return crestUrl;
    }

    public void setCrestUrl(String crestUrl) {
        this.crestUrl = crestUrl;
    }

    public String getTla() {
        return tla;
    }

    public void setTla(String tla) {
        this.tla = tla;
    }

    public Integer getFounded() {
        return founded;
    }

    public void setFounded(Integer founded) {
        this.founded = founded;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getClubColors() {
        return clubColors;
    }

    public void setClubColors(String clubColors) {
        this.clubColors = clubColors;
    }

    public List<SquadItem> getSquad() {
        return squad;
    }

    public void setSquad(List<SquadItem> squad) {
        this.squad = squad;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ActiveCompetitionsItem> getActiveCompetitions() {
        return activeCompetitions;
    }

    public void setActiveCompetitions(List<ActiveCompetitionsItem> activeCompetitions) {
        this.activeCompetitions = activeCompetitions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}