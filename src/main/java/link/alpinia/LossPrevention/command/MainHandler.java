package link.alpinia.LossPrevention.command;

import link.alpinia.LossPrevention.LPUtility;
import link.alpinia.LossPrevention.LossPrevention;
import link.alpinia.LossPrevention.model.Archive;
import link.alpinia.LossPrevention.model.ArchiveMode;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandInfo;
import link.alpinia.SlashComLib.CommandType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
                switch(e.getSubcommandName()) {
                    case "now" -> {
                        try {
                            var textChannel = e.getOption("channel").getAsTextChannel();
                            if (e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                //Member has perms, proceed.
                                e.getChannel().sendMessage("**Starting archive...**").queue();
                                if (LossPrevention.instance.doChannelArchive(textChannel)) {
                                    e.getHook().sendMessage("Archive Complete!").queue();
                                } else {
                                    e.getHook().sendMessage("**An error occurred. Contact a developer.").queue();
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
                    case "create" -> {
                        try {
                            var textChannel = e.getOption("channel").getAsTextChannel();
                            if (e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                try {
                                    var type = ArchiveMode.valueOf(e.getOption("type").getAsString().toUpperCase());
                                    var newArchive = new Archive(type, textChannel);
                                    var archiveTime = e.getOption("archive-time").getAsDouble();
                                    var modToMilis = (archiveTime * 60) * 60 * 1000;
                                    newArchive.setArchiveTime((long) modToMilis);
                                    LossPrevention.instance.archiveManager.addArchive(newArchive);
                                    e.getHook().sendMessage("Registered **" + textChannel.getName() + "** " +
                                            "as a new archive with a repeating archive time of **" + archiveTime + "** hours.").queue();
                                } catch (Exception ex) {
                                    e.getHook().sendMessage("**Select a text channel.**").queue();
                                    return;
                                }
                            } else {
                                e.getHook().sendMessage(NO_PERMS).queue();
                            }
                        } catch (Exception ex) {
                            e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                            LossPrevention.logger.warn("An error occurred while creating an archive.");
                            ex.printStackTrace();
                        }
                        return;
                    }
                    case "modify" -> {
                        try {
                            var textChannel = e.getOption("channel").getAsTextChannel();
                            if(LossPrevention.instance.archiveManager.getArchiveByChannel(textChannel) == null) {
                                e.getHook().setEphemeral(true).sendMessage("**No archive is present for that text channel**").queue();
                                return;
                            }
                            var archive = LossPrevention.instance.archiveManager.getArchiveByChannel(textChannel);
                            if (e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                try {
                                    var key = e.getOption("key").getAsString();
                                    String value = e.getOption("value").getAsString();
                                    switch (key.toLowerCase()) {
                                        case "type" -> {
                                            var mode = ArchiveMode.valueOf(value.toUpperCase());
                                            archive.setType(mode);
                                            e.getHook().sendMessage("**Set archive mode to " + mode + ".**").queue();
                                            return;
                                        }
                                        case "time" -> {
                                            var modToMilis = (Long.getLong(value) * 60) * 60 * 1000;
                                            archive.setArchiveTime(modToMilis);
                                            e.getHook().sendMessage("**Set archive refresh time to " + value + " hours.**").queue();
                                            return;
                                        }
                                    }
                                } catch (Exception ex) {
                                    e.getHook().setEphemeral(true)
                                            .sendMessage("**Ensure that your values are correct, either ARCHIVAL or DELETION for type, or a number for time.**").queue();
                                    return;
                                }
                            } else {
                                e.getHook().sendMessage(NO_PERMS).queue();
                            }
                        } catch (Exception ex) {
                            e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                            LossPrevention.logger.warn("An error occurred while modifying an archive.");
                            ex.printStackTrace();
                        }
                    }
                    case "list" -> {
                        try {
                            if(e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                var str = "";
                                for(var val : LossPrevention.instance.archiveManager.fetchArchivesByGuild(e.getGuild())) {
                                    str = str + val.getChannelName()  + "[" + val.getUuid().toString() + "]" + " - " + val.getChannel().getId() + "\n";
                                }
                                e.getHook().sendMessage("**Archives for Guild[" + e.getGuild().getId() + "]**\n```" + str + "```").queue();
                            } else {
                                e.getHook().sendMessage(NO_PERMS).queue();
                            }
                        } catch (Exception ex) {
                            e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                            LossPrevention.logger.warn("An error occurred while listing channels.");
                        }
                        return;
                    }
                    case "delete" -> {
                        try {
                            if(e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                try {
                                    if(!e.getOption("channel").getChannelType().isMessage()) {
                                        e.getHook().setEphemeral(true).sendMessage("**Select a text channel.**").queue();
                                        return;
                                    }
                                    Archive arc = LossPrevention.instance.archiveManager.getArchiveByChannel(e.getOption("channel").getAsTextChannel());
                                    if(arc == null) {
                                        e.getHook().sendMessage("**That channel is not an archive.**").setEphemeral(true).queue();
                                        return;
                                    }
                                    LossPrevention.instance.archiveManager.deregisterTextChannel(e.getOption("channel").getAsTextChannel());
                                    e.getHook().sendMessage("**The archive was de-registered successfully.**").queue();
                                } catch (Exception ex) {
                                    e.getHook().sendMessage("**You need to select a text channel.**").queue();
                                    return;
                                }
                            } else {
                                e.getHook().sendMessage(NO_PERMS).queue();
                            }
                        } catch (Exception ex) {
                            e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                            LossPrevention.logger.warn("An error occurred while deleting an archive.");
                        }
                        return;
                    }
                    case "delay" -> {
                        try {
                            if(e.getMember().hasPermission(Permission.MANAGE_CHANNEL) || e.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                                if(!e.getOption("channel").getChannelType().isMessage()) {
                                    e.getHook().setEphemeral(true).sendMessage("**Select a text channel.**").queue();
                                    return;
                                }
                                Archive arc = LossPrevention.instance.archiveManager.getArchiveByChannel(e.getOption("channel").getAsTextChannel());
                                if(arc == null) {
                                    e.getHook().sendMessage("**That channel is not an archive.**").setEphemeral(true).queue();
                                    return;
                                }
                                //Fun time
                                var delay = e.getOption("amount").getAsDouble();
                                var modToMilis = (delay * 60) * 60 * 1000;
                                //now add onto prior time
                                var newTime = arc.getLastArchiveTimestamp() + modToMilis; //Update new timestamp with delay in hours.
                                arc.setLastArchiveTimestamp((long) newTime);
                                e.getHook().sendMessage("Successfully delayed time until archive for " + delay + " hours.").queue();
                            } else {
                                e.getHook().sendMessage(NO_PERMS).queue();
                            }
                        } catch (Exception ex) {
                            e.getHook().sendMessageEmbeds(LPUtility.errorResponse().build()).queue();
                            LossPrevention.logger.warn("An error occurred while delaying an archive.");
                        }
                    }
                }
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
            case "info" -> {
                e.deferReply().queue();
                Archive arc = LossPrevention.instance.archiveManager.getArchiveByChannel((TextChannel) e.getChannel());
                if(arc == null) {
                    e.getHook().sendMessage("**This channel doesn't have a registered archive.**").setEphemeral(true).queue();
                    return;
                }
                var timeTo = arc.getArchiveTime() - (System.currentTimeMillis() - arc.getLastArchiveTimestamp());
                var toHours = ((timeTo / 1000) / 60) / 60;
                var timeSince = (System.currentTimeMillis() - arc.getLastArchiveTimestamp());
                var toHour2 = ((timeSince / 1000) / 60) / 60;
                e.getHook().sendMessage("**Archive[" + arc.getUuid() + "]**\n" + "Type: `" + arc.getType().name() + "`\n"
                 + "Interval: `" + ((arc.getArchiveTime() / 1000) / 60) / 60 + " HOURS`\n"
                 + "Time Until Next Archive: `" + toHours + " HOURS`\n"
                 + "Time Since Last Archive: `" + toHour2 + " HOURS`").queue();
                return;
            }
        }
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cis = new ArrayList<>();
        cis.add(new CommandInfo("help", "basic help command", CommandType.COMMAND));
        CommandInfo ci1 = new CommandInfo("archive", "archive command", CommandType.COMMAND);
        //Subcommands for archive
        CommandInfo a1 = new CommandInfo("now", "archive the specified channel now", CommandType.SUBCOMMAND);
        a1.addOption("channel", "the channel to archive", OptionType.CHANNEL, true);
        ci1.addSubcommand(a1);
        CommandInfo a2 = new CommandInfo("create", "creates a new archive for the specified channel", CommandType.SUBCOMMAND);
        a2.addOption("channel", "the channel to create an archive for", OptionType.CHANNEL, true);
        var typeD = new OptionData(OptionType.STRING, "type", "the type of archive you want to create", true);
        typeD.addChoice("ARCHIVAL", "ARCHIVAL");
        typeD.addChoice("DELETION", "DELETION");
        a2.addOption(typeD);
        a2.addOption("archive-time", "the amount of time elapsed before a new archive is created (hours)", OptionType.NUMBER, true);
        ci1.addSubcommand(a2);
        CommandInfo a3 = new CommandInfo("modify", "modifies a pre-existing archive", CommandType.SUBCOMMAND);
        a3.addOption("channel", "the channel associated to the archive", OptionType.CHANNEL, true);
        var keyD = new OptionData(OptionType.STRING, "key", "the key of the option to change", true);
        keyD.addChoice("type", "type");
        keyD.addChoice("time", "time");
        a3.addOption(keyD);
        a3.addOption("value", "the value to set for the option", OptionType.STRING, true);
        ci1.addSubcommand(a3);
        CommandInfo a4 = new CommandInfo("delete", "deletes a pre-existing archive", CommandType.SUBCOMMAND);
        a4.addOption("channel", "the channel associated to the archive", OptionType.CHANNEL, true);
        ci1.addSubcommand(a4);
        CommandInfo a5 = new CommandInfo("list", "lists all existing archives within the guild", CommandType.SUBCOMMAND);
        ci1.addSubcommand(a5);
        cis.add(ci1);
        CommandInfo a6 = new CommandInfo("delay", "delays an archive from being archived", CommandType.SUBCOMMAND);
        a6.addOption("channel", "the channel that the archive is associated to", OptionType.CHANNEL, true);
        a6.addOption("amount", "delay (in hours) to set back the archive before the next archive", OptionType.NUMBER, true);
        ci1.addSubcommand(a6);
        CommandInfo ci2 = new CommandInfo("delete", "immediately  deletes a channel and creates a replacement", CommandType.COMMAND);
        ci2.addOption("channel", "the channel to delete", OptionType.CHANNEL, true);
        cis.add(ci2);
        CommandInfo ci3 = new CommandInfo("info", "checks the archive information for the current channel.", CommandType.COMMAND);
        cis.add(ci3);
        return cis;
    }
}
