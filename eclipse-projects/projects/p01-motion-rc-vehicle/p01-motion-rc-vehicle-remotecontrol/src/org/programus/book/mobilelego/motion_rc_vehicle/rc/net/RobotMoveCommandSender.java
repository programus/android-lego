package org.programus.book.mobilelego.motion_rc_vehicle.rc.net;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;

/**
 * 机器人移动命令发送器。通过此发送器发送命令，可以保证命令发送不会过于频繁，从而防止由于机器人处理能力导致的命令积压。
 * @author programus
 *
 */
public class RobotMoveCommandSender {
	/** 最小命令间隔时间，单位：毫秒 */
	private static final int SEND_ITERVAL = 200;
	
	private Communicator mComm;
	
	/** 等待发送的命令 */
	private RobotMoveCommand mWaitCommand;
	/** 上次发送的命令 */
	private RobotMoveCommand mPrevCommand;
	/** 同步锁 */
	private Lock mLock = new ReentrantLock();
	/** 同步条件 */
	private Condition mHasCommand = mLock.newCondition();
	
	public RobotMoveCommandSender() {
		startCommandSendThread();
	}
	
	public synchronized void init(Communicator comm) {
		this.mComm = comm;
		this.requestSendMove(null);
	}
	
	private void requestSendMove(RobotMoveCommand cmd) {
        mLock.lock();
        try {
            mWaitCommand = cmd;
            mHasCommand.signal();
        } finally {
            mLock.unlock();
        }
	}
	
	/**
	 * 请求发送机器人移动命令。对移动类命令，若有命令正在等待，将以此命令覆盖等待命令。停止类命令将立即发出。
	 * @param cmd
	 */
	public void requestSend(RobotMoveCommand cmd) {
		switch (cmd.getCommand()) {
		case Forward:
		case Backward:
			requestSendMove(cmd);
            break;
		case Float:
		case Stop:
			this.sendStayCommand(cmd);
			break;
		}
	}
	
	private void startCommandSendThread() {
		// 创建命令发送线程
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
                RobotMoveCommand cmd = null;
				while (true) {
					try {
						mLock.lockInterruptibly();
						try {
							// 没有命令等待时，线程暂停
						    while (mWaitCommand == null) {
						        mHasCommand.await();
						    }
						    // 有命令等待时，取出命令
						    cmd = mWaitCommand;
						    mWaitCommand = null;
						} finally {
							mLock.unlock();
						}
						if (!isDuplicatedCommand(cmd)) {
							// 若此命令与前次命令不同，则发送命令
                            sendMoveCommand(cmd);
                            mPrevCommand = cmd;
						}
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}, "Command send thread");
		t.setDaemon(true);
		t.start();
	}
	
	/**
	 * 判断命令是否与前次相同
	 * @param cmd 新的命令
	 * @return 与前次命令相同时返回<code>true</code>
	 */
	private boolean isDuplicatedCommand(RobotMoveCommand cmd) {
		boolean result = cmd == mPrevCommand;
		if (!result) {
			if (cmd != null && mPrevCommand != null) {
                result = mPrevCommand.getCommand().equals(cmd.getCommand());
                switch(cmd.getCommand()) {
                case Forward:
                case Backward:
                	result = result && mPrevCommand.getSpeed() == cmd.getSpeed() && mPrevCommand.getRotation() == (cmd.getRotation());
                	break;
				default:
					break;
                }
			} else {
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * 发送关闭引擎类命令，包含停止（stop）和滑行（float）。
	 * @param cmd 关闭引擎类命令
	 */
	private synchronized void sendStayCommand(RobotMoveCommand cmd) {
		if (mComm != null) {
            mComm.send(cmd);
		}
	}
	
	/**
	 * 发送移动类命令，包括前进（forward）和后退（backward）。
	 * @param cmd 移动类命令
	 * @throws InterruptedException 线程被打断时抛出
	 */
	private synchronized void sendMoveCommand(RobotMoveCommand cmd) throws InterruptedException {
		if (mComm != null) {
            mComm.send(cmd);
            // 为确保机器人的处理时间，暂停一段时间
            Thread.sleep(SEND_ITERVAL);
		}
	}
}
