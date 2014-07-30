package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import org.programus.book.mobilelego.robopet.server.util.CommandManager;

/**
 * 保持处理未处理完的命令
 * @author programus
 *
 */
public class KeepProcessingCommand extends AbstractBehavior {
	private CommandManager cmdMgr = CommandManager.getInstance();

	@Override
	public boolean takeControl() {
		// 当有命令正在处理时，取得控制权，继续处理命令。
		return cmdMgr.hasCommandProcessing();
	}

	@Override
	public void move() {
		while (this.isControlling() && this.body.isMoving() && cmdMgr.hasCommandProcessing()) {
			Thread.yield();
		}
		this.body.stop(false);
		cmdMgr.finishProcess();
	}

}
