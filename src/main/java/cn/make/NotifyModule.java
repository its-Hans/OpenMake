package cn.make;

import java.util.ArrayList;

import cn.make.util.skid.ColorUtil;
import cn.make.util.skid.FadeUtils;
import cn.make.util.skid.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import chad.phobos.api.events.render.Render2DEvent;
import chad.phobos.api.center.Module;
import chad.phobos.modules.client.ClickGui;
import chad.phobos.api.setting.Setting;

public class NotifyModule
	extends Module {
	static NotifyModule noti;
	public static final ArrayList<Notifys> notifyList = new ArrayList();
	private final Setting<Integer> notifyY = this.register(new Setting<>("Y", 18, 25, 500));
	private final Setting<Integer> alpha = this.register(new Setting<>("Alpha", 155, 0, 255));
	public NotifyModule() {
		super("Notifications", "Notify toggle module", Category.CLIENT);
		noti = this;
	}


	public static NotifyModule getNoti() {
		if (noti == null) noti = new NotifyModule();
		return noti;
	}

	@Override
	public void onRender2D(Render2DEvent render2DEvent) {
		boolean bl = true;
		int n = this.renderer.scaledHeight - this.notifyY.getValue();
		int n2 = this.renderer.scaledWidth;
		int n3 = ColorUtil.toRGBA(ClickGui.getInstance().red.getValue(), ClickGui.getInstance().green.getValue(), ClickGui.getInstance().blue.getValue());
		for (Notifys notifys : notifyList) {
			if (notifys == null || notifys.first == null || notifys.firstFade == null || notifys.delayed < 1) continue;
			bl = false;
			if (notifys.delayed < 5 && !notifys.end) {
				notifys.end = true;
				notifys.endFade.reset();
			}
			n = (int)((double)n - 18.0 * notifys.yFade.easeOutQuad());
			String string = notifys.first;
			double d = notifys.delayed < 5 ? (double)n2 - (double)(this.renderer.getStringWidth(string) + 10) * (1.0 - notifys.endFade.easeOutQuad()) : (double)n2 - (double)(this.renderer.getStringWidth(string) + 10) * notifys.firstFade.easeOutQuad();
			RenderUtil.drawRectangleCorrectly((int)d, n, 10 + this.renderer.getStringWidth(string), 15, ColorUtil.toRGBA(20, 20, 20, this.alpha.getValue()));
			this.renderer.drawString(string, 5 + (int)d, 4 + n, ColorUtil.toRGBA(255, 255, 255), true);
			if (notifys.delayed < 5) {
				n = (int)((double)n + 18.0 * notifys.yFade.easeOutQuad() - 18.0 * (1.0 - notifys.endFade.easeOutQuad()));
				continue;
			}
			RenderUtil.drawRectangleCorrectly((int)d, n + 14, (10 + this.renderer.getStringWidth(string)) * (notifys.delayed - 4) / 62, 1, n3);
		}
		if (bl) {
			notifyList.clear();
		}
	}

	@Override
	public void onUpdate() {
		for (Notifys notifys : notifyList) {
			if (notifys == null || notifys.first == null || notifys.firstFade == null) continue;
			--notifys.delayed;
		}
	}

	@Override
	public void onDisable() {
		notifyList.clear();
	}

	public static class Notifys {
		public final FadeUtils firstFade = new FadeUtils(500L);
		public final FadeUtils endFade;
		public final FadeUtils yFade = new FadeUtils(500L);
		public final String first;
		public int delayed = 55;
		public boolean end;

		public Notifys(String string) {
			this.endFade = new FadeUtils(350L);
			this.first = string;
			this.firstFade.reset();
			this.yFade.reset();
			this.endFade.reset();
			this.end = false;
		}
	}
	public void pushEnable(String moduleName) {
		pushString(
			ChatFormatting.AQUA
				+ moduleName
				+ ChatFormatting.GREEN + " on"
		);
	}
	public void pushDisable(String moduleName) {
		pushString(
			ChatFormatting.AQUA
				+ moduleName
				+ ChatFormatting.RED + " off"
		);
	}
	public void pushString(String string) {
		notifyList.add(new Notifys(string));
	}
}

