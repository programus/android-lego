package org.programus.book.mobilelego.robopet.server.robot.behaviors;


/**
 * 情绪平静时的行为模式。
 * @author programus
 *
 */
public class Normal extends AbstractBehavior {

	/**
	 * 除其他行为模式外的行为模式，优先级最低，为常态行为模式。
	 */
	@Override
	public boolean takeControl() {
		return true;
	}

	@Override
	public void move() {
	}

}
