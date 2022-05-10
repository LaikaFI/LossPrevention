package moe.kayla.LossPrevention.listener;

import moe.kayla.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class PrimaryListener extends ListenerAdapter {

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {

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
