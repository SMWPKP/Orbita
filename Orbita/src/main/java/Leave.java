import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;


public class Leave extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentRaw().toLowerCase();
        TextChannel channel = event.getChannel();

        if(message.equals("!leave")){
            VoiceChannel conChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if(conChannel == null) {
                channel.sendMessage("Nie jestem na żadnym kanale").queue();
                return;
            }
            event.getGuild().getAudioManager().closeAudioConnection();
            channel.sendMessage("Wyszedlem z kanalu glosowego!").queue();
        }
    }
}
