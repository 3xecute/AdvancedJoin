package dev.execute.advancedjoin;

import dev.execute.advancedjoin.command.AJoinCommand;
import dev.execute.advancedjoin.gui.GuiManager;
import dev.execute.advancedjoin.listener.GuiListener;
import dev.execute.advancedjoin.listener.JoinListener;
import dev.execute.advancedjoin.manager.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AdvancedJoin extends JavaPlugin {

    private static AdvancedJoin instance;

    private ConfigManager      configManager;
    private LangManager        langManager;
    private DatabaseManager    databaseManager;
    private MessageManager     messageManager;
    private AnimationManager   animationManager;
    private SoundManager       soundManager;
    private ParticleManager    particleManager;
    private VipManager         vipManager;
    private QuitManager        quitManager;
    private PlaceholderManager placeholderManager;
    private CooldownManager    cooldownManager;
    private WelcomeManager     welcomeManager;
    private GuiManager         guiManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("╔═══════════════════════════╗");
        getLogger().info("║      AdvancedJoin v1.0     ║");
        getLogger().info("║       by execute           ║");
        getLogger().info("╚═══════════════════════════╝");

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        configManager      = new ConfigManager(this);      configManager.load();
        langManager        = new LangManager(this);        langManager.load();
        databaseManager    = new DatabaseManager(this);    databaseManager.init();
        placeholderManager = new PlaceholderManager(this); placeholderManager.init();
        messageManager     = new MessageManager(this);
        animationManager   = new AnimationManager(this);
        soundManager       = new SoundManager(this);
        particleManager    = new ParticleManager(this);
        vipManager         = new VipManager(this);
        quitManager        = new QuitManager(this);
        cooldownManager    = new CooldownManager(this);
        welcomeManager     = new WelcomeManager(this);
        guiManager         = new GuiManager(this);

        AJoinCommand ajoinCmd = new AJoinCommand(this);
        Objects.requireNonNull(getCommand("ajoin")).setExecutor(ajoinCmd);
        Objects.requireNonNull(getCommand("ajoin")).setTabCompleter(ajoinCmd);

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("Enabled successfully. Language: "
                + langManager.getCurrentLang().toUpperCase());
    }

    @Override
    public void onDisable() {
        if (animationManager != null) animationManager.cancelAll();
        if (databaseManager  != null) { databaseManager.saveAll(); databaseManager.close(); }
        getLogger().info("AdvancedJoin disabled. All data saved.");
    }

    public static AdvancedJoin getInstance()          { return instance; }
    public ConfigManager      getConfigManager()       { return configManager; }
    public LangManager        getLangManager()         { return langManager; }
    public DatabaseManager    getDatabaseManager()     { return databaseManager; }
    public MessageManager     getMessageManager()      { return messageManager; }
    public AnimationManager   getAnimationManager()    { return animationManager; }
    public SoundManager       getSoundManager()        { return soundManager; }
    public ParticleManager    getParticleManager()     { return particleManager; }
    public VipManager         getVipManager()          { return vipManager; }
    public QuitManager        getQuitManager()         { return quitManager; }
    public PlaceholderManager getPlaceholderManager()  { return placeholderManager; }
    public CooldownManager    getCooldownManager()     { return cooldownManager; }
    public WelcomeManager     getWelcomeManager()      { return welcomeManager; }
    public GuiManager         getGuiManager()          { return guiManager; }
}