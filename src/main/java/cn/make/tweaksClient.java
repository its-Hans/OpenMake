package cn.make;

import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.Client;
import chad.phobos.api.events.player.DeathEvent;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import chad.phobos.api.utils.TextUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class tweaksClient extends Module {
	static tweaksClient INSTANCE;
	public Map<EntityPlayer, Vec3d> deathPoints = new HashMap<>();
	public Setting<String> clientName = register(new Setting("ClientName", clientname));
	public Setting<String> cmLeft = register(new Setting("cmLeft", MessageLeft));
	public Setting<String> cmRight = register(new Setting("cmRight", MessageRight));
	public Setting<ChatFormatting> brColor = register(new Setting("BracketColor", ChatFormatting.WHITE));
	public Setting<ChatFormatting> cmColor = register(new Setting("MessageColor", ChatFormatting.AQUA));
	public Setting<String> clientPrefix = register(new Setting("ClientPrefix", defaultPrefix));
	public Setting<String> clientTitle = register(new Setting("ClientTitle", title));
	public Setting<Boolean> autoSave = register(new Setting("AutoSaving", true));
	public Setting<Boolean> deathBackup = rbool("DeathBackup", true);
	public tweaksClient() {
		super("clientSetting", "set", Category.CLIENT, true, false, true);
		INSTANCE = this;
	}
	public static final String clientid = "makecc";
	public static final String clientname = "Make";
	public static final String MessageLeft = "";
	public static final String MessageRight = "Public";
	public static final String clientversion = "0";
	public static final String simplecfgpath = "MakeCat";
	public static final String defaultPrefix = "-";
	public static final String title = ("MakePublic"); //if set "no" will cancel load title
	public static final boolean noHWIDCheck = false;

	@Override
	public void onEnable() {
		setClient();
		this.disable();
	}
	@SubscribeEvent
	public void onDeath(DeathEvent e) {
		if (!deathBackup.getValue()) return;
		if (e.player == mc.player) {
			//sendModuleMessage("u'll death on :" + BlockChecker.simpleXYZString(e.player.getPosition()));
		}
		deathPoints.remove(e.player);
		deathPoints.put(e.player, e.player.getPositionVector());
	}
	@Override
	public void onLoad() {
		setClient();
	}
	public static tweaksClient getInstance() {
		if (INSTANCE == null) INSTANCE = new tweaksClient();
		return INSTANCE;
	}

	//dont edit
	public static final String configpath = (simplecfgpath + "/");
	static String clientMessageLeft() {
		return getInstance().brColor.getValue() + getInstance().cmLeft.getValue() + TextUtil.RESET;
	}
	static String clientMessageRight() {
		return  getInstance().brColor.getValue() + getInstance().cmRight.getValue() + TextUtil.RESET;
	}
	static String clentMessage() {
		return getInstance().cmColor.getValue() + getInstance().clientName.getValue() + TextUtil.RESET;
	}

	public static boolean setClient() {
		boolean name = setClientName();
		boolean prefix = setClientPrefix();
		boolean title = setTitle();
		return name && prefix && title;
	}

	public static boolean setTitle() {
		if (!isManagersLoaded()) return false;
		String ti = getInstance().clientTitle.getValue();
		String getti = Display.getTitle();
		if (!Objects.equals(getti, ti)) setTitle(ti);
		return Objects.equals(getti, ti);
	}
	public static boolean setClientName() {
		if (!isManagersLoaded()) return false;
		String cm = getCustomClientMessage();
		String getcm = Client.commandManager.getClientMessage();
		if (!Objects.equals(getcm, cm)) Client.commandManager.setClientMessage(cm);
		return Objects.equals(getcm, cm);
	}
	public static boolean setClientPrefix() {
		if (!isManagersLoaded()) return false;
		String cp = tweaksClient.getInstance().clientPrefix.getValue();
		String getcp = Client.commandManager.getPrefix();
		if (!Objects.equals(getcp, cp)) Client.commandManager.setPrefix(cp);
		return Objects.equals(getcp, cp);
	}

	public static boolean isManagersLoaded() {
		if (Client.moduleManager == null || Client.commandManager == null) {
			Client.LOGGER.warn("cannot link managers");
			return false;
		} else return true;
	}

	static void setTitle(String titleName) {
		if (!Objects.equals(titleName, "no")) {
			Display.setTitle(titleName);
		}
	}

	static String getCustomClientMessage() {
		return (clientMessageLeft() + clentMessage() + clientMessageRight());
	}

	public static TextUtil.Color toTextUtilColor(ChatFormatting color) {
		if (color == ChatFormatting.BLACK) return TextUtil.Color.BLACK;
		if (color == ChatFormatting.DARK_BLUE) return TextUtil.Color.DARK_BLUE;
		if (color == ChatFormatting.DARK_GREEN) return TextUtil.Color.DARK_GREEN;
		if (color == ChatFormatting.DARK_AQUA) return TextUtil.Color.DARK_AQUA;
		if (color == ChatFormatting.DARK_RED) return TextUtil.Color.DARK_RED;
		if (color == ChatFormatting.DARK_PURPLE) return TextUtil.Color.DARK_PURPLE;
		if (color == ChatFormatting.GOLD) return TextUtil.Color.GOLD;
		if (color == ChatFormatting.GRAY) return TextUtil.Color.GRAY;
		if (color == ChatFormatting.DARK_GRAY) return TextUtil.Color.DARK_GRAY;
		if (color == ChatFormatting.BLUE) return TextUtil.Color.BLUE;
		if (color == ChatFormatting.GREEN) return TextUtil.Color.GREEN;
		if (color == ChatFormatting.AQUA) return TextUtil.Color.AQUA;
		if (color == ChatFormatting.RED) return TextUtil.Color.RED;
		if (color == ChatFormatting.LIGHT_PURPLE) return TextUtil.Color.LIGHT_PURPLE;
		if (color == ChatFormatting.YELLOW) return TextUtil.Color.YELLOW;
		if (color == ChatFormatting.WHITE) return TextUtil.Color.WHITE;

		return TextUtil.Color.NONE;
	}
}
