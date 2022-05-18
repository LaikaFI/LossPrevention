package link.alpinia.LossPrevention;

import link.alpinia.LossPrevention.command.MainHandler;
import link.alpinia.LossPrevention.database.LPDatabase;
import link.alpinia.LossPrevention.model.ArchiveManager;
import link.alpinia.LossPrevention.listener.PrimaryListener;
import link.alpinia.LossPrevention.model.Archive;
import link.alpinia.SlashComLib.CommandClass;
import link.alpinia.SlashComLib.CommandRegistrar;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LossPrevention {

    public static LossPrevention instance;

    public JDA JDA;

    public static String PREFIX;

    private boolean active;

    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static Logger logger = LoggerFactory.getLogger("LossPrevention");

    public LPConfig lpConfig;

    private CommandRegistrar commandRegistrar;

    public ArchiveManager archiveManager;

    public LPDatabase database;

    public List<CommandClass> activeCommands;

    //We check every 60 seconds.
    private final long timeToArchiveCheck = 60000;

    private final Timer archiveTimer = new Timer();

    public LossPrevention() {
        instance = this;
    }

    public static void main(String[] args) {
        try {
            LossPrevention lp = new LossPrevention();
            lp.run();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        logger.info("Starting LossPrevention.");
        lpConfig = new LPConfig();
        lpConfig.loadConfigurations();
        archiveManager = new ArchiveManager();
        commandRegistrar = new CommandRegistrar();
        activeCommands = commandRegistrar.getCommandClasses("link.alpinia.LossPrevention.command");

        PREFIX = lpConfig.getPrefix();
        //After CFG is loaded we can now load the bot.
        try {
            startInstance();
        } catch (LoginException | InterruptedException e) {
            logger.error("Failed to start LossPrevention, check the stack trace and contact a developer if you cannot fix it on your own.");
            e.printStackTrace();
        }

        //Load DB
        database = lpConfig.getDatabase();
        database.loadArchives();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        logger.info("Shutdown hook registered.");
        logger.info("Invite Link: " + lpConfig.getInviteUrl());
        commandRegistrar.registerCommands(this.JDA, activeCommands);

        archiveTimer.scheduleAtFixedRate(new TimerTask() {
                                             @Override
                                             public void run() {
                                                 checkArchiveTasks();
                                             }
                                         }, 5000, timeToArchiveCheck);
        //We do pizazz magic here.
        while(active) {

        }
    }

    public void shutdown() {
        database.saveArchives();
    }

    public void startInstance() throws LoginException, InterruptedException {
        JDABuilder builder = JDABuilder.createDefault(lpConfig.getToken())
                .addEventListeners(new PrimaryListener())
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.watching("admins"));
        JDA = builder.build().awaitReady();
        active = true;
    }

    /**
     * Because JDA/Discord is garbage we have to scan audit logs per-server in order to make sure that
     * things aren't being abused. Cause some dipshit at discord or JDA decided to not include "getModerator()"
     * functions in their fucking moderation code. Seriously, fuck you guys.
     *
     * SideNote: THIS SHOULD PROBABLY BE REWRITTEN A LOT.
     */
    public void checkLogs() {
        for(Guild guild : JDA.getGuilds()) {
            //Can't check audits so revoke.
            if(!guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) { return; }
            guild.retrieveAuditLogs().type(ActionType.BAN)
                    .limit(10)
                    .queue(list -> {
                        if(list.isEmpty()) { return; }
                        //We record the action unless its already pre-existing.

                    });
        }
    }

    public void checkArchiveTasks() {
        logger.info("Archive Check - " + formatter.format(new Date()));
        for(Archive archive : archiveManager.getChannelsForArchival()) {
            //Don't archive channels with unset times.
            if(!(archive.getArchiveTime() == 0)) {
                if (archive.getArchiveTime() < (System.currentTimeMillis() - archive.getLastArchiveTimestamp())) {
                    doArchiveTask(archive);
                }
            }
        }
    }

    public void doArchiveTask(Archive archive) {
        try {
            JDA.getTextChannelById(lpConfig.getLogChannel()).sendMessage("**Archiving " + archive.getChannelName() + " now...**").queue();
            Date date = new Date();
            //Archive is overdue for handling.
            TextChannel toBeArchived = archive.getChannel();
            String oldName = toBeArchived.getName();
            Category archiveCategory = archive.getChannel().getParentCategory(); //The category that the archived channel was apart of.
            List<PermissionOverride> oldPerms = toBeArchived.getPermissionOverrides(); //We save these for the new channel.
            toBeArchived.sendMessage("**This channel is now being archived as of " + formatter.format(date) + ".**").queue();
            for (PermissionOverride perm : toBeArchived.getPermissionOverrides()) {
                //we strip perms from all.
                perm.delete().queue();
            }
            toBeArchived.putPermissionOverride(JDA.getRolesByName("@everyone", true).get(0)).setDeny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND).queue();
            toBeArchived.getManager().setName(toBeArchived.getName() + "-" + formatter.format(date)).queue();
            TextChannel newChannel = archiveCategory.createTextChannel(oldName).complete();
            newChannel.getManager().sync().queue(); //Sync the channel with the category, THEN apply our permission overrides.
            for(PermissionOverride perm : oldPerms) {
                newChannel.putPermissionOverride(perm.getPermissionHolder()).setDeny(perm.getDenied()).setAllow(perm.getAllowed()).queue();
            }
            newChannel.sendMessage("**Archive Complete.**").queue();
            archive.setChannel(newChannel); //Finally, configure our archive for the new channel.
        } catch (Exception ex) {
            archive.getChannel().sendMessage("**An exception occurred while trying to archive the channel. Check the logs please.").queue();
            ex.printStackTrace();
        }
    }

    public boolean doChannelArchive(TextChannel channel) {
        try {
            JDA.getTextChannelById(lpConfig.getLogChannel()).sendMessage("**Archiving " + channel.getName() + " now...**").queue();
            Date date = new Date();
            //Archive is overdue for handling.
            String oldName = channel.getName();
            Category archiveCategory = channel.getParentCategory(); //The category that the archived channel was apart of.
            List<PermissionOverride> oldPerms = channel.getPermissionOverrides(); //We save these for the new channel.
            channel.sendMessage("**This channel is now being archived as of " + formatter.format(date) + ".**").queue();
            for (PermissionOverride perm : channel.getPermissionOverrides()) {
                //we strip perms from all.
                perm.delete().queue();
            }

            channel.getManager().setName(channel.getName() + "-" + formatter.format(date)).queue();
            for(Role role : channel.getGuild().getRoles()) {
                if(role.getName().equalsIgnoreCase("@everyone")) {
                    channel.putPermissionOverride(role).setDeny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND).completeAfter(2, TimeUnit.SECONDS);
                }
            }
            TextChannel newChannel = archiveCategory.createTextChannel(oldName).complete();
            newChannel.getManager().sync().queue(); //Sync the channel with the category, THEN apply our permission overrides.
            for(PermissionOverride perm : oldPerms) {
                newChannel.putPermissionOverride(perm.getPermissionHolder()).setDeny(perm.getDenied()).setAllow(perm.getAllowed()).queue();
            }
            logger.info("Archive Function completed.");
            newChannel.sendMessage("**Archive Complete.**").queue();
            return true;
        } catch (Exception ex) {
            logger.error("Failed an archival function task.");
            channel.sendMessage("**An exception occurred while trying to archive the channel. Check the logs please.").queue();
            ex.printStackTrace();
            return false;
        }
    }

    public boolean doChannelDelete(TextChannel channel) {
        try {
            this.JDA.getTextChannelById(lpConfig.getLogChannel()).sendMessage("**Deleting " + channel.getName() + " now...**").queue();
            Date date = new Date();
            //Deleted channel is overdue for handling.
            String oldName = channel.getName();
            Category archiveCategory = channel.getParentCategory(); //The category that the deleted channel was apart of.
            List<PermissionOverride> oldPerms = channel.getPermissionOverrides(); //We save these for the new channel.
            //channel.sendMessage("**This channel is now being deleted as of " + formatter.format(date) + ".**").queue();
            for (PermissionOverride perm : channel.getPermissionOverrides()) {
                //we strip perms from all.
                perm.delete().queue();
            }
            channel.delete().complete();
            TextChannel newChannel = archiveCategory.createTextChannel(oldName).complete();
            newChannel.getManager().sync().queue(); //Sync the channel with the category, THEN apply our permission overrides.
            for(PermissionOverride perm : oldPerms) {
                newChannel.putPermissionOverride(perm.getPermissionHolder()).setDeny(perm.getDenied()).setAllow(perm.getAllowed()).queue();
            }
            logger.info("Deletion Function completed.");
            newChannel.sendMessage("**Deletion Complete.**").queue();
            return true;
        } catch (Exception ex) {
            logger.error("Failed an deletion function task.");
            channel.sendMessage("**An exception occurred while trying to delete the channel. Check the logs please.").queue();
            ex.printStackTrace();
            return false;
        }
    }

    public TextChannel tryFetchTextChannel(String id) {
        return this.JDA.getTextChannelById(id);
    }

    public static Logger getLogger() {
        return logger;
    }
}
