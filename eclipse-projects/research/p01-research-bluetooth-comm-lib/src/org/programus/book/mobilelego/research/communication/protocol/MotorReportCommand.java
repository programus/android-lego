package org.programus.book.mobilelego.research.communication.protocol;

public class MotorReportCommand implements Protocol {
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