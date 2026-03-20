package dev.execute.advancedjoin.model;

import java.time.LocalDate;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String messageId;
    private String soundId;
    private String particleId;
    private String animationId;
    private String quitMessageId;
    private boolean messageEnabled;
    private boolean soundEnabled;
    private boolean particleEnabled;
    private boolean animationEnabled;
    private boolean quitMessageEnabled;
    private int streak;
    private String lastJoinDate; // ISO format: yyyy-MM-dd
    private boolean firstJoin;

    // Default constructor
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.messageId = "default";
        this.soundId = "levelup";
        this.particleId = "heart";
        this.animationId = "NONE";
        this.quitMessageId = "default";
        this.messageEnabled = true;
        this.soundEnabled = true;
        this.particleEnabled = true;
        this.animationEnabled = false;
        this.quitMessageEnabled = true;
        this.streak = 0;
        this.lastJoinDate = "";
        this.firstJoin = true;
    }

    // Full constructor for DB load
    public PlayerData(UUID uuid, String messageId, String soundId, String particleId,
                      String animationId, String quitMessageId,
                      boolean messageEnabled, boolean soundEnabled,
                      boolean particleEnabled, boolean animationEnabled,
                      boolean quitMessageEnabled, int streak,
                      String lastJoinDate, boolean firstJoin) {
        this.uuid = uuid;
        this.messageId = messageId;
        this.soundId = soundId;
        this.particleId = particleId;
        this.animationId = animationId;
        this.quitMessageId = quitMessageId;
        this.messageEnabled = messageEnabled;
        this.soundEnabled = soundEnabled;
        this.particleEnabled = particleEnabled;
        this.animationEnabled = animationEnabled;
        this.quitMessageEnabled = quitMessageEnabled;
        this.streak = streak;
        this.lastJoinDate = lastJoinDate;
        this.firstJoin = firstJoin;
    }

    // ─── Streak logic ─────────────────────────────────────────────────

    public void updateStreak() {
        String today = LocalDate.now().toString();
        if (lastJoinDate == null || lastJoinDate.isEmpty()) {
            streak = 1;
        } else {
            LocalDate last = LocalDate.parse(lastJoinDate);
            LocalDate now  = LocalDate.now();
            long diff = java.time.temporal.ChronoUnit.DAYS.between(last, now);
            if (diff == 0) {
                // Same day, no change
            } else if (diff == 1) {
                streak++;
            } else {
                streak = 1; // reset
            }
        }
        lastJoinDate = today;
    }

    // ─── Getters ──────────────────────────────────────────────────────
    public UUID getUuid()               { return uuid; }
    public String getMessageId()        { return messageId; }
    public String getSoundId()          { return soundId; }
    public String getParticleId()       { return particleId; }
    public String getAnimationId()      { return animationId; }
    public String getQuitMessageId()    { return quitMessageId; }
    public boolean isMessageEnabled()   { return messageEnabled; }
    public boolean isSoundEnabled()     { return soundEnabled; }
    public boolean isParticleEnabled()  { return particleEnabled; }
    public boolean isAnimationEnabled() { return animationEnabled; }
    public boolean isQuitMessageEnabled(){ return quitMessageEnabled; }
    public int getStreak()              { return streak; }
    public String getLastJoinDate()     { return lastJoinDate; }
    public boolean isFirstJoin()        { return firstJoin; }

    // ─── Setters ──────────────────────────────────────────────────────
    public void setMessageId(String v)        { this.messageId = v; }
    public void setSoundId(String v)          { this.soundId = v; }
    public void setParticleId(String v)       { this.particleId = v; }
    public void setAnimationId(String v)      { this.animationId = v; }
    public void setQuitMessageId(String v)    { this.quitMessageId = v; }
    public void setMessageEnabled(boolean v)  { this.messageEnabled = v; }
    public void setSoundEnabled(boolean v)    { this.soundEnabled = v; }
    public void setParticleEnabled(boolean v) { this.particleEnabled = v; }
    public void setAnimationEnabled(boolean v){ this.animationEnabled = v; }
    public void setQuitMessageEnabled(boolean v){ this.quitMessageEnabled = v; }
    public void setStreak(int v)              { this.streak = v; }
    public void setLastJoinDate(String v)     { this.lastJoinDate = v; }
    public void setFirstJoin(boolean v)       { this.firstJoin = v; }

    // ─── Toggles ──────────────────────────────────────────────────────
    public boolean toggleMessage()      { return (messageEnabled      = !messageEnabled); }
    public boolean toggleSound()        { return (soundEnabled        = !soundEnabled); }
    public boolean toggleParticle()     { return (particleEnabled     = !particleEnabled); }
    public boolean toggleAnimation()    { return (animationEnabled    = !animationEnabled); }
    public boolean toggleQuitMessage()  { return (quitMessageEnabled  = !quitMessageEnabled); }
}
