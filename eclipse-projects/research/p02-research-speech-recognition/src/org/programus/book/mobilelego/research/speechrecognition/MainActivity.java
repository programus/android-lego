package org.programus.book.mobilelego.research.speechrecognition;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

/**
 * 语音识别调研程序主界面
 * @author programus
 *
 */
public class MainActivity extends Activity {
	/** 语音识别对话框的请求代码 */
	private final static int REQUEST_CODE = 1980;
	
	/** 开始语音识别的按钮 */
	private Button mSpeak;
	/** 显示识别结果的列表 */
	private ListView mResultsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		this.initCtrls();
		// 检查语音识别的可用性
		this.checkVoiceRecognitionAvailability();
	}
	
	/**
	 * 检查是否支持语音支持
	 */
	private void checkVoiceRecognitionAvailability() {
		PackageManager pm = this.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() <= 0) {
			this.mSpeak.setEnabled(false);
			this.mSpeak.setText(this.getString(R.string.not_supported, this.getString(R.string.voice_recognition)));
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initCtrls() {
		this.mSpeak = (Button) this.findViewById(R.id.speak);
		this.mResultsView = (ListView) this.findViewById(R.id.results);
		
		// 按下按钮则弹出语音识别窗口
		this.mSpeak.setOnClickListener(new OnClickListener() {
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			// 当请求代码是语音识别对话框的请求代码 并且 Activity返回结果为正常时
			// 取得识别到的文本列表
			List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			// 更新列表显示
			this.mResultsView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
