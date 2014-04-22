package org.programus.book.mobilelego.research.communication.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.programus.book.mobilelego.research.communication.protocol.Protocol;
import org.programus.book.mobilelego.research.communication.protocol.Protocol.Type;

public class Communicator<R extends Protocol, S extends Protocol> {
	public static interface Processor<Rcv extends Protocol, Snd extends Protocol> {
		void process(Rcv cmd, Communicator<Rcv, Snd> communicator);
	}
	private Map<Type, List<Processor<R, S>>> processorMap = new EnumMap<Type, List<Processor<R, S>>>(Type.class);
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	private boolean alive = true;
	
	public Communicator(ObjectInputStream input, ObjectOutputStream output) {
		this.input = input;
		this.output = output;
		this.startInputReadThread();
	}
	
	public void addProcessor(Type type, Processor<R, S> processor) {
		List<Processor<R, S>> processorList = processorMap.get(type);
		if (processorList == null) {
			processorList = new LinkedList<Processor<R, S>>();
		}
		processorList.add(processor);
	}
	
	public void send(S msg) {
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
                        @SuppressWarnings("unchecked")
						R cmd = (R) o;
                        processReceived(cmd);
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
	
	private void processReceived(R cmd) {
		if (cmd != null) {
            List<Processor<R, S>> processorList = processorMap.get(cmd.getType());
            for (Processor<R, S> processor : processorList) {
            	processor.process(cmd, this);
            }
		}
	}
}
