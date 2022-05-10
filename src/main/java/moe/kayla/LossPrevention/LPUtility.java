package moe.kayla.LossPrevention;

import moe.kayla.LossPrevention.command.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.util.List;

public class LPUtility {

    public static String helpCommandResponse(List<Object> objects) {
        StringBuilder formatted = new StringBuilder();
        for(Object o : objects) {
            if(o instanceof CommandHandler) {
                CommandHandler cmdHandler = (CommandHandler) o;
                //Start print...
                formatted.append("**").append(cmdHandler.getName()).append("**\n");
                //New line established, start command print.
                for(String str : cmdHandler.getCommandsAsList()) {
                    formatted.append(str).append("\n");
                }
            }
        }
        return formatted.toString(); //Return formatted string.
    }

    public static EmbedBuilder errorResponse() {
        return new EmbedBuilder().setTitle("An error occurred").setColor(new Color(0xea2727))
                .setDescription("Contact a developer.");
    }
}
