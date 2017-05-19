package net.wequick.example.appstub.greendao.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;


import net.wequick.example.appstub.gendao.DaoMaster;
import net.wequick.example.appstub.gendao.DaoSession;
import net.wequick.example.appstub.greendao.SDCardContext;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * name:DBCore
 * func:数据库操作
 * author:
 * date:2017/2/08 10:00
 * copyright:jy
 */
public class DBCore {
    //默认数据库名称
    private static final String DEFAULT_DB_NAME = "small_test.db3";
    private static DaoMaster mDaoMaster;
    private static DaoSession mDaoSession;
    private static SQLiteDatabase mDb;
    private static Context mContext;
    private static String DB_NAME;

    /**
     * greendao 初始化
     *
     * @param mContext
     */
    public static void initialize(Context mContext) {
        initialize(mContext, DEFAULT_DB_NAME);
    }

    /**
     * greendao 初始化
     *
     * @param context
     * @param dbName
     */
    public static void initialize(Context context, String dbName) {
        if (context == null) {
            throw new IllegalArgumentException("当前上下文环境不能为空");
        }
        //mContext = context.getApplicationContext();
        SDCardContext sdcontext = new SDCardContext(context);
        mContext = sdcontext;
        DB_NAME = dbName;

        getDaoMaster();
        getDaoSession();
        enableQueryBuilderLog();
    }

    /**
     * 获取数据库管理者
     *
     * @return
     */
    public static DaoMaster getDaoMaster() {
        try {
            if (mDaoMaster == null) {
                //DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
                //mDb = helper.getWritableDatabase();
                //mDaoMaster = new DaoMaster(mDb);

                DBUpgradeOpenHelper dbUpgradeOpenHelper = new DBUpgradeOpenHelper(mContext, DB_NAME, null);
                mDb = dbUpgradeOpenHelper.getWritableDatabase();
                mDaoMaster = new DaoMaster(dbUpgradeOpenHelper.getWritableDatabase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mDaoMaster;
    }

    /**
     * 获取数据库会话
     *
     * @return
     */
    public static DaoSession getDaoSession() {
        if (mDaoSession == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster();
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }

    public static SQLiteDatabase getmDb() {
        return mDb;
    }

    public static void setmDb(SQLiteDatabase mDb) {
        DBCore.mDb = mDb;
    }

    public static void enableQueryBuilderLog() {
        QueryBuilder.LOG_SQL = false;
        QueryBuilder.LOG_VALUES = false;
        MigrationHelper.DEBUG = true;
    }
}
