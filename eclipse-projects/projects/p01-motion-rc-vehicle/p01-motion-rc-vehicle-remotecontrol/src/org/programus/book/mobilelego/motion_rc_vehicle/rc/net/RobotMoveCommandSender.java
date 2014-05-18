package org.programus.book.mobilelego.motion_rc_vehicle.rc.net;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotMoveCommand;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;

public class RobotMoveCommandSender {
	private static final int SEND_ITERVAL = 100;
	
	private Communicator mComm;
	
	private RobotMoveCommand mWaitCommand;
	private RobotMoveCommand mPrevCommand;
	private Lock mLock = new ReentrantLock();
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
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
                RobotMoveCommand cmd = null;
				while (true) {
					try {
						mLock.lockInterruptibly();
						try {
						    while (mWaitCommand == null) {
						        mHasCommand.await();
						    }
						    cmd = mWaitCommand;
						    mWaitCommand = null;
						} finally {
							mLock.unlock();
						}
						if (!isDuplicatedCommand(cmd)) {
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
	
	private synchronized void sendStayCommand(RobotMoveCommand cmd) {
		if (mComm != null) {
            mComm.send(cmd);
		}
	}
	
	private synchronized void sendMoveCommand(RobotMoveCommand cmd) throws InterruptedException {
		if (mComm != null) {
            mComm.send(cmd);
            Thread.sleep(SEND_ITERVAL);
		}
	}
}
