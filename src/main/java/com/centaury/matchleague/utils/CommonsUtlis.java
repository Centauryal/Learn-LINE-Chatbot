package com.centaury.matchleague.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CommonsUtlis {

    public static DateFormat apiDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public static DateFormat inputDate() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
    }

    public static DateFormat outputDate() {
        return new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
    }

    public static DateFormat outputDetailDate() {
        return new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Locale.getDefault());
    }
}
