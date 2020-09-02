package com.freddy.kulaims.config;

public enum IMSConnectStatus {
    Unconnected(0, "未连接"),
    Connecting(1, "连接中"),
    Connected(2, "连接成功"),
    ConnectFailed(-100, "连接失败"),
    ConnectFailed_IMSClosed(-101, "连接失败：IMS已关闭"),
    ConnectFailed_ServerListEmpty(-102, "连接失败：服务器地址列表为空"),
    ConnectFailed_ServerEmpty(-103, "连接失败：服务器地址为空"),
    ConnectFailed_ServerIllegitimate(-104, "连接失败：服务器地址不合法"),
    ConnectFailed_NetworkUnavailable(-105, "连接失败：网络不可用");

    private int errCode;
    private String errMsg;
    IMSConnectStatus(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
