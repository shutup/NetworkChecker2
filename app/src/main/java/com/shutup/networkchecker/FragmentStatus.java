package com.shutup.networkchecker;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStatus extends Fragment {


    @InjectView(R.id.checkBtn)
    Button mCheckBtn;
    @InjectView(R.id.stage_local)
    View mStageLocal;
    @InjectView(R.id.stage_local_gateway)
    View mStageLocalGateway;
    @InjectView(R.id.stage_inner_url)
    View mStageInnerUrl;
    @InjectView(R.id.stage_gfw)
    View mStageGfw;
    @InjectView(R.id.stage_outer_url)
    View mStageOuterUrl;

    private SpannedString mSpannedString;

    public FragmentStatus() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_status, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.checkBtn)
    public void onViewClicked() {
        if (mCheckBtn.getText().toString().equals(getText(R.string.check)) || mCheckBtn.getText().toString().equals(getText(R.string.recheck))) {
            if (!NetworkUtils.isNetworkEnable(getActivity().getApplicationContext())) {
                Toast.makeText(getActivity(), R.string.alert_msg, Toast.LENGTH_SHORT).show();
                return;
            }
            restore();
            mCheckBtn.setText(R.string.checking);
            mCheckBtn.setEnabled(false);
            mSpannedString = new SpannedString(getString(R.string.start_check));
            EventBus.getDefault().post(mSpannedString);

            int stage = getResources().getInteger(R.integer.stage_local);
            startCheckService(getUrl(stage), stage);
        } else {
            mCheckBtn.setText(R.string.check);
            mCheckBtn.setEnabled(true);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultMessage(ResultMessage resultMessage) {
        if (resultMessage.isSuccess()) {
            SpannableString spannableString = new SpannableString("\nstage " + resultMessage.getStage() + " passed!\n");
            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannedString = (SpannedString) TextUtils.concat(mSpannedString, spannableString, resultMessage.getMsg());
            EventBus.getDefault().post(mSpannedString);
            int stage = resultMessage.getStage();
            changeLineColor(stage,true);
            if (stage < getResources().getInteger(R.integer.stage_outter_url)) {
                stage++;
                if (NetworkUtils.isMobileEnable(getActivity().getApplicationContext())) {
                    if (stage == getResources().getInteger(R.integer.stage_local_gateway)) {
                        stage++;
                    }
                }
                String url = getUrl(stage);
                startCheckService(url, stage);
            } else {
                mCheckBtn.setText(R.string.recheck);
                mCheckBtn.setEnabled(true);
            }
        } else {
            int stage = resultMessage.getStage();
            String result = "\nStage " + stage + " failed\n";
            SpannableString spannableString = new SpannableString(result);
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannedString = (SpannedString) TextUtils.concat(mSpannedString, spannableString, resultMessage.getMsg());
            EventBus.getDefault().post(mSpannedString);
            changeLineColor(stage,false);
            mCheckBtn.setText(R.string.recheck);
            mCheckBtn.setEnabled(true);
        }
    }

    private void changeLineColor(int stage,boolean isSuccess) {
        int color = getResources().getColor(R.color.colorLineNormal);
        if (isSuccess) {
            color = getResources().getColor(R.color.colorLineEnable);
        }else {
            color = getResources().getColor(R.color.colorLineDisable);
        }
        if (stage == getResources().getInteger(R.integer.stage_local)) {
            mStageLocal.setBackgroundColor(color);
        }else if (stage == getResources().getInteger(R.integer.stage_local_gateway)) {
            mStageLocalGateway.setBackgroundColor(color);
        }else if (stage == getResources().getInteger(R.integer.stage_inner_url)) {
            mStageInnerUrl.setBackgroundColor(color);
        }else if (stage == getResources().getInteger(R.integer.stage_outter_url)) {
            mStageGfw.setBackgroundColor(color);
            mStageOuterUrl.setBackgroundColor(color);
        }
    }

    private void startCheckService(String url, int stage) {
        Intent intent = new Intent(getActivity(), MyIntentService.class);
        intent.setAction(MyIntentService.ACTION_PING);
        intent.putExtra(MyIntentService.EXTRA_PARAM_URL, url);
        intent.putExtra(MyIntentService.EXTRA_PARAM_STAGE, stage);
        getActivity().startService(intent);
    }

    private String getUrl(int stage) {

        if (stage == getResources().getInteger(R.integer.stage_local)) {
            return NetworkUtils.getIPAddress(true);
        } else if (stage == getResources().getInteger(R.integer.stage_local_gateway)) {
            String url = NetworkUtils.getIPAddress(true);
            String result = url.substring(0, url.lastIndexOf('.'));
            result += ".1";
            return result;
        } else if (stage == getResources().getInteger(R.integer.stage_inner_url)) {
            return getResources().getStringArray(R.array.urls)[0];
        } else if (stage == getResources().getInteger(R.integer.stage_outter_url)) {
            return getResources().getStringArray(R.array.urls)[1];
        } else {
            return null;
        }
    }

    private void restore() {
        mStageLocal.setBackgroundColor(getResources().getColor(R.color.colorLineNormal));
        mStageLocalGateway.setBackgroundColor(getResources().getColor(R.color.colorLineNormal));
        mStageInnerUrl.setBackgroundColor(getResources().getColor(R.color.colorLineNormal));
        mStageGfw.setBackgroundColor(getResources().getColor(R.color.colorLineNormal));
        mStageOuterUrl.setBackgroundColor(getResources().getColor(R.color.colorLineNormal));
    }
}
