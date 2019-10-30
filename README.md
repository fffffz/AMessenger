# AMessenger
[AMessenger](https://github.com/fffffz/AMessenger) 是一个有跨进程能力的 Handler，特点是非常轻巧。


### 使用方法
1、初始化
```
AMessenger.getInstance().init(this);
```

2、每个进程注册要处理的事件
````
if (Util.isMainProcess(this)) {
    AMessenger.getInstance().register(new MainMessengerHandler(), 111);
} else if (Util.isPlayerProcess(this)) {
    AMessenger.getInstance().register(new PlayerMessengerHandler(), 222, 333);
} else if (Util.isDownloadProcess(this)) {
    AMessenger.getInstance().register(new DownloadMessengerHandler(), 222, 333);
}
````

[AMessenger.java]
````
public void register(AHandler handler, int... whats)
````
每个进程可以注册任意个 AHandler（通常一个就够了） 和 what（通常会有多个）

3、发送消息
````
AMessage aMessage = new AMessage(222);
aMessage.putParcelable("user", user);
AMessenger.getInstance().sendMessage(aMessage);

AMessenger.getInstance().sendEmptyMessage(333);
````

4、处理消息
````
public class PlayerMessengerHandler extends AHandler {
    @Override
    public void handleMessage(AMessage aMessage) {
        if (aMessage.what == 222) {
            Log.d("AMessenger", "log in " + aMessage.getParcelable("user", User.CREATOR));
        } else if (aMessage.what == 333) {
            Log.d("AMessenger", "log out");
        }
    }
}
````


### 原理
很显然是基于 Messenger 实现的 
1、AMessenger init 时每个进程都会创建一个 Messenger 
2、非主进程 bind 主进程的 HubService，onBind 返回主进程的 Messenger 
3、在 onServiceConnected 中把自己的 Messenger 通过主进程的 Messenger 发给主进程（sendMsg0），这样主进程就拥有所有进程的 Messenger 
4、之后所有进程之间的消息都先发给主进程再进行转发 
````
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
````
