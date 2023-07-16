package cn.make.verify;

import cn.make.tweaksClient;
import chad.phobos.Client;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Verify {
	final String raw = "https://gitee.com/itsHans/hwid/raw/master/make";
	final String hwid;
	final List<String> hwids;
	public Verify() {
		hwid = gen();
		hwids = get();
	}
	public String gen() {
		return DigestUtils.sha256Hex(
			DigestUtils.sha256Hex(
				System.getenv("os")
					+ "f"
					+ "_k"
					+ System.getProperty("os.name")
					+ System.getProperty("os.arch")
					+ System.getProperty("user.name")
					+ System.getenv("PROCESSOR_LEVEL")
					+ System.getenv("PROCESSOR_REVISION")
					+ System.getenv("PROCESSOR_IDENTIFIER")
					+ System.getenv("PROCESSOR_ARCHITEW6432")
					+ "114514"
			)
		);
	}
	public List<String> get() {
		List<String> hwidList = new ArrayList<>();
		try {
			final URL url = new URL(raw);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
			String hwid;
			while ((hwid = bufferedReader.readLine()) != null) {
				hwidList.add(hwid);
			}
		} catch (Exception ignored) {}
		return hwidList;
	}
	public boolean isHWID() {
		if (tweaksClient.noHWIDCheck) {
			return true;
		} else {
			return hwids.contains(hwid);
		}
	}
	public void check() {
		Client.LOGGER.info(hwid);
		Client.LOGGER.info("HWID LIST: ");
		for (String h: hwids) {
			Client.LOGGER.info(" " + h);
		}
		if (isHWID()) {
			Client.LOGGER.info("verify succes");
		} else {
			Client.LOGGER.info("verify failed");
			JOptionPane.showMessageDialog(null, "verify failed");
			fuck();
		}
	}
	public void fuck() {
		//Client.LOGGER.fatal(((Object) null).getClass());
		Client.LOGGER.fatal("skip dofuck");
	}
}
