package org.programus.book.mobilelego.research.communication.processor;

import org.programus.book.mobilelego.research.communication.protocol.PhoneMessage;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;
import org.programus.book.mobilelego.research.communication.util.Communicator;
import org.programus.book.mobilelego.research.communication.util.Communicator.Processor;

public class MotorReportProcessor implements Processor<PhoneMessage, RobotCommand> {
	public static interface ReportCallback {
		void displayReport(int value);
	}
	
	private ReportCallback mCallback;
	
	public void setReportCallback(ReportCallback callback) {
		this.mCallback = callback;
	}
	
	@Override
	public void process(PhoneMessage msg,
			Communicator<PhoneMessage, RobotCommand> communicator) {
		if (mCallback != null) {
			System.out.printf("Process msg: %s\n", msg.toString());
			int value = msg.getIntValue();
			mCallback.displayReport(value);
		}
	}

}
