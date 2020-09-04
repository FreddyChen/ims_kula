package com.freddy.kulaims.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class NetworkManager extends ConnectivityManager.NetworkCallback {

    private List<INetworkStateChangedObserver> mObservers = new ArrayList<>();

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static final class SingletonHolder {
        private static final NetworkManager INSTANCE = new NetworkManager();
    }

    private NetworkType networkType;

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        Log.d("NetworkManager", "onAvailable()");
        notifyObservers(true);
    }

    @Override
    public void onLost(@NonNull Network network) {
        Log.d("NetworkManager", "onLost()");
        notifyObservers(false);
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        Log.d("NetworkManager", "onCapabilitiesChanged()");
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                updateNetworkType(NetworkType.Wifi);
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                updateNetworkType(NetworkType.Cellular);
            } else {
                updateNetworkType(NetworkType.Other);
            }
        }
    }

    private void updateNetworkType(NetworkType type) {
        if (type == networkType) {
            return;
        }

        this.networkType = type;
    }

    public void registerObserver(Context context, INetworkStateChangedObserver observer) {
        if (context == null) {
            return;
        }

        if (observer == null) {
            return;
        }

        if (mObservers == null) {
            return;
        }

        if (mObservers.contains(observer)) {
            return;
        }

        NetworkRequest request = new NetworkRequest.Builder().build();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.registerNetworkCallback(request, this);

        mObservers.add(observer);
    }

    public void unregisterObserver(Context context, INetworkStateChangedObserver observer) {
        if (context == null) {
            return;
        }

        if (observer == null) {
            return;
        }

        if (mObservers == null) {
            return;
        }

        if (!mObservers.contains(observer)) {
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(this);

        mObservers.remove(observer);
    }

    public void notifyObservers(boolean available) {
        if (mObservers == null || mObservers.isEmpty()) {
            return;
        }

        for (INetworkStateChangedObserver observer : mObservers) {
            if (available) {
                observer.onNetworkAvailable();
            } else {
                observer.onNetworkUnavailable();
            }
        }
    }

    public interface INetworkStateChangedObserver {
        void onNetworkAvailable();
        void onNetworkUnavailable();
    }
}
