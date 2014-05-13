package org.programus.book.mobilelego.motion_rc_vehicle.rc.processors;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.ObstacleInforMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator.Processor;

/**
 * 障碍物信息操作员
 * @author programus
 *
 */
public class ObstacleInforProcessor implements Processor<ObstacleInforMessage> {
	/**
	 * 回调接口
	 */
	public static interface Callback {
		public void displayReport(ObstacleInforMessage msg);
	}
	
	private Callback mCallback;
	
	/**
	 * 设置回调接口对象
	 * @param callback 回调接口对象
	 */
	public void setReportCallback(Callback callback) {
		mCallback = callback;
	}

	@Override
	public void process(ObstacleInforMessage msg, Communicator communicator) {
		if (mCallback != null) {
			System.out.printf("Process msg: %s\n", msg.toString());
			mCallback.displayReport(msg);
		}
	}
}
