package link.alpinia.LossPrevention.listener;

import link.alpinia.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class PrimaryListener extends ListenerAdapter {

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {

    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        if(event.getChannelType().isMessage()) {
            if (LossPrevention.instance.archiveManager.getArchiveByChannel((TextChannel) event.getChannel()) != null) {
                LossPrevention.instance.archiveManager.getArchiveByChannel((TextChannel) event.getChannel()).setChannelName(event.getNewValue());
            }
        }
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if(event.getChannelType().isMessage()) {
            if (LossPrevention.instance.archiveManager.getArchiveByChannel((TextChannel) event.getChannel()) != null) {
                LossPrevention.instance.archiveManager.deregisterTextChannel((TextChannel) event.getChannel());
            }
            var guild = event.getGuild();
            var logging = event.getJDA().getTextChannelById(LossPrevention.instance.lpConfig.getLogChannel());
            guild.retrieveAuditLogs()
                    .type(ActionType.CHANNEL_DELETE)
                    .limit(1)
                    .queue(list -> {
                        if(list.isEmpty()) return;
                        AuditLogEntry entry = list.get(0);
                        logging.sendMessage("**" + entry.getUser() + "** DELETED *" + event.getChannel().getName() + "*!").queue();
                        if(!entry.getGuild().getMember(entry.getUser()).getRoles().contains(entry.getGuild().getRoleById(LossPrevention.instance.lpConfig.getBypassRole()))) {
                            //No bypass, mark the user for offense (TODO)
                        }
                    });
        }
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        var guild = event.getGuild();
        var log = event.getJDA().getTextChannelById(LossPrevention.instance.lpConfig.getLogChannel());
        guild.retrieveAuditLogs()
                .type(ActionType.BAN) // filter by type
                .limit(1)
                .queue(list -> {
                    if (list.isEmpty()) return;
                    AuditLogEntry entry = list.get(0);
                    String message = String.format("**%#s** banned *%#s* with reason %s",
                            entry.getUser(), event.getUser(), entry.getReason());
                    log.sendMessage(message).queue();
                });
    }

    private boolean hasBypass(Member user) {
        if(LossPrevention.instance.lpConfig.getBypassUsers().contains(user.getId())) {
            return true;
        }
        for(Role role : user.getRoles()) {
            if(role.getIdLong() == LossPrevention.instance.lpConfig.getBypassRole()) {
                return true;
            }
        }
        return false;
    }
}
