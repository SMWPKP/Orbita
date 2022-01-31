package lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager extends ListenerAdapter {

    private final Map<Long, GuildMusicManager>  musicManagers;
    private final AudioPlayerManager playerManager;

    public PlayerManager(){
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager mng = musicManagers.get(guildId);

        if (mng == null)
        {
            mng = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, mng);
        }

        guild.getAudioManager().setSendingHandler(mng.getSendHandler());

        return mng;
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        Guild guild = event.getGuild();
        GuildMusicManager mng = getGuildAudioPlayer(guild);
        AudioPlayer player = mng.audioPlayer;
        TrackScheduler scheduler = mng.scheduler;

        if ("!play".equals(command[0]) || "!p".equals(command[0]) && command.length == 2) {
            VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
            AudioManager audioManager = event.getGuild().getAudioManager();
            audioManager.openAudioConnection(connectedChannel);
            loadAndPlay(event.getChannel(),  command[1]);
        }
        else if ("!skip".equals(command[0])) {
            skipTrack(event.getChannel());
        }
        else if("!loop".equals(command[0])){
            scheduler.setLoop(!scheduler.isLoop());
            event.getChannel().sendMessage("Loop: " + ((scheduler.isLoop()) ? "włączony" : "wyłączony")).queue();
        }

        super.onGuildMessageReceived(event);
    }

    private boolean isURL(String test) {
        try {
            new URL(test);
            return true;
        } catch ( MalformedURLException e) {
            return false;
        }
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());


        String trackURL = trackUrl;
        if (!isURL(trackUrl)) {
            trackURL = "ytsearch:" + trackUrl;
        }

        playerManager.loadItemOrdered(musicManager, trackURL , new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Dodano do kolejki " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Dodano do kolejki: " + firstTrack.getInfo().title).queue(); // + " (Pierwsza piosenka na playliscie: " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nie znaleziono:" + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Piosenka nie dziala: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {

        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped.").queue();
    }


}