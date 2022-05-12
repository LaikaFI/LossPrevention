package link.alpinia.LossPrevention.command;

import link.alpinia.LossPrevention.LPUtility;
import link.alpinia.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends CommandHandler{

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
    public void executeCommand(String[] args, GuildMessageReceivedEvent e, boolean prefix) {

        if(!e.getChannel().canTalk()) {
            try {
                PrivateChannel pc = e.getAuthor().openPrivateChannel().complete();
                pc.sendMessage("**I'm unable to speak in that channel!**").queue();
            } catch (Exception ex) {
                //Message failed or something, we just stop trying if this occurs.
            }
            return;
        }
        if(e.getMessage().getMentionedUsers().contains(LossPrevention.instance.JDA.getSelfUser())) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("Hi, I'm LossPrevention.")
                    .setDescription("Hey, use `" + LossPrevention.PREFIX + "help` to get help!\n"
                    + "I'm a bot that encourages discord server security by archiving channels occasionally.")
                    .setColor(new Color(0xfcc95a));
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
        if(!prefix) { return; }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("help")) {
                EmbedBuilder eb = new EmbedBuilder().setTitle("LossPrevention Help")
                        .setColor(new Color(0xfcc95a))
                        .setDescription(LPUtility.helpCommandResponse(LossPrevention.instance.JDA.getRegisteredListeners()))
                        .setFooter("Created by Laika, click for an invite!", LossPrevention.instance.lpConfig.getInviteUrl());
                e.getChannel().sendMessageEmbeds(eb.build()).queue();
                return;
            }
            if(args[0].equalsIgnoreCase("archive")) {
                //Check to make sure the user has the "Manage Channel" permission, or the "Administrator" permission.
                try {
                    if (e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        //Member has perms, proceed.
                        e.getChannel().sendMessage("**Starting archive...**").queue();
                        if(LossPrevention.instance.doChannelArchive(e.getChannel())) {
                            e.getChannel().sendMessage("Archive Complete!").queue();
                        } else {
                            e.getChannel().sendMessage("**An error occurred. Contact a developer.").queue();
                        }
                    } else {
                        e.getChannel().sendMessage(NO_PERMS).queue();
                    }
                } catch (Exception ex) {
                    e.getChannel().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                    LossPrevention.logger.warn("An error occurred while archiving a channel.");
                    ex.printStackTrace();
                }
                return;
            }
            if(args[0].equalsIgnoreCase("delete")) {
                try {
                    if(e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        e.getChannel().sendMessage("**Starting deletion...").queue();
                        if(LossPrevention.instance.doChannelDelete(e.getChannel())) {
                            //Cool it worked
                        } else {
                            e.getChannel().sendMessage("**An error occurred...**").queue();
                        }
                    } else {
                        e.getChannel().sendMessage(NO_PERMS).queue();
                    }
                } catch (Exception ex) {
                    e.getChannel().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                    LossPrevention.logger.warn("An error occurred while deleting a channel.");
                }
                return;
            }
            if(args[0].equalsIgnoreCase("debug")) {
                LossPrevention.getLogger().info("Logging debug func");
                e.getChannel().sendMessage("Debug :boom:").queue();
            }
        }

        //If they've reached here that means they used an invalid command, send help command as a result.
        EmbedBuilder eb = new EmbedBuilder().setTitle("Hi, I'm LossPrevention.")
                .setDescription("Hey, use `" + LossPrevention.PREFIX + "help` to get help!\n"
                        + "I'm a bot that encourages discord server security by archiving channels occasionally.")
                .setColor(new Color(0xfcc95a));
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public List<String> getCommandsAsList() {
        List<String> str = new ArrayList<>();
        str.add("help - basic help command");
        str.add("archive - immediately archives a channel.");
        str.add("delete - immediately deletes a channel and creates a replacement.");
        return str;
    }
}
