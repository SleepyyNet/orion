package com.avairebot.orion.commands.music;

import com.avairebot.orion.Orion;
import com.avairebot.orion.audio.AudioHandler;
import com.avairebot.orion.audio.TrackResponse;
import com.avairebot.orion.audio.VoiceConnectStatus;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PlayCommand extends Command {

    public PlayCommand(Orion orion) {
        super(orion, false);
    }

    @Override
    public String getName() {
        return "Play Music Command";
    }

    @Override
    public String getDescription() {
        return "Plays the music that you request for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <song>` - Plays the given song");
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("play", "request");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:guild,2,5");
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(message, "Missing music `query`, you must include a link to the song you want to listen to!");
        }

        VoiceConnectStatus voiceConnectStatus = AudioHandler.connectToVoiceChannel(message);
        if (!voiceConnectStatus.isSuccess()) {
            MessageFactory.makeWarning(message, voiceConnectStatus.getErrorMessage()).queue();
            return false;
        }

        AudioHandler.loadAndPlay(message, buildTrackRequestString(args)).handle((Consumer<TrackResponse>) (TrackResponse response) -> {
            message.delete().reason("Song request, removing song to cleanup chat").queue();

            if (response.getMusicManager().getPlayer().isPaused()) {
                response.getMusicManager().getPlayer().setPaused(false);
            }

            if (response.getMusicManager().getPlayer().getPlayingTrack() != null) {
                if (response.isPlaylist()) sendPlaylistResponse(message, response);
                else sendTrackResponse(message, response);
            }
        }, throwable -> MessageFactory.makeError(message, throwable.getMessage()).queue());

        return true;
    }

    private void sendPlaylistResponse(Message message, TrackResponse response) {
        AudioPlaylist playlist = (AudioPlaylist) response.getAudioItem();

        MessageFactory.makeSuccess(message, "<@%s> has added %s songs from the [%s](%s) playlist to the queue. There are `%s` song(s) ahead of it in the queue.",
                message.getAuthor().getId(),
                playlist.getTracks().size(),
                playlist.getName(),
                response.getTrackUrl(),
                AudioHandler.getQueueSize(response.getMusicManager())
        ).queue();
    }

    private void sendTrackResponse(Message message, TrackResponse response) {
        AudioTrack track = (AudioTrack) response.getAudioItem();

        MessageFactory.makeSuccess(message,
                "<@%s> has added [%s](%s) to the queue. There are `%s` song(s) ahead of it in the queue.",
                message.getAuthor().getId(),
                track.getInfo().title,
                track.getInfo().uri,
                AudioHandler.getQueueSize(response.getMusicManager())
        ).queue();
    }

    private String buildTrackRequestString(String[] args) {
        String string = String.join(" ", args);

        try {
            new URL(string);

            return string;
        } catch (MalformedURLException ex) {
            return "ytsearch:" + string;
        }
    }
}
