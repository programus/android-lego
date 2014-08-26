package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import java.io.IOException;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.server.net.Server;
import org.programus.book.mobilelego.robopet.server.util.KeyCommandContainer;

/**
 * 退出程序行为
 * @author programus
 *
 */
public class ExitProgram extends AbstractBehavior {
	private KeyCommandContainer cc;
	
	public ExitProgram(KeyCommandContainer cc) {
		this.cc = cc;
	}

	@Override
	public boolean takeControl() {
		return cc.getKeyCommand() == KeyCommandContainer.KeyCommand.Esc;
	}

	@Override
	public void move() {
		System.out.println("Exit...");
		cc.setKeyCommand(null);
		Server server = Server.getInstance();
		Communicator comm = server.getCommunicator();
		if (comm.isAvailable()) {
			// 向客户端通知自己退出
			server.getCommunicator().send(ExitSignal.getInstance());
		}
		// 关闭服务器
		server.close();
		// 停止身体行动
		this.body.stop(false);
		// 保存当前状态
		try {
			this.param.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 关闭身体连接的传感器
		this.body.close();
		// 退出程序
		System.exit(0);
	}

}
