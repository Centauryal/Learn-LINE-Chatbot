package com.centaury.matchleague.utils;

public class StringMessage {

    public static String greetingInfo() {
        return "Pilih kompetisi liga yang ingin kamu cari.\n" +
                "Aku akan menampilkan seminggu kedepan jadwal kompetisi liga yang akan dipilih.";
    }

    public static String updateInfo() {
        return "*UPDATED*\n" +
                "Sekarang kamu bisa melihat pertandingan yang sedang berlangsung dan telah usai di tiap kompetisi";
    }

    public static String greetingHelp() {
        return "Kamu bisa ketik 'bantu', untuk info lebih lanjut!";
    }

    public static String addBot() {
        return "Hi, tambahkan dulu bot Jadwal Liga sebagai teman!";
    }

    public static String finishLeague() {
        return "liga selesai";
    }

    public static String showLeague() {
        return "Lihat liga";
    }

    public static String competitionLeague() {
        return "kompetisi liga";
    }

    public static String liveMatch() {
        return "berlangsung kompetisi liga";
    }

    public static String finishMatch() {
        return "hasil kompetisi liga";
    }

    public static String matchSchedule() {
        return "jadwal pertandingan";
    }

    public static String matchLive() {
        return "berlangsung pertandingan";
    }

    public static String matchResult() {
        return "hasil pertandingan";
    }

    public static String txtHelp() {
        return "bantu";
    }

    public static String noCompetition(String name) {
        return "Hi " + name + ", maaf kompetisi liga tidak ada. Silahkan cek kompetisi liga yang tersedia";
    }

    public static String noCompetition() {
        return "Maaf, tidak ada jadwal pertandingan untuk seminggu kedepan.";
    }

    public static String premierLeague() {
        return "premier league";
    }

    public static String serieALeague() {
        return "serie a";
    }

    public static String laLigaLeague() {
        return "laliga santander";
    }

    public static String bundesligaLeague() {
        return "bundesliga";
    }

    public static String eredivisieLeague() {
        return "eredivisie";
    }

    public static String ligue1League() {
        return "ligue 1";
    }

    public static String messageHelp() {
        return "Info bantuan untuk Jadwal Liga yaitu:\n" +
                "'lihat liga' = Untuk mengetahui kompetisi liga yang tersedia.\n" +
                "'bantu' = Untuk mengetahui info dari Bot Jadwal Liga.";
    }

    public static String titleMatchDay(String matchday, String matchDate) {
        return "Matchday " + matchday + ", " + matchDate;
    }
}
