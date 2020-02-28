package com.centaury.matchleague.utils;

import com.centaury.matchleague.model.League;

import java.util.ArrayList;

public class DataKompetisi {

    public static ArrayList<League> leagueList() {
        ArrayList<League> leagues = new ArrayList<>();

        leagues.add(new League(
                2021,
                "Premier League",
                "Liga tertinggi dalam sistem liga sepak bola di Inggris."));

        leagues.add(new League(
                2014,
                "LaLiga Santander",
                "Liga tertinggi dalam sistem liga sepak bola di Spanyol."));

        leagues.add(new League(
                2002,
                "Bundesliga",
                "Liga tertinggi dalam sistem liga sepak bola di Jerman."));

        leagues.add(new League(
                2003,
                "Eredivisie",
                "Liga tertinggi dalam sistem liga sepak bola di Belanda."));

        leagues.add(new League(
                2015,
                "Ligue 1",
                "Liga tertinggi dalam sistem liga sepak bola di Prancis."));

        return leagues;
    }
}
