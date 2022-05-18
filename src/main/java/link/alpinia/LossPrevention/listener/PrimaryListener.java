package link.alpinia.LossPrevention.listener;

import link.alpinia.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
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
        }
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
