package chad.phobos;

import chad.phobos.api.managers.*;
import cn.make.manager.NewRotationManager;
import cn.make.manager.TimerManager;
import cn.make.tweaksClient;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = tweaksClient.clientid, name = tweaksClient.clientname, version = tweaksClient.clientversion)
public class Client {
    public static final String MODID = tweaksClient.clientid;
    public static final String MODNAME = tweaksClient.clientname;
    public static final String MODVER = tweaksClient.clientversion;
    public static final String FULLMODEVER = MODNAME + " " + MODVER;
    public static String CFGPATH = tweaksClient.configpath;
    public static String CFGFOLDER = tweaksClient.simplecfgpath;
    public static final Logger LOGGER = LogManager.getLogger(MODNAME);
    public static CommandManager commandManager;
    public static FriendManager friendManager;
    public static ModuleManager moduleManager;
    public static PacketManager packetManager;
    public static ColorManager colorManager;
    public static HoleManager holeManager;
    public static InventoryManager inventoryManager;
    public static PotionManager potionManager;
    public static RotationManager rotationManager;
    public static NewRotationManager newRotationManager;
    public static PositionManager positionManager;
    public static SpeedManager speedManager;
    public static ReloadManager reloadManager;
    public static FileManager fileManager;
    public static ConfigManager configManager;
    public static ServerManager serverManager;
    public static EventManager eventManager;
    public static TextManager textManager;
    public static TimerManager timerManager;
    @Mod.Instance
    public static Client INSTANCE;
    private static boolean unloaded;

    static {
        unloaded = false;
    }

    public static void load() {
        LOGGER.info("\n\nLoading " + FULLMODEVER);
        unloaded = false;
        if (reloadManager != null) {
            reloadManager.unload();
            reloadManager = null;
        }
        textManager = new TextManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();
        moduleManager = new ModuleManager();
        rotationManager = new RotationManager();
        newRotationManager = new NewRotationManager();
        packetManager = new PacketManager();
        eventManager = new EventManager();
        speedManager = new SpeedManager();
        potionManager = new PotionManager();
        inventoryManager = new InventoryManager();
        serverManager = new ServerManager();
        fileManager = new FileManager();
        colorManager = new ColorManager();
        positionManager = new PositionManager();
        configManager = new ConfigManager();
        holeManager = new HoleManager();
        timerManager = new TimerManager();
        LOGGER.info("Managers loaded.");
        moduleManager.init();
        LOGGER.info("Modules loaded.");
        configManager.init();
        eventManager.init();
        LOGGER.info("EventManager loaded.");
        textManager.init(true);
        moduleManager.onLoad();
        LOGGER.info(MODNAME + " successfully loaded!\n");
    }

    public static void unload(boolean unload) {
        LOGGER.info("\n\nUnloading " + FULLMODEVER);
        if (unload) {
            reloadManager = new ReloadManager();
            reloadManager.init(commandManager != null ? commandManager.getPrefix() : tweaksClient.defaultPrefix);
        }
        Client.onUnload();
        eventManager = null;
        friendManager = null;
        speedManager = null;
        holeManager = null;
        positionManager = null;
        rotationManager = null;
        configManager = null;
        commandManager = null;
        colorManager = null;
        serverManager = null;
        fileManager = null;
        potionManager = null;
        inventoryManager = null;
        moduleManager = null;
        textManager = null;
        newRotationManager = null;
        timerManager = null;
        LOGGER.info(MODNAME + " unloaded!\n");
    }

    public static void reload() {
        Client.unload(false);
        Client.load();
    }

    public static void onUnload() {
        if (!unloaded) {
            eventManager.onUnload();
            moduleManager.onUnload();
            configManager.saveConfig(Client.configManager.config.replaceFirst(tweaksClient.configpath, ""));
            moduleManager.onUnloadPost();
            unloaded = true;
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("I am gona gas you kike - Alpha432");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Client.load();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LOGGER.info("trying refill clientSettings");
        if (tweaksClient.setClient()) {
            LOGGER.info("seted");
        } else LOGGER.warn("cannot set");
    }
}

