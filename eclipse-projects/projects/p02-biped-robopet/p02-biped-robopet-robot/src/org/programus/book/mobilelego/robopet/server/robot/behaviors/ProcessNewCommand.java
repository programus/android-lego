package org.programus.book.mobilelego.robopet.server.robot.behaviors;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lejos.hardware.Sound;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.server.net.Server;
import org.programus.book.mobilelego.robopet.server.robot.RobotBody;
import org.programus.book.mobilelego.robopet.server.util.CommandManager;

/**
 * 处理新来的命令的行为
 * @author programus
 *
 */
public class ProcessNewCommand extends AbstractBehavior {
	/** 系统的关机命令 */
	private static final String SHUTDOWN_CMD = "init 0";
	private CommandManager cmdMgr = CommandManager.getInstance();

	@Override
	public boolean takeControl() {
		boolean result = false;
		if (cmdMgr.hasCommandWaiting()) {
			// 当有命令传来，在等待时。
			switch (this.param.getMood()) {
			case Crazy:
			case Angry:
			case Sad:
			{
				// 疯狂、生气、悲伤的时候
				PetCommand cmd = cmdMgr.peekCommand();
				// 以下命令不论什么情绪都会生效，为超级命令
				List<PetCommand.Command> superCommands = Arrays.asList(
						PetCommand.Command.Calm, 
						PetCommand.Command.Exit,
						PetCommand.Command.Shutdown
						); 
				// 只有命令为以上超级命令时才能取得控制权
				result = superCommands.contains(cmd.getCommand());
			}
			default:
				// 其他情绪下，直接取得控制权
				result = true;
			}
		}
		return result;
	}

	@Override
	public void move() {
		// 取出等待的命令进行处理
		PetCommand cmd = cmdMgr.getCommandToProcess();
		if (cmd != null) {
			System.out.println("Process command: " + cmd);
			// 根据命令种类执行不同的处理
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
			case TurnLeft:
				this.turn(RobotBody.Speed.WalkSpeed.value, -cmd.getValue());
				break;
			case TurnRight:
				this.turn(RobotBody.Speed.WalkSpeed.value, cmd.getValue());
				break;
			case Stop:
				this.stop();
				break;
			}
		}
	}

	private void stop() {
		// 停止行动
		this.body.stop(false);
		// 当没有新的命令传来，此命令仍在处理时，不断循环，保持停止
		while (this.isControlling() && cmdMgr.hasCommandProcessing() && !cmdMgr.hasCommandWaiting()) {
			PetCommand cmdProcessing = cmdMgr.peekProcessingCommand();
			if (cmdProcessing == null || cmdProcessing.getCommand() != PetCommand.Command.Stop) {
				break;
			}
			Thread.yield();
		}
		cmdMgr.finishProcess();
	}

	private void turn(int speed, int angle) {
		this.body.turn(speed, angle, true);
		while (this.isControlling() && this.body.isMoving() && !cmdMgr.hasCommandWaiting()) {
			Thread.yield();
		}
		cmdMgr.finishProcess();
	}

	private void forward(int speed, int steps) {
		this.body.forward(speed, steps, true);
		while (this.isControlling() && this.body.isMoving() && !cmdMgr.hasCommandWaiting()) {
			Thread.yield();
		}
		cmdMgr.finishProcess();
	}
	
	private void backward(int speed, int steps) {
		this.body.backward(speed, steps, true);
		while (this.isControlling() && this.body.isMoving() && !cmdMgr.hasCommandWaiting()) {
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
	
	private void closeCommunication() {
		Server server = Server.getInstance();
		Communicator comm = server.getCommunicator();
		if (server.isStarted() && comm != null) {
			comm.send(ExitSignal.getInstance());
		}
		server.close();
	}
	
	private void exit() {
		this.saveAndClose();
		this.closeCommunication();
		Sound.buzz();
		System.exit(0);
		cmdMgr.finishProcess();
	}
	
	private void shutdown() {
		this.saveAndClose();
		this.closeCommunication();
		try {
			Runtime.getRuntime().exec(SHUTDOWN_CMD);
		} catch (IOException e) {
			// 与leJOS源代码一样，忽略
		}
	}
}
