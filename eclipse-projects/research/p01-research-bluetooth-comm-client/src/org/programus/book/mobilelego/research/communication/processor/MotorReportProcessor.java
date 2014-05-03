package org.programus.book.mobilelego.research.communication.processor;

import org.programus.book.mobilelego.research.communication.protocol.MotorReportMessage;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

/**
 * 马达数据报告操作员
 * @author programus
 */
public class MotorReportProcessor implements Processor<MotorReportMessage> {
	/**
	 * 报告回调接口
	 */
	public static interface ReportCallback {
		/**
		 * 具体现实报告的函数
		 * @param msg 报告消息
		 */
		void displayReport(MotorReportMessage msg);
	}
	
	private ReportCallback mCallback;
	
	/**
	 * 设置回调接口对象
	 * @param callback 回调接口对象
	 */
	public void setReportCallback(ReportCallback callback) {
		this.mCallback = callback;
	}
	
	@Override
	public void process(MotorReportMessage msg, Communicator communicator) {
		if (mCallback != null) {
			System.out.printf("Process msg: %s\n", msg.toString());
			mCallback.displayReport(msg);
		}
	}

}
