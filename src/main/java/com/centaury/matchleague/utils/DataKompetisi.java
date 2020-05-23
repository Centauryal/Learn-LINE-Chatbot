package com.centaury.matchleague.utils;

import com.centaury.matchleague.model.League;

import java.util.ArrayList;

public class DataKompetisi {

    public static ArrayList<League> leagueList() {
        ArrayList<League> leagues = new ArrayList<>();

        leagues.add(new League(
                2021,
                "https://jadwalliga.herokuapp.com/image/premier-league",
                "Premier League",
                "Liga tertinggi dalam sistem liga sepak bola di Inggris."));

        leagues.add(new League(
                2019,
                "https://jadwalliga.herokuapp.com/image/serie-a",
                "Serie A",
                "Liga tertinggi dalam sistem liga sepak bola di Italia."));

        leagues.add(new League(
                2014,
                "https://jadwalliga.herokuapp.com/image/laliga-santander",
                "LaLiga Santander",
                "Liga tertinggi dalam sistem liga sepak bola di Spanyol."));

        leagues.add(new League(
                2002,
                "https://jadwalliga.herokuapp.com/image/bundesliga",
                "Bundesliga",
                "Liga tertinggi dalam sistem liga sepak bola di Jerman."));

        leagues.add(new League(
                2003,
                "https://jadwalliga.herokuapp.com/image/eredivisie",
                "Eredivisie",
                "Liga tertinggi dalam sistem liga sepak bola di Belanda."));

        leagues.add(new League(
                2015,
                "https://jadwalliga.herokuapp.com/image/ligue1",
                "Ligue 1",
                "Liga tertinggi dalam sistem liga sepak bola di Prancis."));

        return leagues;
    }
}
