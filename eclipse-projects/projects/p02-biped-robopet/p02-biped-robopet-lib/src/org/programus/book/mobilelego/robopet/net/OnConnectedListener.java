package org.programus.book.mobilelego.robopet.net;

import org.programus.book.mobilelego.robopet.comm.util.Communicator;

/**
 * 当建立连接时回调用的接口
 */
public interface OnConnectedListener {
	/**
	 * 正常建立连接时将调用此函数
	 * @param comm 建立连接后创建的通讯员对象
	 */
	void onConnected(Communicator comm);
	/**
	 * 连接失败时调用此函数
	 * @param e 失败时的例外
	 */
	void onFailed(Exception e);
}
