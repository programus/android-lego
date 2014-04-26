package org.programus.book.mobilelego.research.communication.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.programus.book.mobilelego.research.communication.protocol.ExitSignal;
import org.programus.book.mobilelego.research.communication.protocol.Protocol;

public class Communicator {
	public static interface Processor<T extends Protocol> {
		void process(T msg, Communicator communicator);
	}
	private Map<String, List<Processor<? extends Protocol>>> processorMap = new HashMap<String, List<Processor<? extends Protocol>>>();
	private ObjectInputStream input;
	private ObjectOutputStream output;
	
	private boolean available;
	
	public Communicator() {
	}
	
	public Communicator(InputStream input, OutputStream output) throws IOException {
		this.reset(input, output);
	}
	
	public synchronized void reset(InputStream input, OutputStream output) throws IOException {
		if (this.available) {
			this.finish();
		}
		this.available = true;
		this.output = new ObjectOutputStream(output);
		this.output.flush();
		this.input = new ObjectInputStream(input);
		this.startInputReadThread();
	}
	
	public <P extends Protocol> void addProcessor(Class<P> type, Processor<P> processor) {
		List<Processor<? extends Protocol>> processorList = processorMap.get(type.getName());
		if (processorList == null) {
			processorList = new LinkedList<Processor<? extends Protocol>>();
			processorMap.put(type.getName(), processorList);
		}
		processorList.add(processor);
	}
	
	public void send(Protocol msg) {
		synchronized (output) {
			try {
				System.out.println(String.format("Send: %s", msg.toString()));
				output.writeObject(msg);
				output.flush();
			} catch (IOException e) {
				available = false;
			}
		}
	}
	
	public void close() {
		this.available = false;
	}
	
	public boolean isAvailable() {
		return this.available;
	}
	
	private void startInputReadThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (available) {
					Object o = null;
					try {
						o = input.readObject();
					} catch (Exception e) {
						available = false;
						break;
					}
					if (o != null) {
						System.out.println(String.format("Received: %s", o.toString()));
						if (o instanceof ExitSignal) {
							available = false;
							break;
						} else {
                            Protocol cmd = (Protocol) o;
                            processReceived(cmd);
						}
					}
				}
				finish();
			}
		}, "read-input");
		t.start();
	}
	
	private synchronized void finish() {
		this.available = false;
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
	
	private <P extends Protocol> void processReceived(P cmd) {
		if (cmd != null) {
            List<Processor<? extends Protocol>> processorList = processorMap.get(cmd.getClass().getName());
            if (processorList != null) {
                for (Processor<? extends Protocol> processor : processorList) {
                	@SuppressWarnings("unchecked")
					Processor<P> p = (Processor<P>) processor;
                    p.process(cmd, this);
                }
            }
		}
	}
}
