package moe.kayla.LossPrevention.listener;

import moe.kayla.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

/**
 * This is a terrible way to do a listener but this is just for temporary application.
 */
public abstract class LPListener extends ListenerAdapter {

    public abstract String getName();

    /**
     * Your execution function. What the CommandPlugin will include.
     * Use a CASE statement to help it out.
     * @param args - basic arguments send with the command for QOL.
     * @param e - MessageRecievedEvent for the sake of getting author or whatever.
     */
    public abstract void executeCommand(String[] args, GuildMessageReceivedEvent e, boolean prefix);

    /**
     * Overriding our GuildMessageReceivedEvent that way we can execute a cmd...
     * @param event
     */
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(event.getAuthor().getId() == LossPrevention.instance.JDA.getSelfUser().getId()) return;
        if(!event.getChannel().canTalk()) return;

        String[] args = event.getMessage().getContentDisplay().replace(LossPrevention.instance.lpConfig.getPrefix(), "").split(" ");
        //Removes prefix from args and also splits the arguments into proper args.

        boolean prefixed = event.getMessage().getContentDisplay().startsWith(LossPrevention.instance.lpConfig.getPrefix());
        executeCommand(args, event, prefixed);
    }

    /**
     * For the sake of organization. Format the strings like so:
     * command - description.
     * @return
     */
    public abstract List<String> getCommandsAsList();
}
