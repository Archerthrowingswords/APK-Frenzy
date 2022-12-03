package org.benews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.util.HashMap;
import org.benews.BackgroundSocket;
import org.benews.BeNewsFragList;
/* loaded from: classes.dex */
public class BeNews extends FragmentActivity implements BeNewsFragList.OnFragmentInteractionListener, View.OnClickListener, BackgroundSocket.NewsUpdateListener {
    private static final String TAG = "BeNews";
    private static Context context;
    private static ProgressBar pb = null;
    ArrayAdapter<HashMap<String, String>> listAdapter;
    boolean toUpdate = false;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() { // from class: org.benews.BeNews.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context2, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            BeNews.this.finishOnStart();
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_be_news);
        BitmapHelper.init(getResources().getDisplayMetrics().density);
        context = getApplicationContext();
        Intent serviceIntent = new Intent(context, PullIntentService.class);
        context.startService(serviceIntent);
    }

    public void setProgressBar(int progress) {
        if (pb != null) {
            pb.setProgress(progress);
        }
    }

    public synchronized boolean isToUpdate() {
        return this.toUpdate;
    }

    public synchronized void setToUpdate(boolean toUpdate) {
        this.toUpdate = toUpdate;
    }

    @Override // org.benews.BackgroundSocket.NewsUpdateListener
    public synchronized void onNewsUpdate() {
        setToUpdate(true);
        final Button b = (Button) findViewById(R.id.bt_refresh);
        runOnUiThread(new Runnable() { // from class: org.benews.BeNews.1
            @Override // java.lang.Runnable
            public synchronized void run() {
                if (BeNews.this.isToUpdate()) {
                    BeNews.this.listAdapter.notifyDataSetChanged();
                    BackgroundSocket sucker = BackgroundSocket.self();
                    if (!b.isEnabled()) {
                        sucker.setRun(true);
                        int i = 100;
                        while (sucker.isRunning() && i > 0) {
                            i -= 20;
                            BeNews.this.setProgressBar(i);
                            BackgroundSocket.Sleep(1);
                        }
                        BeNews.this.setProgressBar(0);
                        b.setEnabled(true);
                    }
                    BeNews.this.listAdapter.notifyDataSetChanged();
                    BeNews.this.setToUpdate(false);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onStop() {
        super.onStop();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onStart() {
        super.onStart();
        if (BackgroundSocket.self().isThreadStarted()) {
            finishOnStart();
        }
    }

    public void finishOnStart() {
        BackgroundSocket sucker = BackgroundSocket.self();
        BeNewsFragList bfl = new BeNewsFragList();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_placeholder, bfl);
        ft.commit();
        this.listAdapter = new BeNewsArrayAdapter(this, sucker.getList());
        bfl.setListAdapter(this.listAdapter);
        Button b = (Button) findViewById(R.id.bt_refresh);
        b.setOnClickListener(this);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setProgress(0);
        pb.setMax(100);
        sucker.setOnNewsUpdateListener(this);
        setToUpdate(true);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mMessageReceiver, new IntentFilter(new IntentFilter(BackgroundSocket.READY)));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mMessageReceiver);
        super.onPause();
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.be_news_menu, menu);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override // org.benews.BeNewsFragList.OnFragmentInteractionListener
    public void onItemPress(int position) {
        try {
            Object o = this.listAdapter.getItem(position);
            String keyword = o.toString();
            Toast.makeText(this, "You selected: " + keyword, 0).show();
            BackgroundSocket sucker = BackgroundSocket.self();
            if (sucker != null) {
                DetailFragView details = DetailFragView.newInstance((HashMap) o);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_placeholder, details);
                ft.addToBackStack("DETAILS");
                ft.commit();
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception:" + e);
        }
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.detail_image) != null) {
            getSupportFragmentManager().popBackStack("DETAILS", 1);
        } else {
            super.onBackPressed();
        }
    }

    public static Context getAppContext() {
        return context;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Button button = (Button) view;
        final BackgroundSocket sucker = BackgroundSocket.self();
        button.setEnabled(false);
        sucker.setRun(false);
        new Thread(new Runnable() { // from class: org.benews.BeNews.3
            @Override // java.lang.Runnable
            public void run() {
                int i = 0;
                BeNews.this.setProgressBar(0);
                while (sucker.isRunning()) {
                    BackgroundSocket.Sleep(1);
                    i++;
                    if (i <= 100) {
                        BeNews.this.setProgressBar(i);
                    }
                }
                sucker.reset_news();
            }
        }).start();
    }
}
