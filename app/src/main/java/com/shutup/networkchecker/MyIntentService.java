package com.shutup.networkchecker;

import android.app.IntentService;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;


public class MyIntentService extends IntentService {
    public static final String ACTION_PING = "com.shutup.networkchecker.action.PING";

    public static final String EXTRA_PARAM_URL = "com.shutup.networkchecker.extra.URL";
    public static final String EXTRA_PARAM_STAGE = "com.shutup.networkchecker.extra.STAGE";

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PING.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_PARAM_URL);
                final int stage = intent.getIntExtra(EXTRA_PARAM_STAGE,-1);
                handleActionPing(url,stage);
            }
        }
    }

    /**
     * Handle action PING in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPing(String url,int stage) {
        ResultMessage result = NetworkUtils.ping(url,stage);
        EventBus.getDefault().post(result);
    }




}
