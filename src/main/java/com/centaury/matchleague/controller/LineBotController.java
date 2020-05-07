package com.centaury.matchleague.controller;

import com.centaury.matchleague.model.League;
import com.centaury.matchleague.model.LineLeagueScheduleModel;
import com.centaury.matchleague.model.match.Match;
import com.centaury.matchleague.model.match.MatchesItem;
import com.centaury.matchleague.model.team.Team;
import com.centaury.matchleague.service.BotService;
import com.centaury.matchleague.service.BotTemplate;
import com.centaury.matchleague.utils.CommonsUtlis;
import com.centaury.matchleague.utils.DataKompetisi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.ReplyEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class LineBotController {

    private final Calendar calendar = Calendar.getInstance();
    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;
    @Autowired
    private BotService botService;
    @Autowired
    private BotTemplate botTemplate;
    @Autowired
    private Environment mEnv;
    private List<League> leagueList = new ArrayList<>();
    private UserProfileResponse sender = null;
    private Match match = null;
    private Team team = null;
    private String nowDate, weekDate;

    @RequestMapping(value = "/webhook", method = RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload) {

        try {
            // validasi line signature. matikan validasi ini jika masih dalam pengembangan
            /*if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }*/

            System.out.println(eventsPayload);
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            LineLeagueScheduleModel LeagueScheduleModel = objectMapper.readValue(eventsPayload, LineLeagueScheduleModel.class);

            LeagueScheduleModel.getEvents().forEach((event) -> {
                if (event instanceof JoinEvent || event instanceof FollowEvent) {
                    String replyToken = ((ReplyEvent) event).getReplyToken();
                    handleJointOrFollowEvent(replyToken, event.getSource());
                } else if (event instanceof MessageEvent) {
                    handleMessageEvent((MessageEvent) event);
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void greetingMessage(String replyToken, Source source, String additionalMessage) {
        if (sender == null) {
            String senderId = source.getSenderId();
            sender = botService.getProfile(senderId);
        }

        TemplateMessage greetingMessage = botTemplate.greetingMessage(source, sender);

        List<Message> messages = new ArrayList<>();
        if (additionalMessage != null) {
            messages.add(new TextMessage(additionalMessage));
            messages.add(greetingMessage);
        } else {
            messages.add(greetingMessage);
            messages.add(new TextMessage("Pilih kompetisi liga yang ingin kamu cari."));
            messages.add(new TextMessage("Aku akan menampilkan seminggu kedepan jadwal kompetisi liga yang akan dipilih."));
            messages.add(new TextMessage("Kamu bisa ketik 'bantu', untuk info lebih lanjut!"));
        }
        botService.reply(replyToken, messages);
    }

    private void handleJointOrFollowEvent(String replyToken, Source source) {
        greetingMessage(replyToken, source, null);
    }

    private void handleMessageEvent(MessageEvent event) {
        String replyToken = event.getReplyToken();
        MessageContent content = event.getMessage();
        Source source = event.getSource();
        String senderId = source.getSenderId();
        sender = botService.getProfile(senderId);

        if (content instanceof TextMessageContent) {
            handleTextMessage(replyToken, (TextMessageContent) content, source);
        } else {
            greetingMessage(replyToken, source, null);
        }
    }

    private void handleTextMessage(String replyToken, TextMessageContent content, Source source) {
        if (source instanceof GroupSource) {
            handleGroupChats(replyToken, content.getText(), ((GroupSource) source).getGroupId());
        } else if (source instanceof RoomSource) {
            handleRoomChats(replyToken, content.getText(), ((RoomSource) source).getRoomId());
        } else if (source instanceof UserSource) {
            handleOneOnOneChats(replyToken, content.getText());
        } else {
            botService.replyText(replyToken, "Unknown Message Source!");
        }
    }

    private void handleGroupChats(String replyToken, String textMessage, String groupId) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("liga selesai")) {
            if (sender == null) {
                botService.replyText(replyToken, "Hi, tambahkan dulu bot Jadwal Liga sebagai teman!");
            } else {
                botService.leaveGroup(groupId);
            }
        } else if (msgText.contains("lihat liga")) {
            showCarouselLeagueCompetition(replyToken);
        } else if (msgText.contains("kompetisi liga")) {
            showCarouselLeagueSchedule(replyToken, textMessage);
        } else if (msgText.contains("jadwal pertandingan")) {
            showScheduleDetail(replyToken, textMessage);
        } else if (msgText.contains("bantu")) {
            showMessageHelp(replyToken);
        } else {
            handleFallbackMessage(replyToken, new GroupSource(groupId, sender.getUserId()));
        }
    }

    private void handleRoomChats(String replyToken, String textMessage, String roomId) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("liga selesai")) {
            if (sender == null) {
                botService.replyText(replyToken, "Hi, tambahkan dulu bot Jadwal Liga sebagai teman!");
            } else {
                botService.leaveRoom(roomId);
            }
        } else if (msgText.contains("lihat liga")) {
            showCarouselLeagueCompetition(replyToken);
        } else if (msgText.contains("kompetisi liga")) {
            showCarouselLeagueSchedule(replyToken, textMessage);
        } else if (msgText.contains("jadwal pertandingan")) {
            showScheduleDetail(replyToken, textMessage);
        } else if (msgText.contains("bantu")) {
            showMessageHelp(replyToken);
        } else {
            handleFallbackMessage(replyToken, new RoomSource(roomId, sender.getUserId()));
        }
    }

    private void handleOneOnOneChats(String replyToken, String textMessage) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("lihat liga")) {
            showCarouselLeagueCompetition(replyToken);
        } else if (msgText.contains("kompetisi liga")) {
            showCarouselLeagueSchedule(replyToken, textMessage);
        } else if (msgText.contains("jadwal pertandingan")) {
            showScheduleDetail(replyToken, textMessage);
        } else if (msgText.contains("bantu")) {
            showMessageHelp(replyToken);
        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }
    }

    private void handleFallbackMessage(String replyToken, Source source) {
        greetingMessage(replyToken, source, "Hi " + sender.getDisplayName() +
                ", maaf kompetisi liga tidak ada. Silahkan cek kompetisi liga yang tersedia");
    }

    private void getLeagueScheduleData(String nameLeague, String dateFrom, String dateTo) {
        String league = nameLeague.toLowerCase();
        int idLeague = 0;

        if (league.contains("premier league")) {
            idLeague = 2021;
        } else if (league.contains("laliga santander")) {
            idLeague = 2014;
        } else if (league.contains("bundesliga")) {
            idLeague = 2002;
        } else if (league.contains("eredivisie")) {
            idLeague = 2003;
        } else if (league.contains("ligue 1")) {
            idLeague = 2015;
        }

        // Act as client with GET method
        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(ApiEndPoint.apiLeagueSchedule(idLeague, dateFrom, dateTo));
            get.addHeader("X-Auth-Token", mEnv.getProperty("API_KEY"));

            Future<HttpResponse> future = client.execute(get, null);
            HttpResponse responseGet = future.get();
            System.out.println("HTTP executed");
            System.out.println("HTTP Status of response liga: " + responseGet.getStatusLine().getStatusCode());

            // Get the response from the GET request
            InputStream inputStream = responseGet.getEntity().getContent();
            String encoding = StandardCharsets.UTF_8.name();
            String jsonResponse = IOUtils.toString(inputStream, encoding);

            System.out.println("Got result liga");
            System.out.println(jsonResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            match = objectMapper.readValue(jsonResponse, Match.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getTeamDetailData(Integer idTeam) {
        // Act as client with GET method
        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(ApiEndPoint.apiTeamDetail(idTeam));
            get.addHeader("X-Auth-Token", mEnv.getProperty("API_KEY"));

            Future<HttpResponse> future = client.execute(get, null);
            HttpResponse responseGet = future.get();
            System.out.println("HTTP executed");
            System.out.println("HTTP Status of response team: " + responseGet.getStatusLine().getStatusCode());

            // Get the response from the GET request
            InputStream inputStream = responseGet.getEntity().getContent();
            String encoding = StandardCharsets.UTF_8.name();
            String jsonResponse = IOUtils.toString(inputStream, encoding);

            System.out.println("Got result team");
            System.out.println(jsonResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            team = objectMapper.readValue(jsonResponse, Team.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showMessageHelp(String replyToken) {
        String message = "Info bantuan untuk Jadwal Liga yaitu:\n" +
                "'lihat liga' = Untuk mengetahui kompetisi liga yang tersedia.\n" +
                "'bantu' = Untuk mengetahui info dari Bot Jadwal Liga.";

        botService.replyText(replyToken, message);
    }

    private void showCarouselLeagueCompetition(String replyToken) {

        if (leagueList == null || leagueList.size() < 1) {
            leagueList = DataKompetisi.leagueList();
        }

        TemplateMessage leagueCompetition = botTemplate.carouselLeagueCompetition(leagueList);
        botService.reply(replyToken, leagueCompetition);
    }

    private void showCarouselLeagueSchedule(String replyToken, String textName) {
        Date currentDate = calendar.getTime();
        nowDate = CommonsUtlis.apiDate().format(currentDate);

        calendar.add(Calendar.DATE, 6);
        Date nextWeek = calendar.getTime();
        weekDate = CommonsUtlis.apiDate().format(nextWeek);

        if (textName.toLowerCase().contains("premier league") ||
                textName.toLowerCase().contains("laliga santander") ||
                textName.toLowerCase().contains("bundesliga") ||
                textName.toLowerCase().contains("eredivisie") ||
                textName.toLowerCase().contains("ligue 1")) {
            getLeagueScheduleData(textName, nowDate, weekDate);
        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }

        if (match.getMatches().size() > 10) {
            showScheduleMoreItem(replyToken, match);
        } else {
            TemplateMessage leagueSchedule = botTemplate.carouselLeagueSchedule(match);
            calendar.add(Calendar.DATE, -6);
            botService.reply(replyToken, leagueSchedule);
        }
    }

    private void showScheduleMoreItem(String replyToken, Match match) {
        int i, j;
        String title, matchday, team, league, matchDate = null;
        List[] lists = BotTemplate.splitSchedule(match.getMatches());
        CarouselColumn columnFirst;
        CarouselColumn columnSecond;
        List<CarouselColumn> columnsListFirst = new ArrayList<>();
        List<CarouselColumn> columnsListSecond = new ArrayList<>();
        List<Message> messageList = new ArrayList<>();

        List<MatchesItem> itemFirstSchedule = lists[0];
        for (i = 0; i < itemFirstSchedule.size(); i++) {
            team = itemFirstSchedule.get(i).getHomeTeam().getName() + "\nvs\n" + itemFirstSchedule.get(i).getAwayTeam().getName();
            league = match.getCompetition().getName();
            matchday = String.valueOf(itemFirstSchedule.get(i).getMatchday());

            try {
                Date date = CommonsUtlis.inputDate().parse(itemFirstSchedule.get(i).getUtcDate());
                if (date != null) {
                    matchDate = CommonsUtlis.outputDate().format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            title = "Matchday " + matchday + ", " + matchDate;

            columnFirst = new CarouselColumn(null,
                    title, team, Collections.singletonList(new MessageAction("Detail", "[" + (i + 1) + "]" +
                    " Jadwal Pertandingan " + league + ":\n\n" + team)));

            columnsListFirst.add(columnFirst);
        }
        CarouselTemplate templateFirst = new CarouselTemplate(columnsListFirst);

        List<MatchesItem> itemSecondSchedule = lists[1];
        for (j = 0; j < itemSecondSchedule.size(); j++) {
            team = itemSecondSchedule.get(j).getHomeTeam().getName() + "\nvs\n" + itemSecondSchedule.get(j).getAwayTeam().getName();
            league = match.getCompetition().getName();
            matchday = String.valueOf(itemSecondSchedule.get(j).getMatchday());

            try {
                Date date = CommonsUtlis.inputDate().parse(itemSecondSchedule.get(j).getUtcDate());
                if (date != null) {
                    matchDate = CommonsUtlis.outputDate().format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            title = "Matchday " + matchday + ", " + matchDate;

            columnSecond = new CarouselColumn(null,
                    title, team, Collections.singletonList(new MessageAction("Detail",
                    "[" + (itemFirstSchedule.size() + j + 1) + "]" +
                            " Jadwal Pertandingan " + league + ":\n\n" + team)));

            columnsListSecond.add(columnSecond);
        }
        CarouselTemplate templateSecond = new CarouselTemplate(columnsListSecond);

        messageList.add(new TemplateMessage("Pertandingan", templateFirst));
        messageList.add(new TemplateMessage("Pertandingan", templateSecond));

        calendar.add(Calendar.DATE, -6);
        botService.reply(replyToken, messageList);
    }

    private void showScheduleDetail(String replyToken, String textName) {
        Date currentDate = calendar.getTime();
        nowDate = CommonsUtlis.apiDate().format(currentDate);

        calendar.add(Calendar.DATE, 6);
        Date nextWeek = calendar.getTime();
        weekDate = CommonsUtlis.apiDate().format(nextWeek);

        try {
            if (match == null) {
                if (textName.toLowerCase().contains("premier league") ||
                        textName.toLowerCase().contains("laliga santander") ||
                        textName.toLowerCase().contains("bundesliga") ||
                        textName.toLowerCase().contains("eredivisie") ||
                        textName.toLowerCase().contains("ligue 1")) {
                    getLeagueScheduleData(textName, nowDate, weekDate);
                } else {
                    handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
                }
            }

            int matchIndex;
            String s = String.valueOf(textName.charAt(2));

            if (s.equals("]")) {
                matchIndex = Integer.parseInt(String.valueOf(textName.charAt(1))) - 1;
            } else {
                int dataTen = Integer.parseInt(String.valueOf(textName.charAt(1)) + textName.charAt(2));
                matchIndex = dataTen - 1;
            }

            MatchesItem matchesItem = match.getMatches().get(matchIndex);

            String nameHomeTeam = "";
            String venueHomeTeam = "";
            String addressHomeTeam = "";
            String nameAwayTeam = "";

            int homeTeamId = matchesItem.getHomeTeam().getId();
            if (homeTeamId != 0) {
                getTeamDetailData(homeTeamId);
                if (team != null) {
                    nameHomeTeam = team.getName();
                    venueHomeTeam = team.getVenue();
                    addressHomeTeam = team.getAddress();
                }
            }

            int awayTeamId = matchesItem.getAwayTeam().getId();
            if (awayTeamId != 0) {
                team = new Team();
                getTeamDetailData(awayTeamId);
                if (team != null) {
                    nameAwayTeam = team.getName();
                }
            }

            ClassLoader classLoader = getClass().getClassLoader();
            String encoding = StandardCharsets.UTF_8.name();
            String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("flex_detailjadwal.json"), encoding);

            String matchDate = "";
            try {
                Date date = CommonsUtlis.inputDate().parse(matchesItem.getUtcDate());
                if (date != null) {
                    matchDate = CommonsUtlis.outputDetailDate().format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            flexTemplate = String.format(flexTemplate,
                    botTemplate.escape(match.getCompetition().getName()),
                    botTemplate.escape(nameHomeTeam),
                    botTemplate.escape(nameAwayTeam),
                    botTemplate.escape(matchDate),
                    botTemplate.escape(venueHomeTeam),
                    botTemplate.escape(addressHomeTeam));

            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
            calendar.add(Calendar.DATE, -6);
            botService.reply(replyToken, new FlexMessage("Detail Pertandingan", flexContainer));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
