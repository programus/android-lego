package org.programus.book.mobilelego.research.connect;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
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
		// TODO: 追加网络连接和发送数据代码
	}

}
