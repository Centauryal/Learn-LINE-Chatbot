package com.centaury.matchleague.controller;

import com.centaury.matchleague.model.League;
import com.centaury.matchleague.model.LineJadwalLigaModel;
import com.centaury.matchleague.model.match.Match;
import com.centaury.matchleague.model.match.MatchesItem;
import com.centaury.matchleague.model.team.Team;
import com.centaury.matchleague.service.BotService;
import com.centaury.matchleague.service.BotTemplate;
import com.centaury.matchleague.utils.DataKompetisi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineSignatureValidator;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class LineBotController {

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
    private DateFormat useDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    private String nowDate, weekDate;

    @RequestMapping(value = "/webhook", method = RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload) {

        try {
            // validasi line signature. matikan validasi ini jika masih dalam pengembangan
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            System.out.println(eventsPayload);
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            LineJadwalLigaModel jadwalLigaModel = objectMapper.readValue(eventsPayload, LineJadwalLigaModel.class);

            jadwalLigaModel.getEvents().forEach((event) -> {
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

        if (additionalMessage != null) {
            List<Message> messages = new ArrayList<>();
            messages.add(new TextMessage(additionalMessage));
            messages.add(greetingMessage);
            botService.reply(replyToken, messages);
        } else {
            List<Message> messages = new ArrayList<>();
            messages.add(greetingMessage);
            messages.add(new TextMessage("Pilih kompetisi liga yang ingin kamu cari."));
            messages.add(new TextMessage("Aku akan menampilkan seminggu kedepan jadwal kompetisi liga yang akan dipilih."));
            messages.add(new TextMessage("Kamu bisa ketik 'bantu', untuk info lebih lanjut!"));
            botService.reply(replyToken, messages);
        }
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
            showCarouselKompetisiLiga(replyToken);
        } else if (msgText.contains("kompetisi liga")) {
            showCarouselJadwalLiga(replyToken, textMessage);
        } else if (msgText.contains("jadwal pertandingan")) {
            showJadwalDetail(replyToken, textMessage);
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
            showCarouselKompetisiLiga(replyToken);
        } else if (msgText.contains("kompetisi liga")) {
            showCarouselJadwalLiga(replyToken, textMessage);
        } else if (msgText.contains("jadwal pertandingan")) {
            showJadwalDetail(replyToken, textMessage);
        } else if (msgText.contains("bantu")) {
            showMessageHelp(replyToken);
        } else {
            handleFallbackMessage(replyToken, new RoomSource(roomId, sender.getUserId()));
        }
    }

    private void handleOneOnOneChats(String replyToken, String textMessage) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("lihat liga")) {
            showCarouselKompetisiLiga(replyToken);
        } else if (msgText.contains("kompetisi liga")) {
            showCarouselJadwalLiga(replyToken, textMessage);
        } else if (msgText.contains("jadwal pertandingan")) {
            showJadwalDetail(replyToken, textMessage);
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

    private void getJadwalLigaData(Integer ligaId, String dateFrom, String dateTo) {
        // Act as client with GET method
        String URI = "https://api.football-data.org/v2/competitions/" + ligaId + "/matches?dateFrom="
                + dateFrom + "&dateTo=" + dateTo + "&status=SCHEDULED";
        System.out.println("URI: " + URI);

        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(URI);
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

    private void getTeamDetailData(Integer teamId) {
        // Act as client with GET method
        String URI = "https://api.football-data.org/v2/teams/" + teamId;
        System.out.println("URI: " + URI);

        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            //Use HTTP Get to retrieve data
            HttpGet get = new HttpGet(URI);
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

    private void showCarouselKompetisiLiga(String replyToken) {

        if (leagueList == null || leagueList.size() < 1) {
            leagueList = DataKompetisi.leagueList();
        }

        TemplateMessage kompetisiLiga = botTemplate.carouselKompetisiLiga(leagueList);
        botService.reply(replyToken, kompetisiLiga);
    }

    private void showCarouselJadwalLiga(String replyToken, String textName) {
        String nameLeague = textName.toLowerCase();

        Date currentDate = calendar.getTime();
        nowDate = useDate.format(currentDate);

        calendar.add(Calendar.DATE, 6);
        Date nextWeek = calendar.getTime();
        weekDate = useDate.format(nextWeek);

        if (nameLeague.contains("premier league")) {
            getJadwalLigaData(2021, nowDate, weekDate);
        } else if (nameLeague.contains("laliga santander")) {
            getJadwalLigaData(2014, nowDate, weekDate);
        } else if (nameLeague.contains("bundesliga")) {
            getJadwalLigaData(2002, nowDate, weekDate);
        } else if (nameLeague.contains("eredivisie")) {
            getJadwalLigaData(2003, nowDate, weekDate);
        } else if (nameLeague.contains("ligue 1")) {
            getJadwalLigaData(2015, nowDate, weekDate);
        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }

        TemplateMessage jadwalLiga = botTemplate.carouselJadwalLiga(match);
        botService.reply(replyToken, jadwalLiga);
    }

    private void showJadwalDetail(String replyToken, String textName) {
        String league = textName.toLowerCase();
        Date currentDate = calendar.getTime();
        nowDate = useDate.format(currentDate);

        calendar.add(Calendar.DATE, 6);
        Date nextWeek = calendar.getTime();
        weekDate = useDate.format(nextWeek);

        try {
            if (match == null) {
                if (league.contains("premier league")) {
                    getJadwalLigaData(2021, nowDate, weekDate);
                } else if (league.contains("primera division")) {
                    getJadwalLigaData(2014, nowDate, weekDate);
                } else if (league.contains("bundesliga")) {
                    getJadwalLigaData(2002, nowDate, weekDate);
                } else if (league.contains("eredivisie")) {
                    getJadwalLigaData(2003, nowDate, weekDate);
                } else if (league.contains("ligue 1")) {
                    getJadwalLigaData(2015, nowDate, weekDate);
                } else {
                    handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
                }
            }

            int matchIndex = Integer.parseInt(String.valueOf(textName.charAt(1))) - 1;
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

            DateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            DateFormat outputDate = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm", Locale.getDefault());
            String matchDate = "";
            try {
                Date date = inputDate.parse(matchesItem.getUtcDate());
                if (date != null) {
                    matchDate = outputDate.format(date);
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
            botService.reply(replyToken, new FlexMessage("Detail Pertandingan", flexContainer));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
