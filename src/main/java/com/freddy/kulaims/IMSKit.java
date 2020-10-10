package com.freddy.kulaims;

import android.content.Context;
import android.util.Log;

import com.freddy.kulaims.config.CommunicationProtocol;
import com.freddy.kulaims.config.IMSOptions;
import com.freddy.kulaims.config.ImplementationMode;
import com.freddy.kulaims.config.TransportProtocol;
import com.freddy.kulaims.interf.IMSInterface;
import com.freddy.kulaims.listener.IMSConnectStatusListener;
import com.freddy.kulaims.listener.IMSMsgReceivedListener;

/**
 * IMS核心类
 */
public class IMSKit {

    private static final String TAG = "FreddyChen";// todo 不知道为什么tag为IMSKit的时候，Logcat无法打印日志
    private IMSInterface ims;

    private IMSKit() {
    }

    public static IMSKit getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        private static final IMSKit INSTANCE = new IMSKit();
    }

    public boolean init(Context context, IMSOptions options, IMSConnectStatusListener connectStatusListener, IMSMsgReceivedListener msgReceivedListener) {
        Log.d(TAG, "IMSKit初始化开始");
        if (context == null) {
            Log.w(TAG, "IMSKit初始化失败：Context 为 null");
            return false;
        }

        if (options == null) {
            Log.w(TAG, "IMSKit初始化失败：IMSOptions 为 null");
            return false;
        }

        ImplementationMode implementationMode = options.getImplementationMode();
        if (implementationMode == null) {
            Log.w(TAG, "IMSKit初始化失败：ImplementationMode 为 null");
            return false;
        }

        CommunicationProtocol communicationProtocol = options.getCommunicationProtocol();
        if (communicationProtocol == null) {
            Log.w(TAG, "IMSKit初始化失败：CommunicationProtocol 为 null");
            return false;
        }

        TransportProtocol transportProtocol = options.getTransportProtocol();
        if (transportProtocol == null) {
            Log.w(TAG, "IMSKit初始化失败：TransportProtocol 为 null");
            return false;
        }

        ims = IMSFactory.getIMS(implementationMode, communicationProtocol);
        if (ims == null) {
            Log.w(TAG, "IMSKit初始化失败：ims 为 null");
            return false;
        }

        boolean initialized = ims.init(context, options, connectStatusListener, msgReceivedListener);
        if (!initialized) {
            Log.w(TAG, "IMSKit初始化失败：请查看 " + ims.getClass().getSimpleName() + " 相关的日志");
            return false;
        }

        Log.d(TAG, "IMSKit初始化完成\nims = " + ims.getClass().getSimpleName() + "\noptions = " + options);
        return true;
    }

    public void connect() {
        if (ims == null) {
            Log.d(TAG, "IMSKit启动失败");
            return;
        }

        ims.connect();
    }

    public void disconnect() {
        if(ims == null) return;
        ims.release();
    }
}
