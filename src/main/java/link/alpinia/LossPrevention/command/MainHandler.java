package link.alpinia.LossPrevention.command;

import link.alpinia.LossPrevention.LPUtility;
import link.alpinia.LossPrevention.LossPrevention;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends CommandClass {

    public static String NO_PERMS = "**You lack the permission required to execute this command.**";

    @Override
    public boolean isEnabled() {
        return true; //Cannot be disabled, this plugin will always be enabled.
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        switch(name) {
            case "help" -> {
                e.deferReply().queue();
                e.getHook().sendMessage("**just... look at the command menu?**").queue();
                return;
            }
            case "archive" -> {
                e.deferReply().queue();
                //Check to make sure the user has the "Manage Channel" permission, or the "Administrator" permission.
                try {
                    var textChannel = e.getOption("channel").getAsTextChannel();
                    if (e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        //Member has perms, proceed.
                        e.getChannel().sendMessage("**Starting archive...**").queue();
                        if(LossPrevention.instance.doChannelArchive(textChannel)) {
                            textChannel.sendMessage("Archive Complete!").queue();
                        } else {
                            textChannel.sendMessage("**An error occurred. Contact a developer.").queue();
                        }
                    } else {
                        e.getHook().sendMessage(NO_PERMS).queue();
                    }
                } catch (Exception ex) {
                    e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                    LossPrevention.logger.warn("An error occurred while archiving a channel.");
                    ex.printStackTrace();
                }
                return;
            }
            case "delete" -> {
                e.deferReply().queue();
                try {
                    var textChannel = e.getOption("channel").getAsTextChannel();
                    if(e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        e.getChannel().sendMessage("**Starting deletion...").queue();
                        if(LossPrevention.instance.doChannelDelete(textChannel)) {
                            //Cool it worked
                        } else {
                            textChannel.sendMessage("**An error occurred...**").queue();
                        }
                    } else {
                        e.getHook().sendMessage(NO_PERMS).queue();
                    }
                } catch (Exception ex) {
                    e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                    LossPrevention.logger.warn("An error occurred while deleting a channel.");
                }
                return;
            }
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cis = new ArrayList<>();
        cis.add(new CommandInfo("help", "basic help command", CommandType.COMMAND));
        CommandInfo ci1 = new CommandInfo("archive", "immediately archives a channel", CommandType.COMMAND);
        ci1.addOption("channel", "the channel to archive", OptionType.CHANNEL, true);
        cis.add(ci1);
        CommandInfo ci2 = new CommandInfo("delete", "immediately deletes a channel and creates a replacement", CommandType.COMMAND);
        ci2.addOption("channel", "the channel to delete", OptionType.CHANNEL, true);
        cis.add(ci2);
        return cis;
    }
}
