package org.programus.book.mobilelego.research.connect;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 主窗口
 *
 */
public class MainActivity extends Activity {
	private final static int PORT = 9988;
	private TextView mIpInput;
	private Button mBeep;
	private TextView mLog;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_activity);
		this.initComponents();
	}
	
	/**
	 * 初始化控件。
	 */
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

	/**
	 * 连接并发送数据到EV3
	 */
    private void connectAndSendData() {
        this.clearLog();
        // 从输入取得IP地址
        String ip = this.mIpInput.getText().toString();
        Socket socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(ip, PORT);
        OutputStream out = null;
        try {
            // 建立Socket连接
            this.appendLog(String.format("正在与%s:%d建立连接...", 
                ip, PORT));
            socket.connect(address);
            this.appendLog(String.format("连接%s:%d成功！", ip, PORT));
            // 取得输出流
            out = socket.getOutputStream();
            this.appendLog("成功取得输出流。");
            // 输出数据
            out.write(1);
            this.appendLog(String.format("输出数据：%d。", 1));
            // 清除本地缓存，确保数据发送出去
            out.flush();
        } catch (IOException e) {
            this.appendLog(e);
        } finally {
            // 确保输出流和连接关闭
            if (out != null) {
                try {
                    this.appendLog("关闭输出流。");
                    out.close();
                } catch (IOException e) {}
            }
            try {
                this.appendLog("关闭socket。");
                socket.close();
            } catch (IOException e) {}
        }
    }

    /**
     * 追加文本到日志文本框中
     * @param log 需要追加的文本
     */
    private void appendLog(final String log) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLog.append(log);
                mLog.append("\n");
            }
        });
    }

    /**
     * 追加例外信息到日志文本框中
     * @param e 需要追加的例外
     */
    private void appendLog(final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        String stackTrace = sw.toString();
        pw.close();
        this.appendLog(stackTrace);
    }

    /**
     * 清除日志
     */
    private void clearLog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLog.setText("");
            }
        });
    }
}
