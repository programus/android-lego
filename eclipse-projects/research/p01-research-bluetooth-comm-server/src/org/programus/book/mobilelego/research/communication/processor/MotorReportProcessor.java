package org.programus.book.mobilelego.research.communication.processor;

import java.util.Timer;
import java.util.TimerTask;

import lejos.hardware.motor.BaseRegulatedMotor;

import org.programus.book.mobilelego.research.communication.protocol.MotorReportCommand;
import org.programus.book.mobilelego.research.communication.protocol.MotorReportMessage;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

/**
 * 处理开启/关闭报告命令的操作员类
 * @author programus
 *
 */
public class MotorReportProcessor implements Processor<MotorReportCommand> {
	/** 所操作的马达 */
	private BaseRegulatedMotor motor;
	
	/** 用以获取马达参数的定时器 */
	private Timer timer = new Timer("Reporting Timer", true);
	/** 用以获取马达参数的定时任务 */
	private TimerTask task = null;
	
	/**
	 * 构造函数
	 * @param motor 所操作的马达
	 */
	public MotorReportProcessor(BaseRegulatedMotor motor) {
		this.motor = motor;
	}

	/**
	 * 发送马达数据报告，当报告内容没有变化时，不予发送。
	 * @param communicator 帮助发送消息的通讯员
	 * @param prevMsg 前次报告的内容
	 * @return 本次报告的内容
	 */
	private MotorReportMessage sendReport(Communicator communicator, MotorReportMessage prevMsg) {
		// 假定报告没有变化，将前次报告赋值给本次报告
		MotorReportMessage msg = prevMsg;
		// 获取转速和转过的角度
		int speed = motor.getRotationSpeed();
		int tachoCount = motor.getTachoCount();
		// 检查数值是否有变化（当报告为null时，表示这是第一次报告）
		if (msg == null || speed != prevMsg.getSpeed() || tachoCount != prevMsg.getTachoCount()) {
			// 使用新的数值创建新报告
			msg = new MotorReportMessage();
			msg.setSpeed(speed);
			msg.setTachoCount(tachoCount);
			// 发送报告
            communicator.send(msg);
		}
		
		// 返回本次报告内容
		return msg;
	}
	
	/**
	 * 启动定时报告任务
	 * @param communicator 帮助发送报告的通讯员
	 */
	private void startReportTask(final Communicator communicator) {
		// 因为定时器启动任务，会等待一个循环周期时间后第一次运行，
		// 所以在此立即发送一次报告
        final MotorReportMessage msg = sendReport(communicator, null);
		if (task == null) {
			// 任务不存在，意味着任务未启动，创建新任务
			// 报告定时任务的匿名类
            task = new TimerTask() {
            	// 用以存储前次报告的变量，初始值为启动前发送的报告
                MotorReportMessage prevMsg = msg;
                @Override
                public void run() {
                	// 发送报告，并将本次报告内容存为下次报告时的前次报告
                    prevMsg = sendReport(communicator, prevMsg);
                }
            };
            
            // 启动定时器，执行间隔100毫秒
            timer.schedule(task, 0, 100);
		}
	}
	
	/**
	 * 停止定时报告任务
	 */
	private void stopReportTask() {
		if (task != null) {
			// 当任务存在时，意味着定时器正在运行
			// 取消任务
            task.cancel();
            // 将任务置空
            task = null;
            // 刷新定时器
            timer.purge();
		}
	}

	@Override
	public void process(MotorReportCommand msg, Communicator communicator) {
		if (msg.isReportOn()) {
			this.startReportTask(communicator);
		} else {
			this.stopReportTask();
		}
	}
}
