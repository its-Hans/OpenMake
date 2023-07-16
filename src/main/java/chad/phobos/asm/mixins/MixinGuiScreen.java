package chad.phobos.asm.mixins;

import chad.phobos.modules.client.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {GuiScreen.class})
public class MixinGuiScreen extends Gui {

	@Inject(method = "drawWorldBackground(I)V", at = @At("HEAD"), cancellable = true)
	private void drawWorldBackgroundHook(int tint, CallbackInfo info) {

		if (Minecraft.getMinecraft().world != null && ClickGui.getInstance().cleanGui.getValue()) {
			if (Minecraft.getMinecraft().currentScreen == ClickGui.getInstance().nowGUI())
				info.cancel();
		}
	}
}

