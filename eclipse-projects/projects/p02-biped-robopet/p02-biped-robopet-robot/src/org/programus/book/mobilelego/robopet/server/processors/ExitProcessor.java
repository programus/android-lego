package org.programus.book.mobilelego.robopet.server.processors;

import lejos.hardware.Sound;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.comm.util.Communicator.Processor;
import org.programus.book.mobilelego.robopet.server.net.Server;
import org.programus.book.mobilelego.robopet.server.util.CommandManager;

/**
 * 连接断开信号处理器。
 * 接收到连接断开信号时，清除所有网络传来的命令，并重新启动服务器进行监听。
 * @author programus
 *
 */
public class ExitProcessor implements Processor<ExitSignal>{
	private CommandManager cmdMgr = CommandManager.getInstance();

	@Override
	public void process(ExitSignal msg, Communicator communicator) {
		cmdMgr.clearCommand();
		final Server server = Server.getInstance();
		server.close();
		Sound.buzz();
		// 在新线程中启动服务器，以防止阻塞处理
		new Thread(new Runnable() {
			@Override
			public void run() {
				Sound.beepSequenceUp();
				server.start();
			}
		}).start();
	}
}
