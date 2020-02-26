package com.centaury.matchleague.service;

import com.centaury.matchleague.model.League;
import com.centaury.matchleague.model.Match;
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
import org.springframework.stereotype.Service;

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
            message = String.format(message, "Group");
        } else if (source instanceof RoomSource) {
            message = String.format(message, "Room");
        } else if (source instanceof UserSource) {
            message = String.format(message, sender.getDisplayName());
        } else {
            message = "Unknown Message Source!";
        }

        return createBubble(message, action, action);
    }

    public TemplateMessage carouselKompetisiLiga(List<League> leagues) {
        int i;
        String id, name, desc;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        for (i = 0; i < leagues.size(); i++) {
            id = leagues.get(i).getLeagueId().toString();
            name = leagues.get(i).getName();
            desc = leagues.get(i).getDesc();

            column = new CarouselColumn(null, null,
                    name, desc, new MessageAction("Jadwal", "Kompetisi liga " + id), null);

            carouselColumns.add(column);
        }

        CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
        return new TemplateMessage("Kompetisi Liga", carouselTemplate);
    }

    public TemplateMessage carouselJadwal(Match match) {
        int i;
        String team, matchDate = null;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        for (i = 0; i < match.getMatches().size(); i++) {
            team = match.getMatches().get(i).getHomeTeam().getName() + "\nvs\n" + match.getMatches().get(i).getAwayTeam().getName();

            DateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            DateFormat outputDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            try {
                Date date = inputDate.parse(match.getMatches().get(i).getUtcDate());
                if (date != null) {
                    matchDate = outputDate.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            column = new CarouselColumn(null, null,
                    matchDate, team, new MessageAction("Detail", "Jadwal Pertandingan:\n" + team), null);

            carouselColumns.add(column);
        }

        CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
        return new TemplateMessage("Pertandingan", carouselTemplate);
    }
}
