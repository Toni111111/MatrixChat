package com.example.matrixchat;

import android.app.Activity;
import android.app.Application;
import com.example.matrixchat.di.AppComponent;
import com.example.matrixchat.di.AppModule;
import com.example.matrixchat.di.DaggerAppComponent;
import com.example.matrixchat.utils.EventEmitter;
import org.matrix.androidsdk.MXSession;

import java.util.HashSet;
import java.util.Set;

public class MyApplication extends Application {

    private static MyApplication instance = null;

    private static AppComponent component;

    public static AppComponent getComponent() {
        return component;
    }

    private static final Set<MXSession> mSyncingSessions = new HashSet<>();

    private static Activity mCurrentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build(); // это   RetrofitComponent
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public static void removeSyncingSession(MXSession session) {
        if (null != session) {
            synchronized (mSyncingSessions) {
                mSyncingSessions.remove(session);
            }
        }
    }

    /**
     * Clear syncing sessions list
     */
    public static void clearSyncingSessions() {
        synchronized (mSyncingSessions) {
            mSyncingSessions.clear();
        }
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    /**
     * Tell if a session is syncing
     *
     * @param session the session
     * @return true if the session is syncing
     */
    public static boolean isSessionSyncing(MXSession session) {
        boolean isSyncing = false;

        if (null != session) {
            synchronized (mSyncingSessions) {
                isSyncing = mSyncingSessions.contains(session);
            }
        }

        return isSyncing;
    }

    private final EventEmitter<Activity> mOnActivityDestroyedListener = new EventEmitter<>();

    public EventEmitter<Activity> getOnActivityDestroyedListener() {
        return mOnActivityDestroyedListener;
    }


}
