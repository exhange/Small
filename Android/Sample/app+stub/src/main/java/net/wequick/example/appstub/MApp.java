package net.wequick.example.appstub;

import android.app.Application;

import net.wequick.example.appstub.gendao.DaoMaster;
import net.wequick.example.appstub.gendao.DaoSession;
import net.wequick.example.appstub.greendao.core.DBCore;

import org.greenrobot.greendao.database.Database;

public abstract class MApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DBCore.initialize(this);
        initAppData();
    }

    public abstract void initAppData();
}
