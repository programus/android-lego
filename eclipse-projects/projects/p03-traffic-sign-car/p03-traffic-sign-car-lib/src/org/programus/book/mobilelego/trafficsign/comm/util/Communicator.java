package org.programus.book.mobilelego.trafficsign.comm.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.programus.book.mobilelego.trafficsign.comm.protocol.ExitSignal;
import org.programus.book.mobilelego.trafficsign.comm.protocol.NetMessage;

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
	 * @param <T> 操作员可处理的消息类型
	 */
	public static interface Processor<T extends NetMessage> {
		void process(T msg, Communicator communicator);
	}

	/** 存储所有操作员的Map。 */
	private Map<String, List<Processor<? extends NetMessage>>> 
		processorMap = 
		new HashMap<String, List<Processor<? extends NetMessage>>>();

	/** 读取消息用的输入流 */
	private ObjectInputStream input;
	/** 发送消息用的输出流 */
	private ObjectOutputStream output;
	
	/** 标记接收线程是否仍活跃 */
	private boolean available;
	
	/**
	 * 默认构造函数
	 */
	public Communicator() {
	}
	
	/**
	 * 构造函数
	 * @param input 输入流
	 * @param output 输出流
	 * @throws IOException 当创建输入输出流出错时抛出
	 */
	public Communicator(InputStream input, OutputStream output) throws IOException {
		this.reset(input, output);
	}
	
	/**
	 * 使用新的输入输出流对象重设通讯员。此函数不会重设已添加的操作员信息。
	 * @param input 输入流
	 * @param output 输出流
	 * @throws IOException 当创建输入输出流出错时抛出
	 */
	public synchronized void reset(InputStream input, OutputStream output) throws IOException {
		if (this.available) {
			this.finish();
		}
		this.available = true;
		this.output = new ObjectOutputStream(output);
		// 对ObjectOutputStream，必须在建立输出流后立即清空缓存，方能避免阻塞。
		this.output.flush();
		this.input = new ObjectInputStream(input);
		this.startInputReadThread();
	}
	
	/**
	 * 清除操作员对象。
	 * 当指定消息类时，清除指定类的操作员对象；
	 * 当未指定消息类（消息类为null）时，清除所有操作员对象。
	 * @param type 指定清楚操作员对象的消息类，为null时清除所有操作员
	 */
	public synchronized <M extends NetMessage> void clearProcessor(Class<M> type) {
		if (type != null) {
			// 如果指定了类型，则清楚指定类型中的所有操作员对象
			List<Processor<? extends NetMessage>> processorList = processorMap.get(type.getName());
			if (processorList != null) {
                processorList.clear();
			}
		} else {
			// 否则，清空操作员清单
			processorMap.clear();
		}
	}
	
	/**
	 * 添加需要通讯员转发消息的操作员。
	 * @param type 要追加的操作员可以处理的消息类型
	 * @param processor 操作员对象
	 */
	public synchronized <M extends NetMessage> void addProcessor(Class<M> type, Processor<M> processor) {
		// 从Map中取出此消息类型对应的操作员列表
		List<Processor<? extends NetMessage>> processorList = processorMap.get(type.getName());
		if (processorList == null) {
			// 如果列表不存在，说明目前为止尚无此类型操作员被加入
			// 创建列表
			processorList = new LinkedList<Processor<? extends NetMessage>>();
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
	public void send(NetMessage msg) {
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
	
	/**
	 * 建议通讯员结束工作。此函数不会强制关闭输入输出流。
	 */
	public void close() {
		this.available = false;
	}
	
	/**
	 * 确认输入流读取线程仍在工作
	 * @return 当输入流读取线程仍在工作时返回真
	 */
	public boolean isAvailable() {
		return this.available;
	}
	
	/**
	 * 启动读取输入流的线程
	 */
	private void startInputReadThread() {
		// 创建一个新线程
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (available) {
					// 当通讯员未被关闭时，循环
					Object o = null;
					try {
						// 读取消息
						o = input.readObject();
					} catch (Exception e) {
						available = false;
						break;
					}
					if (o != null) {
						System.out.println(String.format("Received: %s", o.toString()));
                        NetMessage msg = (NetMessage) o;
                        // 处理消息
                        processReceived(msg);
						if (o instanceof ExitSignal) {
							// 如果消息为退出命令，则关闭通讯员
							close();
							// 退出循环
							break;
						}
					}
				}
				finish();
			}
		}, "read-input");
		// 启动线程
		t.start();
	}
	
	/**
	 * 彻底结束通讯员工作，关闭输入输出流
	 */
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
	
	/**
	 * 将接收到的消息转给操作员处理
	 * @param msg 接收到的消息
	 */
	private <M extends NetMessage> void processReceived(M msg) {
		// 检查传入参数的有效性
		if (msg != null) {
			// 取出消息类型对应的操作员列表
            List<Processor<? extends NetMessage>> processorList = processorMap.get(msg.getClass().getName());
            if (processorList != null) {
            	// 当操作员列表存在时，循环所有操作员
                for (Processor<? extends NetMessage> processor : processorList) {
                	// 强制转换操作员类型为实际的类型
                	@SuppressWarnings("unchecked")
					Processor<M> p = (Processor<M>) processor;
                	// 让操作员处理消息
                    p.process(msg, this);
                }
            }
		}
	}
}
