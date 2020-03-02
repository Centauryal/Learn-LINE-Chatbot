package com.centaury.matchleague.service;

import com.centaury.matchleague.model.League;
import com.centaury.matchleague.model.match.Match;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BotTemplate {

    public TemplateMessage createBubble(String message, String actionTitle, String actionText) {
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                null,
                null,
                message,
                Collections.singletonList(new MessageAction(actionTitle, actionText))
        );

        return new TemplateMessage(actionTitle, buttonsTemplate);
    }

    public TemplateMessage greetingMessage(Source source, UserProfileResponse sender) {
        String message = "Hi %s! Jangan sampai terlewatkan nonton tim sepak bola favoritmu, yuk cek di Jadwal Liga!";
        String action = "Lihat liga";

        if (source instanceof GroupSource) {
            message = String.format(message, "kawan");
        } else if (source instanceof RoomSource) {
            message = String.format(message, "semua");
        } else if (source instanceof UserSource) {
            message = String.format(message, sender.getDisplayName());
        } else {
            message = "Unknown Message Source!";
        }

        return createBubble(message, action, action);
    }

    public TemplateMessage carouselKompetisiLiga(List<League> leagues) {
        int i;
        URI image;
        String name, desc;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        for (i = 0; i < leagues.size(); i++) {
            image = URI.create(leagues.get(i).getImage());
            name = leagues.get(i).getName();
            desc = leagues.get(i).getDesc();

            column = new CarouselColumn(image,
                    name, desc, Collections.singletonList(new MessageAction("Jadwal", "Kompetisi Liga " + name)));

            carouselColumns.add(column);
        }

        CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
        return new TemplateMessage("Kompetisi Liga", carouselTemplate);
    }

    public TemplateMessage carouselJadwalLiga(Match match) {
        int i;
        String title, matchday, team, league, matchDate = null;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        for (i = 0; i < match.getMatches().size(); i++) {
            team = match.getMatches().get(i).getHomeTeam().getName() + "\nvs\n" + match.getMatches().get(i).getAwayTeam().getName();
            league = match.getCompetition().getName();
            matchday = String.valueOf(match.getMatches().get(i).getMatchday());

            DateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            DateFormat outputDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            try {
                Date date = inputDate.parse(match.getMatches().get(i).getUtcDate());
                if (date != null) {
                    matchDate = outputDate.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            title = "Pertandingan ke-" + matchday + ", " + matchDate;

            column = new CarouselColumn(null,
                    title, team, Collections.singletonList(new MessageAction("Detail", "[" + (i + 1) + "]" +
                    " Jadwal Pertandingan " + league + ":\n\n" + team)));

            carouselColumns.add(column);
        }

        CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
        return new TemplateMessage("Pertandingan", carouselTemplate);
    }

    public String escape(String text) {
        return StringEscapeUtils.escapeJson(text.trim());
    }
}
