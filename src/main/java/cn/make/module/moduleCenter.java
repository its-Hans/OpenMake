package cn.make.module;

import chad.phobos.api.center.Module;
import cn.make.Notifiction;
import cn.make.NotifyModule;
import cn.make.Targets;
import cn.make.module.combat.*;
import cn.make.module.misc.*;
import cn.make.module.movement.*;
import cn.make.module.player.*;
import cn.make.module.render.*;
import cn.make.tweaksClient;
import cn.make.verify.verifyModule;
import cn.make.xinbb.xinBowBomb;

import java.util.Arrays;
import java.util.List;

public class moduleCenter {
	public static List<Module> getModules() {
		return Arrays.asList(
			new tweaksClient(),
			new verifyModule(),
			new CatsPush(),
			new AntiPushPlus(),
			new RebirthPush(),
			new RebirthPullCrystal(),
			new SexyBurrow(),
			new BowGod(),
			new BowMcBomb(),
			new ChatSuffix(),
			new insanebow(),
			new kit(),
			new RebirthMine(),
			new Cat3Push(),
			new LoverMending(),
			new Animations(),
			new login(),
			new AutoBurrow(),
			new MakePacketMiner(),
			new ThunderMine(),
			new fogColor(),
			new PlaceRender(),
			new BreakESPOld(),
			new AutoCityBypass(),
			new ProgressRender(),
			new KeyPushMake(),
			//new BreakHighLight(),
			//new SilentBow(),
			new OffGroundSpoof(),
			new CivSelect(),
			//new RebirthBlockRenderTest(),
			new AutoPlace(),
			new InstantMine(),
			new SuperBow(),
			new Shake(),
			//new MinerESP(),
			//new PistonCrystal(),
			//new SimpleCev(),
			new Ghost(),
			new FarRender(),
			new ModuleTimer(),
			new Notifiction(),
			new NotifyModule(),
			new Targets(),
			new Blink(),
			new CatBurrow(),
			//new PacketExp(),
			//new BowAim(),
			new HoleSnap(),
			//new AntiPushMake(),
			new LongJump(),
			new SmartBowBomb(),
			new TPCoordLog(),
			new RebirthBurrow(),
			new AutoCenter(),
			new StrafeA(),
			new SpeedA(),
			new StrafeR(),
			new SpeedR(),
			new StrafeD(),
			new StrafeM(),
			new AutoCNM(),
			new XpThrower(),
			new xinBowBomb(),
			new PBowBomb(),
			new BreakESP()
		);
	}
}
