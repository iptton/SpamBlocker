package com.iptton.SpamBlocker;

import com.iptton.SpamBlocker.R;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends Activity {

	final static String TAG = "MainActivity";
	SQLiteDatabase db;
	DBHelper helper;
	ListView lv;
	EditText editText;
	Cursor spamCursor;

    Boolean isShowingSpam = false;
    MyReceiver receiver;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = new DBHelper(this);
        db = helper.getWritableDatabase();
        
        
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SMSReceiver.ACTION_UPDATE);
        this.registerReceiver(receiver, filter);
		
        lv = (ListView)findViewById(R.id.blackList);
        editText = (EditText)findViewById(R.id.editText2);
        
        
        editText.setText("");
        String[] numbers = helper.getNumbers();
        for(int i=0;i<numbers.length;++i){
        	editText.append(numbers[i]+"\n");
        }
        
        Button updateBtn = (Button)findViewById(R.id.update);
        updateBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String str = editText.getText().toString();
				helper.saveRules(str, null);
			}
        	
        });
        
        
        
        Button toggleBtn = (Button)findViewById(R.id.toggleSpams);
        toggleBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
				if(imm.isActive()){
					imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				}
				if(!isShowingSpam){
					lv.setVisibility(View.VISIBLE);
					editText.setVisibility(View.GONE);
				}else{
					lv.setVisibility(View.GONE);
					editText.setVisibility(View.VISIBLE);
				}
				isShowingSpam = !isShowingSpam;
			}
        	
        });
        
        spamCursor = helper.select(DBHelper.TB_SPAM_NAME);
        startManagingCursor(spamCursor);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
        		android.R.layout.simple_list_item_2,
        		spamCursor,
        		new String[]{DBHelper.SENDER,DBHelper.MESSAGE_BODY},
        		new int[]{android.R.id.text1,android.R.id.text2});
        lv.setAdapter(adapter);
        
    }

    @Override
    public void onResume(){
    	super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    @Override
    public void onPause(){
    	super.onPause();
    }
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	this.stopManagingCursor(spamCursor);
    	db.close();
    	unregisterReceiver(receiver);
    	Log.i(TAG,"onDestroy");
    	
    }
    public void updateListView(){
    	spamCursor.requery();
    	lv.invalidate();
    }
    
    public class MyReceiver extends BroadcastReceiver{
		@Override
    	public void onReceive(Context context, Intent intent){
			Bundle bundle = intent.getExtras();
			String action = bundle.getString(SMSReceiver.KEY_ACTION);
			if(action.equals(SMSReceiver.UPDATE)){
				updateListView();
			}
		}
    }
    
}
