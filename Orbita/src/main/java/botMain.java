
import lavaplayer.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;


public class botMain {
    public static void main(String[] args) throws Exception {

        //API
        JDA api = JDABuilder.createDefault("ODkyMDM1MTAxNjgzMjQ1MDY2.YVHCbQ.frQChYX9H9hdqZzph8WleRGCSjQ").build();
        api.addEventListener(new myListener());
        api.addEventListener(new PlayerManager());
        api.addEventListener(new Leave());

    }
}
