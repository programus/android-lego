package org.programus.book.mobilelego.robopet.mobile;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.programus.book.mobilelego.robopet.comm.protocol.NetMessage;
import org.programus.book.mobilelego.robopet.comm.protocol.PetCommand;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	/** 语音识别对话框的请求代码 */
	private final static int REQUEST_CODE = 1980;
	
	private final static String CMD_RE = "^([^0-9]+)([0-9]*)";
	
	private final static int DEFAULT_VALUE = 3;
	
	/** 主要组件，用以显示命令和点击开始识别语音 */
	private TextView mMainView;
	
	private Map<String, PetCommand.Command> mCmdTable = new HashMap<String, PetCommand.Command>();
	
	private Map<PetCommand.Command, String> mCmdFormats = new EnumMap<PetCommand.Command, String>(PetCommand.Command.class);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		this.initCtrls();
		// 初始化语音命令表及命令内容表
		this.initData();
		// 检查语音识别的可用性
		this.checkVoiceRecognitionAvailability();
	}
	
	private void initData() {
		Resources res = this.getResources();
		SparseArray<PetCommand.Command> resTable = new SparseArray<PetCommand.Command>();
		resTable.append(R.array.forward, PetCommand.Command.Forward);
		resTable.append(R.array.backward, PetCommand.Command.Backward);
		resTable.append(R.array.turn_left, PetCommand.Command.TurnLeft);
		resTable.append(R.array.turn_right, PetCommand.Command.TurnRight);
		resTable.append(R.array.calm, PetCommand.Command.Calm);
		resTable.append(R.array.stop, PetCommand.Command.Stop);
		resTable.append(R.array.exit, PetCommand.Command.Exit);
		resTable.append(R.array.shutdown, PetCommand.Command.Shutdown);
		for (int i = 0; i < resTable.size(); i++) {
			String[] candidates = res.getStringArray(resTable.keyAt(i));
			for (String text : candidates) {
				this.mCmdTable.put(text, resTable.valueAt(i));
			}
		}
		
		mCmdFormats.put(PetCommand.Command.Forward, res.getString(R.string.forward));
		mCmdFormats.put(PetCommand.Command.Backward, res.getString(R.string.backward));
		mCmdFormats.put(PetCommand.Command.TurnLeft, res.getString(R.string.turn_left));
		mCmdFormats.put(PetCommand.Command.TurnRight, res.getString(R.string.turn_right));
		mCmdFormats.put(PetCommand.Command.Calm, res.getString(R.string.calm));
		mCmdFormats.put(PetCommand.Command.Stop, res.getString(R.string.stop));
		mCmdFormats.put(PetCommand.Command.Exit, res.getString(R.string.exit));
		mCmdFormats.put(PetCommand.Command.Shutdown, res.getString(R.string.shutdown));
	}
	
	/**
	 * 检查是否支持语音支持
	 */
	private void checkVoiceRecognitionAvailability() {
		PackageManager pm = this.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() <= 0) {
			this.mMainView.setOnClickListener(null);
			this.mMainView.setText(this.getString(R.string.not_supported, this.getString(R.string.voice_recognition)));
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initCtrls() {
		this.mMainView = (TextView) this.findViewById(R.id.main_view);
		
		// 按下按钮则弹出语音识别窗口
		this.mMainView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});
	}
	
	/**
	 * 启动语音识别对话框
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getString(R.string.app_name));
		// 使用请求代码启动Activity
		this.startActivityForResult(intent, REQUEST_CODE);
	}
	
	private void processRecognitionResults(List<String> results) {
		final int CMD_INDEX = 1;
		final int VALUE_INDEX = 2;
		
		Pattern p = Pattern.compile(CMD_RE);
		String message = null;
		for (String result : results) {
			Matcher m = p.matcher(result);
			if (m.find()) {
				String cmdPart = m.group(CMD_INDEX);
				String valuePart = m.group(VALUE_INDEX);
				PetCommand.Command cmd = this.mCmdTable.get(cmdPart);
				if (cmd != null) {
					int value = valuePart.length() > 0 ? Integer.parseInt(valuePart) : DEFAULT_VALUE;
					PetCommand msg = new PetCommand(cmd, 4);
					this.sendMessage(msg);
					message = String.format(this.mCmdFormats.get(cmd), value);
					break;
				}
			}
		}
		
		if (message == null) {
			message = this.getString(R.string.unknown);
		}
		
		this.mMainView.setText(message);
	}
	
	private void sendMessage(NetMessage msg) {
		Toast.makeText(this, "Send message...", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// 当请求代码是语音识别对话框的请求代码 并且 Activity返回结果为正常时
			// 取得识别到的文本列表
			List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			// 处理识别内容
			this.processRecognitionResults(results);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
