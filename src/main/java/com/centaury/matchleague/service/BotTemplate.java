package com.centaury.matchleague.service;

import com.centaury.matchleague.model.League;
import com.centaury.matchleague.model.match.Match;
import com.centaury.matchleague.model.match.MatchesItem;
import com.centaury.matchleague.utils.CommonsUtils;
import com.centaury.matchleague.utils.StringMessage;
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
import java.text.ParseException;
import java.util.*;

@Service
public class BotTemplate {

    public static List[] splitSchedule(List<MatchesItem> matches) {
        int size = matches.size();

        List<MatchesItem> firstSchedule = new ArrayList<>(matches.subList(0, (size) / 2));
        List<MatchesItem> secondSchedule = new ArrayList<>(matches.subList((size) / 2, size));

        return new List[]{firstSchedule, secondSchedule};
    }

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
        String action = StringMessage.showLeague();

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

    public TemplateMessage carouselLeagueCompetition(List<League> leagues) {
        int i;
        URI image;
        String name, desc;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        for (i = 0; i < leagues.size(); i++) {
            image = URI.create(leagues.get(i).getImage());
            name = leagues.get(i).getName();
            desc = leagues.get(i).getDesc();

            column = new CarouselColumn(image, name, desc,
                    Arrays.asList(
                            new MessageAction("Jadwal", "Kompetisi Liga " + name),
                            new MessageAction("Sedang Berlangsung", "Berlangsung Kompetisi " + name),
                            new MessageAction("Hasil", "Hasil Kompetisi " + name)
                    ));

            carouselColumns.add(column);
        }

        CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
        return new TemplateMessage("Kompetisi Liga", carouselTemplate);
    }

    public TemplateMessage carouselLeagueSchedule(Match match) {
        int i;
        String title, matchday, team, teamHome, teamAway, league, message, action, matchDate = null;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        if (match.getMatches() == null || match.getMatches().size() < 1) {
            message = StringMessage.noCompetition();
            action = StringMessage.showLeague();

            return createBubble(message, action, action);
        } else {
            for (i = 0; i < match.getMatches().size(); i++) {
                teamHome = match.getMatches().get(i).getHomeTeam().getName();
                teamAway = match.getMatches().get(i).getAwayTeam().getName();

                team = teamHome + "\nvs\n" + teamAway;
                league = match.getCompetition().getName();
                matchday = String.valueOf(match.getMatches().get(i).getMatchday());

                try {
                    Date date = CommonsUtils.inputDate().parse(match.getMatches().get(i).getUtcDate());
                    if (date != null) {
                        matchDate = CommonsUtils.outputDate().format(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                title = StringMessage.titleMatchDay(matchday, matchDate);

                column = new CarouselColumn(null,
                        title, team, Collections.singletonList(new MessageAction("Detail", "[" + (i + 1) + "]" +
                        " Jadwal Pertandingan " + league + ":\n\n" + team)));

                carouselColumns.add(column);
            }

            CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
            return new TemplateMessage("Pertandingan", carouselTemplate);
        }
    }

    public TemplateMessage carouselLeagueFinish(Boolean play, Match match) {
        int i;
        String title, matchday, team, league, message, action, matchDate = null;
        String teamHome, scoreHome, scoreAway, teamAway;
        CarouselColumn column;
        List<CarouselColumn> carouselColumns = new ArrayList<>();

        if (match.getMatches() == null || match.getMatches().size() < 1) {
            message = StringMessage.noCompetition();
            action = StringMessage.showLeague();

            return createBubble(message, action, action);
        } else {
            for (i = 0; i < match.getMatches().size(); i++) {
                teamHome = match.getMatches().get(i).getHomeTeam().getName();
                scoreHome = match.getMatches().get(i).getScore().getFullTime().getHomeTeam();
                teamAway = match.getMatches().get(i).getAwayTeam().getName();
                scoreAway = match.getMatches().get(i).getScore().getFullTime().getAwayTeam();

                team = teamHome + "   " + scoreHome +
                        "\nvs\n" +
                        teamAway + "   " + scoreAway;
                league = match.getCompetition().getName();
                matchday = String.valueOf(match.getMatches().get(i).getMatchday());

                try {
                    Date date = CommonsUtils.inputDate().parse(match.getMatches().get(i).getUtcDate());
                    if (date != null) {
                        matchDate = CommonsUtils.outputDate().format(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                title = StringMessage.titleMatchDay(matchday, matchDate);

                if (play) {
                    column = new CarouselColumn(null,
                            title, team, Collections.singletonList(new MessageAction("Detail", "[" + (i + 1) + "]" +
                            " Berlangsung Pertandingan " + league + ":\n\n" + team)));
                } else {
                    column = new CarouselColumn(null,
                            title, team, Collections.singletonList(new MessageAction("Detail", "[" + (i + 1) + "]" +
                            " Hasil Pertandingan " + league + ":\n\n" + team)));
                }

                carouselColumns.add(column);
            }

            CarouselTemplate carouselTemplate = new CarouselTemplate(carouselColumns);
            return new TemplateMessage("Pertandingan", carouselTemplate);
        }
    }

    public String escape(String text) {
        return StringEscapeUtils.escapeJson(text.trim());
    }
}
