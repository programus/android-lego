package org.programus.book.mobilelego.research.communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.programus.book.mobilelego.research.communication.protocol.PhoneMessage;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand;
import org.programus.book.mobilelego.research.communication.protocol.RobotCommand.Type;

public class Communicator {
	public static interface RobotCmdProcessor {
		void process(RobotCommand cmd, Communicator communicator);
	}
	private Map<Type, List<RobotCmdProcessor>> processorMap = new EnumMap<Type, List<RobotCmdProcessor>>(Type.class);
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	private boolean alive = true;
	
	public Communicator(ObjectInputStream input, ObjectOutputStream output) {
		this.input = input;
		this.output = output;
		this.startInputReadThread();
	}
	
	public void addRobotCmdProcessor(Type type, RobotCmdProcessor processor) {
		List<RobotCmdProcessor> processorList = processorMap.get(type);
		if (processorList == null) {
			processorList = new LinkedList<RobotCmdProcessor>();
		}
		processorList.add(processor);
	}
	
	public void sendPhoneMsg(PhoneMessage msg) {
		synchronized (output) {
			try {
				output.writeObject(msg);
			} catch (IOException e) {
				alive = false;
			}
		}
	}
	
	private void startInputReadThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (alive) {
					Object o = null;
					try {
						o = input.readObject();
					} catch (Exception e) {
						break;
					}
					if (o != null) {
                        RobotCommand cmd = (RobotCommand) o;
                        processRobotCmd(cmd);
                        if (cmd.getType() == Type.Exit) {
                        	alive = false;
                        }
					}
				}
				finish();
			}
		}, "read-input");
		t.start();
	}
	
	private void finish() {
        try {
            input.close();
        } catch (IOException e) {
        }
        synchronized (output) {
            try {
                output.close();
            } catch (IOException e) {
            }
        }
	}
	
	private void processRobotCmd(RobotCommand cmd) {
		if (cmd != null) {
            List<RobotCmdProcessor> processorList = processorMap.get(cmd.getType());
            for (RobotCmdProcessor processor : processorList) {
            	processor.process(cmd, this);
            }
		}
	}
}
