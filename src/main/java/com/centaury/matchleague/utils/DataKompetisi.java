package com.centaury.matchleague.utils;

import com.centaury.matchleague.model.League;

import java.util.ArrayList;

public class DataKompetisi {

    public static ArrayList<League> leagueList() {
        ArrayList<League> leagues = new ArrayList<>();

        leagues.add(new League(
                2021,
                "Premier League",
                "Liga Utama Inggris atau Liga Premier Inggris adalah liga tertinggi dalam sistem liga sepak bola di Inggris. "));

        leagues.add(new League(
                2014,
                "LaLiga Santander",
                "Laliga Santander adalah liga profesional tertinggi dalam sistem kompetisi liga sepak bola di Spanyol."));

        leagues.add(new League(
                2002,
                "Bundesliga",
                "Bundesliga adalah sebuah liga sepak bola profesional di Jerman."));

        leagues.add(new League(
                2003,
                "Eredivisie",
                "Eredivisie adalah eselon tertinggi dari sepak bola profesional di Belanda."));

        leagues.add(new League(
                2015,
                "Ligue 1",
                "Ligue 1, disebut juga Ligue 1 Conforama untuk keperluan sponsor, adalah divisi teratas dalam liga sepak bola Prancis. Ligue 1 terdiri dari 20 tim sejak musim 2002-03."));

        return leagues;
    }
}
