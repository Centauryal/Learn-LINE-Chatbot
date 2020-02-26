package com.centaury.matchleague.controller;

import com.centaury.matchleague.model.League;
import com.centaury.matchleague.model.LineJadwalLigaModel;
import com.centaury.matchleague.model.Match;
import com.centaury.matchleague.service.BotService;
import com.centaury.matchleague.service.BotTemplate;
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
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
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
import java.util.ArrayList;
import java.util.List;
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

    private UserProfileResponse sender = null;
    private List<League> league = new ArrayList<>();
    private Match match = null;

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
            botService.reply(replyToken, greetingMessage);
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
        } else if (msgText.contains("jadwal pertandingan")
        ) {
            processJadwalDetail(replyToken, textMessage);
        } else if (msgText.contains("lihat liga")) {
            showCarouselKompetisiLiga(replyToken);
        } else if (msgText.contains("jadwal")) {
            showEventSummary(replyToken, textMessage);
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
        } else if (msgText.contains("jadwal pertandingan")
        ) {
            processJadwalDetail(replyToken, msgText);
        } else if (msgText.contains("lihat liga")) {
            showCarouselKompetisiLiga(replyToken);
        } else if (msgText.contains("jadwal")) {
            showEventSummary(replyToken, textMessage);
        } else {
            handleFallbackMessage(replyToken, new RoomSource(roomId, sender.getUserId()));
        }
    }

    private void handleOneOnOneChats(String replyToken, String textMessage) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("jadwal pertandingan")
        ) {
            processJadwalDetail(replyToken, msgText);
        } else if (msgText.contains("lihat liga")) {
            showCarouselKompetisiLiga(replyToken);
        } else if (msgText.contains("jadwal")) {
            showEventSummary(replyToken, textMessage);
        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }
    }

    private void handleFallbackMessage(String replyToken, Source source) {
        greetingMessage(replyToken, source, "Hi " + sender.getDisplayName() + ", maaf kompetisi liga tidak ada. Silahkan cek kompetisi liga yang tersedia");
    }

    private void processJadwalDetail(String replyToken, String messageText) {
        String[] words = messageText.trim().split("\\s+");
        String intent = words[0];

        if (intent.equalsIgnoreCase("jadwal pertandingan")) {
            handleDetail(replyToken, words);
        }
    }

    private void messageHelp(String replyToken) {
        List<String> messages = new ArrayList<>();

        messages.add("Pilih kompetisi liga yang ingin kamu cari.");
        messages.add("Aku akan menampilkan seminggu kedepan jadwal kompetisi liga yang akan dipilih.");

        botService.replyText(replyToken, messages.toArray(new String[messages.size()]));
    }

    private void showCarouselKompetisiLiga(String replyToken) {
        messageHelp(replyToken);

        if (league == null || league.size() < 1) {
            getKompetisiLigaData();
        }

        TemplateMessage kompetisiLiga = botTemplate.carouselKompetisiLiga(league);
        botService.reply(replyToken, kompetisiLiga);
    }

    private void getKompetisiLigaData() {

        List<League> leagueList = new ArrayList<>();
        leagueList.add(new League(
                2021,
                "Premier League",
                "Liga Utama Inggris atau Liga Premier Inggris adalah liga tertinggi dalam sistem liga sepak bola di Inggris. Kompetisi ini diikuti oleh 20 klub, liga ini menerapkan sistem promosi dan degradasi dengan English Football League."));

        leagueList.add(new League(
                2014,
                "LaLiga Santander",
                "Campeonato Nacional de Liga de Primera División, umumnya dikenal sebagai La Liga, adalah liga profesional tertinggi dalam sistem kompetisi liga sepak bola di Spanyol."));

        leagueList.add(new League(
                2002,
                "Bundesliga",
                "Bundesliga adalah sebuah liga sepak bola profesional di Jerman. Liga ini adalah liga tingkat atas pada sistem liga sepak bola di Jerman dan merupakan kompetisi utama sepak bola Jerman. Bundesliga yang diikuti oleh 18 klub dan beroperasi dengan sistem promosi dan degradasi."));

        leagueList.add(new League(
                2003,
                "Eredivisie",
                "Eredivisie adalah eselon tertinggi dari sepak bola profesional di Belanda. Liga ini didirikan pada tahun 1956. Divisi teratas terdiri dari 18 klub."));

        leagueList.add(new League(
                2015,
                "Ligue 1",
                "Ligue 1, disebut juga Ligue 1 Conforama untuk keperluan sponsor, adalah divisi teratas dalam liga sepak bola Prancis. Ligue 1 terdiri dari 20 tim sejak musim 2002-03."));

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
            System.out.println("HTTP Status of response: " + responseGet.getStatusLine().getStatusCode());

            // Get the response from the GET request
            InputStream inputStream = responseGet.getEntity().getContent();
            String encoding = StandardCharsets.UTF_8.name();
            String jsonResponse = IOUtils.toString(inputStream, encoding);

            System.out.println("Got result");
            System.out.println(jsonResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            //dicodingEvents = objectMapper.readValue(jsonResponse, DicodingEvents.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
