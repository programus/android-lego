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

public class MainActivity extends Activity {
	private final static int REQUEST_CODE = 1980;
	
	private Button mSpeak;
	private ListView mResultsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.initCtrls();
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
	
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getString(R.string.app_name));
		this.startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			this.mResultsView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
