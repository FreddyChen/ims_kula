package com.freddy.kulaims;

import com.freddy.kulaims.config.CommunicationProtocol;
import com.freddy.kulaims.config.ImplementationMode;
import com.freddy.kulaims.interf.IMSInterface;
import com.freddy.kulaims.mina.tcp.MinaTCPIMS;
import com.freddy.kulaims.mina.websocket.MinaWebSocketIMS;
import com.freddy.kulaims.netty.tcp.NettyTCPIMS;
import com.freddy.kulaims.netty.websocket.NettyWebSocketIMS;
import com.freddy.kulaims.nio.tcp.NioTCPIMS;
import com.freddy.kulaims.nio.websocket.NioWebSocketIMS;

public class IMSFactory {

    public static IMSInterface getIMS(ImplementationMode implementationMode, CommunicationProtocol communicationProtocol) {
        switch (implementationMode) {
            case Nio: {
                switch (communicationProtocol) {
                    case TCP:
                        return NioTCPIMS.getInstance();

                    case WebSocket:
                        return NioWebSocketIMS.getInstance();

                    default:
                        break;
                }
                break;
            }

            case Mina: {
                switch (communicationProtocol) {
                    case TCP:
                        return MinaTCPIMS.getInstance();

                    case WebSocket:
                        return MinaWebSocketIMS.getInstance();

                    default:
                        break;
                }
                break;
            }

            case Netty:
            default:
                switch (communicationProtocol) {
                    case TCP:
                        return NettyTCPIMS.getInstance();

                    case WebSocket:
                        return NettyWebSocketIMS.getInstance();

                    default:
                        break;
                }
                break;
        }

        return null;
    }
}
