package ru.he3hauka.toolvisuals.client.chat.reply;

import java.util.List;
import java.util.regex.Pattern;

public final class ReplyPresets {
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{1,16}");
    public static final List<String> WIN = List.of(
            "! Слушай, {player}, тебе надо отдохнуть, ты слишком слаб, пойди травку пощупай не знаю.",
            "! {player}, хороший бой! Спасибо за игру.",
            "! GG, {player}!",
            "! {player}, этот раунд за мной. До реванша!",
            "! {player}, отправляю тебя на короткий перерыв. Без обид!");
    public static final List<String> LOSE = List.of(
            "! О боже, {player}, какое ты лакерное, порадуйся выбитой сетке",
            "! {player}, хороший бой! В этот раз ты сильнее.",
            "! GG, {player}, реванш?",
            "! {player}, раунд твой, но я ещё вернусь!",
            "! {player}, ладно, победу одалживаю. В следующий раз заберу!");
    private ReplyPresets() {}
    public static String message(boolean win, int preset, String name) {
        if (name == null || !NAME.matcher(name).matches()) return null;
        var messages = win ? WIN : LOSE;
        return messages.get(Math.clamp(preset, 0, messages.size() - 1)).replace("{player}", name);
    }
}
