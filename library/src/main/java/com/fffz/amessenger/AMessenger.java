package com.fffz.amessenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class AMessenger {

    private static final AMessenger INSTANCE = new AMessenger();
    private Context mContext;
    private boolean mIsMainProcess;
    private Messenger mMessenger;
    private Messenger mHubMessenger;
    private Map<Integer, Set<AHandler>> mWhatHandlersMap = new HashMap<>();
    private Map<Handler, int[]> mHandlerWhatsMap = new HashMap<>();
    private int mPid;
    private boolean mIsBindingHub;
    private ServiceConnection mServiceConnection;
    private LinkedList<AMessage> mPendingMessages = new LinkedList<>();

    public static AMessenger getInstance() {
        return INSTANCE;
    }

    public synchronized void register(AHandler handler, int... whats) {
        mHandlerWhatsMap.put(handler, whats);
        for (int what : whats) {
            Set<AHandler> handlers = mWhatHandlersMap.get(what);
            if (handlers == null) {
                handlers = new HashSet<>();
                mWhatHandlersMap.put(what, handlers);
            }
            handlers.add(handler);
        }
        sendMsg0();
    }

    public synchronized void unregister(AHandler handler) {
        int[] whats = mHandlerWhatsMap.remove(handler);
        if (whats == null) {
            return;
        }
        for (int what : whats) {
            Set<AHandler> handlers = mWhatHandlersMap.get(what);
            if (handlers.remove(handler) && handlers.size() == 0) {
                mWhatHandlersMap.remove(what);
            }
        }
        sendMsg0();
    }

    public synchronized void init(Context context) {
        mContext = context.getApplicationContext();
        mIsMainProcess = Util.isMainProcess(context);
        mPid = android.os.Process.myPid();
        if (mMessenger != null) {
            return;
        }
        HandlerThread handlerThread = new HandlerThread("AMessenger");
        handlerThread.start();
        if (mIsMainProcess) {
            mMessenger = new Messenger(new Handler(handlerThread.getLooper()) {
                @Override
                public void dispatchMessage(Message msg) {
                    if (msg.what == Integer.MIN_VALUE) {
                        int[] whats = msg.getData().getIntArray("whats");
                        int pid = msg.getData().getInt("pid");
                        if (whats == null) {
                            Iterator<Map<Integer, Messenger>> iterator = mWhatMessengersMap.values().iterator();
                            Map<Integer, Messenger> pidMessengerMap;
                            while ((pidMessengerMap = iterator.next()) != null) {
                                if (pidMessengerMap.remove(pid) != null && pidMessengerMap.size() == 0) {
                                    iterator.remove();
                                }
                            }
                            return;
                        }
                        for (int what : whats) {
                            Map<Integer, Messenger> pidMessengerMap = mWhatMessengersMap.get(what);
                            if (pidMessengerMap == null) {
                                pidMessengerMap = new HashMap<>();
                                mWhatMessengersMap.put(what, pidMessengerMap);
                            }
                            pidMessengerMap.put(pid, msg.replyTo);
                        }
                        return;
                    }
                    Set<AHandler> handlers = mWhatHandlersMap.get(msg.what);
                    if (handlers != null) {
                        for (Handler handler : handlers) {
                            handler.dispatchMessage(msg);
                        }
                    }
                    int sendingPid = msg.getData().getInt("com.ximalaya.ting.amessenger_sendingPid");
                    Map<Integer, Messenger> pidMessengerMap = mWhatMessengersMap.get(msg.what);
                    if (pidMessengerMap == null) {
                        return;
                    }
                    for (Map.Entry<Integer, Messenger> entry : pidMessengerMap.entrySet()) {
                        if (entry.getKey() == sendingPid) {
                            continue;
                        }
                        try {
                            entry.getValue().send(msg);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return;
        }
        Handler handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void dispatchMessage(Message msg) {
                Set<AHandler> handlers = mWhatHandlersMap.get(msg.what);
                if (handlers == null) {
                    return;
                }
                final Message copy = new Message();
                copy.copyFrom(msg);
                for (final Handler handler : handlers) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handler.dispatchMessage(copy);
                        }
                    });
                }
            }
        };
        mMessenger = new Messenger(handler);
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized (AMessenger.this) {
                    mHubMessenger = new Messenger(service);
                    mIsBindingHub = false;
                    sendMsg0();
                    for (AMessage aMessage : mPendingMessages) {
                        try {
                            mHubMessenger.send(aMessage.obtainMessage());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mPendingMessages.clear();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                synchronized (AMessenger.this) {
                    mHubMessenger = null;
                    bindHub();
                }
            }
        };
        bindHub();
    }

    private void bindHub() {
        mIsBindingHub = true;
        Intent intent = new Intent(mContext, HubService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void sendMsg0() {
        if (mIsMainProcess || mHubMessenger == null) {
            return;
        }
        Message msg0 = Message.obtain();
        msg0.what = Integer.MIN_VALUE;
        msg0.replyTo = mMessenger;
        msg0.getData().putInt("pid", mPid);
        msg0.getData().putIntArray("whats", getWhats());
        try {
            mHubMessenger.send(msg0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int[] getWhats() {
        if (mWhatHandlersMap.size() == 0) {
            return null;
        }
        int[] whats = new int[mWhatHandlersMap.size()];
        int i = 0;
        for (int key : mWhatHandlersMap.keySet()) {
            whats[i] = key;
            ++i;
        }
        return whats;
    }

    public void sendEmptyMessage(int what) {
        AMessage aMessage = new AMessage(what);
        sendMessage(aMessage);
    }

    public synchronized void sendMessage(AMessage aMessage) {
        if (mIsMainProcess) {
            Message msg = aMessage.obtainMessage();
            Map<Integer, Messenger> pidMessengerMap = mWhatMessengersMap.get(msg.what);
            if (pidMessengerMap == null) {
                return;
            }
            for (Map.Entry<Integer, Messenger> entry : pidMessengerMap.entrySet()) {
                try {
                    entry.getValue().send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return;
        }
        aMessage.putInt("com.ximalaya.ting.amessenger_sendingPid", mPid);
        if (mHubMessenger == null) {
            mPendingMessages.add(aMessage);
            if (!mIsBindingHub) {
                bindHub();
            }
            return;
        }
        try {
            mHubMessenger.send(aMessage.obtainMessage());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    Messenger getMessenger() {
        return mMessenger;
    }

    private Map<Integer, Map<Integer, Messenger>> mWhatMessengersMap = new HashMap<>();

}