package org.programus.book.mobilelego.motion_rc_vehicle.rc.processors;

import org.programus.book.mobilelego.motion_rc_vehicle.comm.protocol.RobotReportMessage;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator;
import org.programus.book.mobilelego.motion_rc_vehicle.comm.util.Communicator.Processor;

/**
 * 机器人报告数据操作员
 * @author programus
 *
 */
public class RobotReportProcessor implements Processor<RobotReportMessage> {
	/**
	 * 报告回调接口
	 */
	public static interface ReportCallback {
		public void displayReport(RobotReportMessage msg);
	}
	
	private ReportCallback mCallback;
	
	/**
	 * 设置回调接口对象
	 * @param callback 回调接口对象
	 */
	public void setReportCallback(ReportCallback callback) {
		mCallback = callback;
	}

	@Override
	public void process(RobotReportMessage msg, Communicator communicator) {
		if (mCallback != null) {
			System.out.printf("Process msg: %s\n", msg.toString());
			mCallback.displayReport(msg);
		}
	}

}
