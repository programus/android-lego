package org.programus.book.mobilelego.robopet.server.robot;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

import org.programus.book.mobilelego.robopet.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;
import org.programus.book.mobilelego.robopet.comm.util.Communicator;
import org.programus.book.mobilelego.robopet.net.OnConnectedListener;
import org.programus.book.mobilelego.robopet.server.net.Server;
import org.programus.book.mobilelego.robopet.server.processors.ExitProcessor;
import org.programus.book.mobilelego.robopet.server.processors.PetCommandProcessor;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.AngryForward;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.AvoidObstacle;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.CrazyBehavior;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.ExitProgram;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.HappyForward;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.ProcessNewCommand;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.SadForward;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.Stop;
import org.programus.book.mobilelego.robopet.server.robot.behaviors.WalkForward;
import org.programus.book.mobilelego.robopet.server.util.KeyCommandContainer;

public class Robot implements OnConnectedListener{
	private Server server;
	private Arbitrator arby; 
	private Behavior[] behaviors;
	private KeyCommandContainer cc = new KeyCommandContainer();
	
	private PetCommandProcessor petCmdProc = new PetCommandProcessor();
	private ExitProcessor exitProc = new ExitProcessor();
	
	public Robot() {
		this.behaviors = new Behavior[] {
			new WalkForward(),
			new SadForward(),
			new HappyForward(),
			new AngryForward(),
			new CrazyBehavior(),
			new AvoidObstacle(),
			new Stop(),
			new ProcessNewCommand(),
			new ExitProgram(cc),
		};
		
		this.arby = new Arbitrator(this.behaviors);
		this.initListeners();
	}
	
	private void initListeners() {
		Button.ESCAPE.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(Key k) {
				cc.setKeyCommand(KeyCommandContainer.KeyCommand.Esc);
			}
			@Override
			public void keyReleased(Key k) {
			}
		});
	}
	
	private void startServerAsync() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Sound.beepSequenceUp();
				server.start();
			}
		}, "Server-Daemon");
		t.start();
	}
	
	public void start()	 {
		this.server = Server.getInstance();
		server.setOnConnectedListener(this);
		this.startServerAsync();
		try {
			this.arby.start();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void onConnected(Communicator comm) {
		comm.addProcessor(PetCommand.class, this.petCmdProc);
		comm.addProcessor(ExitSignal.class, this.exitProc);
		Sound.beep();
	}

	@Override
	public void onFailed(Exception e) {
		Sound.buzz();
		System.err.println(e.getMessage());
	}
}
