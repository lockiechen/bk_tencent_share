package com.tencent.wework.api;

import com.tencent.wework.api.model.BaseMessage;

import android.content.Intent;

public interface IWWAPI {
    /**
     *
     * @param schema
     * @return
     */
    boolean registerApp(String schema);

    void unregisterApp();

    boolean handleIntent(Intent var1, IWWAPIEventHandler var2);

    /**
     *
     * @return 
     */
    boolean isWWAppInstalled();

    /**
     *
     * @return 
     */
    boolean isWWAppSupportAPI();

    /**
     *
     * @return 
     */
    int getWWAppSupportAPI();

    /**
     * 
     * @return
     */
    boolean openWWApp();

    /**
     *
     * @param 
     * @return 
     */
    boolean sendMessage(BaseMessage var1);

    /**
     *
     * @param var1 
     * @param callback 
     * @return 
     */
    boolean sendMessage(BaseMessage var1, IWWAPIEventHandler callback);
    void detach();
}
