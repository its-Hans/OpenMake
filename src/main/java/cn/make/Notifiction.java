package cn.make;

import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.api.center.Module;
import chad.phobos.api.setting.Setting;
import net.minecraft.util.text.TextComponentString;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Notifiction extends Module {
	static Notifiction noti;
	static final LinkedHashMap<String, Integer> notiMap = new LinkedHashMap<>();
	public Setting<Boolean> listenModuleToggle = rbool("ModuleToggle", true);
	public Setting<Integer> moduleRunTick = rinte("ModuleRunTick", 40, 5, 60, v -> listenModuleToggle.getValue());
	public Setting<Boolean> text = rbool("Chat", true);
	public Setting<Boolean> noMulti = rbool("removeTest", true, v -> text.getValue());

	public Notifiction() {
		super("Noti", "test", Category.CLIENT);
		noti = this;
	}
	public static Notifiction getNoti() {
		if (noti == null) noti = new Notifiction();
		return noti;
	}

	@Override
	public void onUpdate() {
	}

	public void putMap(String notiStr, Integer ticks) {
		if (this.isOn()) {
			synchronized (notiMap) {
				notiMap.put(notiStr, ticks);
			}
		} else {
			clearMap();
		}
	}

	public void clearMap() {
		notiMap.clear();
	}

	@Override
	public void onTick() {
		if (!this.isOn() || mc.world == null) {
			clearMap();
			return;
		}

		// update
		Iterator<Map.Entry<String, Integer>> it = notiMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Integer> data = it.next();
			Integer value = data.getValue();
			if (value <= 0) {
				it.remove();
				continue;
			}
			if (value >= moduleRunTick.getValue()) {
				if (text.getValue()) {
					String key = data.getKey();
					if (noMulti.getValue()) {
						mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(key), 1);
					} else {
						sendModuleMessage(key);
					}
				}
			}
			data.setValue(value-1);
		}
	}

	public void putModuleToggle(boolean enable, String moduleName) {
		String e;
		if (enable) {
			e = (ChatFormatting.GREEN +  " on");
		} else {
			e = (ChatFormatting.RED + " off");
		}
		String message = (
			(ChatFormatting.GRAY + "Module " + ChatFormatting.RESET)
				+ (ChatFormatting.DARK_GRAY + moduleName + ChatFormatting.RESET)
				+ (e + ChatFormatting.RESET)
		);
		putMap(message, this.moduleRunTick.getValue());
	}

}
