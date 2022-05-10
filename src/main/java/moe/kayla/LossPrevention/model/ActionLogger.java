package moe.kayla.LossPrevention.model;

import moe.kayla.LossPrevention.LossPrevention;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.User;

import java.time.temporal.ChronoField;
import java.util.HashMap;

public class ActionLogger {

    private HashMap<Long, Integer> banOffenses = new HashMap<>();
    private HashMap<Long, Integer> channelOffenses = new HashMap<>();

    /**
     * Clear cache per audit-log scan. This prevents actions from being logged twice.
     */
    public void clearCache() {
        banOffenses.clear();
        channelOffenses.clear();
    }

    /**
     * Only logs coming through here are "ban" logs. So we don't really need to check.
     * @param entry
     */
    public void logBan(AuditLogEntry entry) {
        if(System.currentTimeMillis() - entry.getWebhook().getTimeCreated().getLong(ChronoField.INSTANT_SECONDS) > LossPrevention.instance.lpConfig.getBanTimeInMilis()) { return; }

    }

    public void restrictUser(User member) {

    }
}
