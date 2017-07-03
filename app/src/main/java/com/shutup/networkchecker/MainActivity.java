package com.shutup.networkchecker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity{

    @InjectView(R.id.resultView)
    TextView mResultView;
    @InjectView(R.id.checkBtn)
    Button mCheckBtn;

    SpannedString mSpannedString = null;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.checkBtn)
    public void onViewClicked() {
        if (mCheckBtn.getText().toString().equals(getText(R.string.check))||mCheckBtn.getText().toString().equals(getText(R.string.recheck))) {
            if (!NetworkUtils.isNetworkEnable(getApplicationContext())) {
                Toast.makeText(this, "please connect to network !", Toast.LENGTH_SHORT).show();
                return;
            }
            mCheckBtn.setText(R.string.checking);
            mCheckBtn.setEnabled(false);
            mSpannedString = new SpannedString(getString(R.string.start_check));
            mResultView.setText( mSpannedString);
            int stage = getResources().getInteger(R.integer.stage_local);
            startCheckService(getUrl(stage), stage);
        }else {
            mCheckBtn.setText(R.string.check);
            mCheckBtn.setEnabled(true);
        }
    }

    private void startCheckService(String url, int stage) {
        Intent intent = new Intent(MainActivity.this,MyIntentService.class);
        intent.setAction(MyIntentService.ACTION_PING);
        intent.putExtra(MyIntentService.EXTRA_PARAM_URL, url);
        intent.putExtra(MyIntentService.EXTRA_PARAM_STAGE, stage);
        startService(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultMessage(ResultMessage resultMessage) {
        if (resultMessage.isSuccess()) {
            SpannableString spannableString = new SpannableString("\nstage "+ resultMessage.getStage() + " passed!\n");
            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0,spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannedString = (SpannedString) TextUtils.concat(mSpannedString,spannableString,resultMessage.getMsg());

            mResultView.setText(mSpannedString);
            int stage = resultMessage.getStage();
            if (stage < getResources().getInteger(R.integer.stage_outter_url)) {
                stage++;
                if (NetworkUtils.isMobileEnable(getApplicationContext())) {
                    if (stage == getResources().getInteger(R.integer.stage_local_gateway)) {
                        stage++;
                    }
                }
                String url = getUrl(stage);
                startCheckService(url,stage);
            }else {
                mCheckBtn.setText(R.string.recheck);
                mCheckBtn.setEnabled(true);
            }
        }else {
            String result = "\nStage " + resultMessage.getStage() + " failed\n";
            SpannableString spannableString = new SpannableString(result);
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0,spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannedString = (SpannedString) TextUtils.concat(mSpannedString,spannableString,resultMessage.getMsg());
            mResultView.setText(mSpannedString);

            mCheckBtn.setText(R.string.recheck);
            mCheckBtn.setEnabled(true);
        }
    }

    private String getUrl(int stage) {

        if (stage == getResources().getInteger(R.integer.stage_local)) {
            return NetworkUtils.getIPAddress(true);
        }else if (stage == getResources().getInteger(R.integer.stage_local_gateway)) {
            String url = NetworkUtils.getIPAddress(true);
            String result = url.substring(0,url.lastIndexOf('.'));
            result += ".1";
            return result;
        }else if (stage == getResources().getInteger(R.integer.stage_inner_url)) {
            return getResources().getStringArray(R.array.urls)[0];
        }else if (stage == getResources().getInteger(R.integer.stage_outter_url)) {
            return getResources().getStringArray(R.array.urls)[1];
        }else {
            return null;
        }
    }
}
