package com.pomodoro.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        var pomodoroBot = new PomodoroBot();
        telegramBotsApi.registerBot(pomodoroBot);
        new Thread(() -> {
            try {
                pomodoroBot.checkTimer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).run();
    }
    static class PomodoroBot extends TelegramLongPollingBot{

        @Override
        public String getBotUsername() {
            return "Pomodoro bot";
        }

        @Override
        public String getBotToken() {
            return "5818747634:AAHGjN6ehq7lnr8fEPZsW_47CJN6HJIVst0";
        }

        private static final ConcurrentHashMap<Timer, Long> timers = new ConcurrentHashMap<>();

        enum TimerType{
            WORK,
            BREAK
        }

        record Timer(Instant timer, TimerType timerType){};
        @Override
        public void onUpdateReceived(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()){
                var chatId = update.getMessage().getChatId();
                if(update.getMessage().getText().equals("/start")){
                    sendMsg(chatId.toString(), """
                            Pomodoro - сделай рабочее время эффективным!
                            Задай время работы и отдыха через пробел. Например 1 1.
                            Время  в минутах""");
                } else {
                    var args = update.getMessage().getText().split(" ");
                    if (args.length >= 1){
                        var workTime = Instant.now().plus(Long.parseLong(args[0]), ChronoUnit.MINUTES);
                        timers.put(new Timer(workTime, TimerType.WORK), chatId);
                        if(args.length >= 2){
                            var breakTime = workTime.plus(Long.parseLong(args[1]), ChronoUnit.MINUTES);
                            timers.put(new Timer(breakTime, TimerType.BREAK), chatId);

                        }

                    }
                }
            }

        }
        public void checkTimer() throws InterruptedException {
            while (true){
                System.out.println("Count timers users" + timers.size());
                timers.forEach((timer, userid) -> {
                    if(Instant.now().isAfter(timer.timer)){
                        timers.remove(timer);
                        switch(timer.timerType){
                            case WORK -> sendMsg(userid.toString(),"Пора отдыхать");
                            case BREAK -> sendMsg(String.valueOf(userid), "Таймер завершил работу.");
                        }
                    }
                });
                Thread.sleep(1000L);
            }
        }
        private void sendMsg(String chatId, String text) {
            SendMessage msg = new SendMessage(chatId, text);
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }
    static class EchoBot extends TelegramLongPollingBot{

        @Override
        public String getBotUsername() {
            return "Попугай бот";
        }

        @Override
        public String getBotToken() {
            return "5818747634:AAHGjN6ehq7lnr8fEPZsW_47CJN6HJIVst0";
        }
        public static int userCount = 0;
        @Override
        public void onUpdateReceived(Update update) {

            if (update.hasMessage() && update.getMessage().hasText()){
                var chatId = update.getMessage().getChatId().toString();
                if(update.getMessage().getText().equals("/start")){
                    userCount++;

                    sendMsg(chatId, "Privet, I popugay bot!");
                } else {
                    sendMsg(chatId, update.getMessage().getText());
                }
            }
            System.out.println("Количество пользователей "+ userCount);
        }

        private void sendMsg(String chatId, String text) {
            SendMessage msg = new SendMessage(chatId, text);
            try {
                execute(msg);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
