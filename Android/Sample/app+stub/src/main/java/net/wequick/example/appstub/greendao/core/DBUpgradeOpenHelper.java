package net.wequick.example.appstub.greendao.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import net.wequick.example.appstub.gendao.DaoMaster;

/**
 * name:DBUpgradeOpenHelper
 * func:数据库升级辅助操作
 * author:
 * date:2017/2/08 10:00
 * copyright:jy
 */
public class DBUpgradeOpenHelper extends DaoMaster.OpenHelper {
    public DBUpgradeOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
