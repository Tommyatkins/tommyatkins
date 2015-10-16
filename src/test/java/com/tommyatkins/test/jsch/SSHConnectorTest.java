package com.tommyatkins.test.jsch;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHConnectorTest {

	public static void main(String[] args) throws Exception {
		test();
	}

	public static void test() throws Exception {
		JSch jsch = new JSch();
		// jsch.addIdentity("Identity");
		Session session = jsch.getSession("yangli", "192.168.7.180", 22);
		session.setUserInfo(new UserInfo() {
			@Override
			public void showMessage(String message) {
				System.out.println("showMessage");
			}

			@Override
			public boolean promptYesNo(String message) {
				System.out.println("promptYesNo");
				return true;
			}

			@Override
			public boolean promptPassword(String message) {
				System.out.println("promptPassword");
				return true;
			}

			@Override
			public boolean promptPassphrase(String message) {
				System.out.println("promptPassphrase");
				return false;
			}

			@Override
			public String getPassword() {
				System.out.println("getPassword");
				return "123456";
			}

			@Override
			public String getPassphrase() {
				System.out.println("getPassphrase");
				return null;
			}
		});
		session.connect(5000);
		System.out.println("session ok");
		ChannelShell channelShell = (ChannelShell) session.openChannel("shell");

		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);
		channelShell.setInputStream(pis);
		channelShell.setOutputStream(System.out);
		channelShell.connect(3000);
		System.out.println("shell ok");

		Scanner scan = new Scanner(System.in);
		String cmd = null;
		while ((cmd = scan.nextLine()) != null) {
			pos.write(String.format("%s\n", cmd).getBytes("utf-8"));
			if (cmd.equals("exit")) {
				Thread.sleep(1000);
				break;
			}
		}
		scan.close();
		pos.close();
		channelShell.disconnect();
		System.out.println("shell disconnect");
		session.disconnect();
		System.out.println("session disconnect");

	}
}
