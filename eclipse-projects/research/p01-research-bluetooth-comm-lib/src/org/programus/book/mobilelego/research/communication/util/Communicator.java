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

/**
 * 通讯员类。
 * 通讯员负责持续监听网络，取得消息，
 * 并将其转发给相应的操作员——{@link Processor}，
 * 同时提供发送数据功能。
 * @author programus
 */
public class Communicator {
	/**
	 * 操作员接口，所有操作员类必须实现此接口，
	 * 用以操作通讯员传来的消息。
	 * @param <T> 操作员可处理的消息协议
	 */
	public static interface Processor<T extends Protocol> {
		void process(T msg, Communicator communicator);
	}

	/** 存储所有操作员的Map。 */
	private Map<String, List<Processor<? extends Protocol>>> 
		processorMap = 
		new HashMap<String, List<Processor<? extends Protocol>>>();

	/** 读取消息用的输入流 */
	private ObjectInputStream input;
	/** 发送消息用的输出流 */
	private ObjectOutputStream output;
	
	/** 标记接收线程是否仍活跃 */
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
	
	/**
	 * 添加需要通讯员转发命令的操作员。
	 * @param type 要追加的操作员可以处理的命令协议类型
	 * @param processor 操作员对象
	 */
	public synchronized <P extends Protocol> void addProcessor(Class<P> type, Processor<P> processor) {
		// 从Map中取出此协议类型对应的操作员列表
		List<Processor<? extends Protocol>> processorList = processorMap.get(type.getName());
		if (processorList == null) {
			// 如果列表不存在，说明目前为止尚无此类型操作员被加入
			// 创建列表
			processorList = new LinkedList<Processor<? extends Protocol>>();
			// 将列表放入Map
			processorMap.put(type.getName(), processorList);
		}
		// 向列表中追加操作员
		processorList.add(processor);
	}
	
	/**
	 * 发送消息。
	 * @param msg 消息
	 */
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
