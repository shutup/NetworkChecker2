package com.shutup.networkchecker;

/**
 * Created by shutup on 2017/7/3.
 */

public class ResultMessage {
    private String msg;
    private boolean isSuccess;
    private int stage;

    public ResultMessage(String msg, boolean isSuccess, int stage) {
        this.msg = msg;
        this.isSuccess = isSuccess;
        this.stage = stage;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
