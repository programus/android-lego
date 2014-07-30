package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import java.io.IOException;

import lejos.hardware.Sound;

import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;
import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.util.CommandManager;

/**
 * 处理新来的命令的行为
 * @author programus
 *
 */
public class ProcessNewCommand extends AbstractBehavior {
	private static final String SHUTDOWN_CMD = "init 0";
	private CommandManager cmdMgr = CommandManager.getInstance();

	@Override
	public boolean takeControl() {
		if (cmdMgr.hasCommandWaiting()) {
			// 当有命令传来，在等待时。
			switch (this.param.getMood()) {
			case Crazy:
			case Angry:
			case Sad:
			{
				// 疯狂、生气、悲伤的时候
				PetCommand cmd = cmdMgr.peekCommand();
				// 只有命令为安静才能取得控制权
				return cmd.getCommand() == PetCommand.Command.Calm;
			}
			default:
				// 其他情绪下，直接取得控制权
				return true;
			}
		}
		return false;
	}

	@Override
	public void move() {
		PetCommand cmd = cmdMgr.getCommandToProcess();
		if (cmd != null) {
			switch (cmd.getCommand()) {
			case Calm:
				this.calm();
				break;
			case Exit:
				this.exit();
				break;
			case Shutdown:
				this.shutdown();
				break;
			case Forward:
				this.forward(RobotBody.Speed.WalkSpeed.value, cmd.getValue());
				break;
			case Backward:
				this.backward(RobotBody.Speed.WalkSpeed.value, cmd.getValue());
				break;
			case Turn:
				this.turn(RobotBody.Speed.WalkSpeed.value, cmd.getValue());
				break;
			case Stop:
				this.stop();
				break;
			}
		}
	}

	private void stop() {
		this.body.stop(false);
		cmdMgr.finishProcess();
	}

	private void turn(int speed, int angle) {
		this.body.turn(speed, angle, true);
		while (this.isControlling() && this.body.isMoving()) {
			Thread.yield();
		}
		cmdMgr.finishProcess();
	}

	private void forward(int speed, int steps) {
		this.body.forward(speed, steps, true);
		while (this.isControlling() && this.body.isMoving()) {
			Thread.yield();
		}
		cmdMgr.finishProcess();
	}
	
	private void backward(int speed, int steps) {
		this.body.backward(speed, steps, true);
		while (this.isControlling() && this.body.isMoving()) {
			Thread.yield();
		}
		cmdMgr.finishProcess();
	}

	private void calm() {
		this.param.calmDown();
		this.body.presentMood();
		cmdMgr.finishProcess();
	}
	
	private void saveAndClose() {
		try {
			this.param.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.body.close();
	}
	
	private void exit() {
		this.saveAndClose();
		Sound.buzz();
		System.exit(0);
		cmdMgr.finishProcess();
	}
	
	private void shutdown() {
		this.saveAndClose();
		try {
			Runtime.getRuntime().exec(SHUTDOWN_CMD);
		} catch (IOException e) {
			// 与leJOS源代码一样，忽略
		}
	}
}
