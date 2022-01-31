
import lavaplayer.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;


public class botMain {
    public static void main(String[] args) throws Exception {

        //API
        JDA api = JDABuilder.createDefault(/*Token API */).build();
        api.addEventListener(new myListener());
        api.addEventListener(new PlayerManager());
        api.addEventListener(new Leave());

    }
}
