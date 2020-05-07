package com.centaury.matchleague.controller;

public class ApiEndPoint {

    public static String apiLeagueSchedule(Integer id, String dateFrom, String dateTo) {
        return "https://api.football-data.org/v2/competitions/" + id + "/matches?dateFrom="
                + dateFrom + "&dateTo=" + dateTo + "&status=SCHEDULED";
    }

    public static String apiTeamDetail(Integer id) {
        return "https://api.football-data.org/v2/teams/" + id;
    }
}
