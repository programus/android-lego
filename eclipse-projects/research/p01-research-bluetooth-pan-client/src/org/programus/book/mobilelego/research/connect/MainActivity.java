package org.programus.book.mobilelego.research.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private final static int PORT = 9988;
	private TextView mIpInput;
	private Button mBeep;
	private TextView mLog;
	
	private static Handler sHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);
		this.initComponents();
	}
	
	private void initComponents() {
		this.mIpInput = (TextView) this.findViewById(
				R.id.ip_input);
		this.mLog = (TextView) this.findViewById(R.id.log);
		this.mBeep = (Button) this.findViewById(R.id.beep);
		
		this.mBeep.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 为防止界面线程阻塞，在新线程执行网络相关代码。
				Thread t = new Thread("net-thread") {
					@Override
					public void run() {
						connectAndSendData();
					}
				};
				t.start();
			}
		});
	}

	protected void connectAndSendData() {
		// 从输入取得IP地址
		String ip = this.mIpInput.getText().toString();
		Socket socket = null;
		OutputStream out = null;
		try {
			// 建立Socket连接
			socket = new Socket(ip, PORT);
			// 取得输出流
			out = socket.getOutputStream();
			// 输出数据
			out.write(1);
			// 清除本地缓存，确保数据发送出去
			out.flush();
		} catch (IOException e) {
			// TODO: 出错时输出到Log中
		} finally {
			// 确保输出流和连接关闭
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {}
			}
		}
	}

}
