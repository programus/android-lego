package org.programus.book.mobilelego.robopet.server.robot;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

import org.programus.book.mobilelego.robopet.server.robot.behaviors.AngryForward;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.AvoidObstacle;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.CrazyBehavior;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.ExitProgram;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.HappyForward;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.SadForward;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.Stop;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.WalkForward;

public class Robot {
	private Arbitrator arby; 
	private Behavior[] behaviors;
	private CommandContainer cc = new CommandContainer();
	
	public Robot() {
		this.behaviors = new Behavior[] {
			new WalkForward(),
			new SadForward(),
			new HappyForward(),
			new AngryForward(),
			new CrazyBehavior(),
			new AvoidObstacle(),
			new Stop(),
			new ExitProgram(cc),
		};
		
		this.arby = new Arbitrator(this.behaviors);
		this.initListeners();
	}
	
	private void initListeners() {
		Button.ESCAPE.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				cc.setKeyCommand(CommandContainer.KeyCommand.Esc);
			}
			@Override
			public void keyReleased(Key k) {
			}
		});
	}
	
	public void start()	 {
		this.arby.start();
	}
}
