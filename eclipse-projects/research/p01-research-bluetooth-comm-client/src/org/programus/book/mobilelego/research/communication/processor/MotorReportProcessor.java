package org.programus.book.mobilelego.research.communication.processor;

import org.programus.book.mobilelego.research.communication.protocol.MotorReportMessage;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

public class MotorReportProcessor implements Processor<MotorReportMessage> {
	public static interface ReportCallback {
		void displayReport(MotorReportMessage msg);
	}
	
	private ReportCallback mCallback;
	
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
