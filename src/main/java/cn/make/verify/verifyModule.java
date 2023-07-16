package cn.make.verify;

import chad.phobos.api.center.Module;

public class verifyModule extends Module
{
    public verifyModule() {
        super("verify", "rwar", Category.MISC);
    }
	Verify verify = new Verify();

	@Override
	public void onEnable() {
		sendModuleMessage(verify.hwid);
		this.disable();
	}
	@Override
	public void onLoad() {
		verify.check();
	}
}