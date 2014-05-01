package org.programus.book.mobilelego.research.communication.protocol;

/**
 * 通知EV3打开/关闭马达报告功能的网络消息
 * @author programus
 *
 */
public class MotorReportCommand implements NetMessage {
	private static final long serialVersionUID = -3009205522237798520L;

	private boolean reportOn;

	public boolean isReportOn() {
		return reportOn;
	}

	public void setReportOn(boolean reportOn) {
		this.reportOn = reportOn;
	}

	@Override
	public String toString() {
		return "MotorReportCommand [reportOn=" + reportOn + "]";
	}
}
