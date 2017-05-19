package net.wequick.example.appstub.greendao.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * name:DBTools
 * func:数据库操作方法
 * author:
 * date:2017/2/08 10:00
 * copyright:jy
 */
public class DBTools {

    /**
     * 方法2：检查表中某列是否存在
     *
     * @param db
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    public static boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from sqlite_master where name = ? and sql like ?", new String[]{tableName, "%" + columnName + "%"});
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            Log.e("test", "checkColumnExists2..." + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                try {
                    cursor.close();
                    cursor = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }


    public static void DbToAddColumn(SQLiteDatabase db, String tableName, String columnName, String mDataType) {
        if (db == null) {
            return;
        }

        boolean ishasColum = checkColumnExists(db, tableName, columnName);//菜单代码
        if (!ishasColum) {
            String sql = "alter table " + tableName + " add COLUMN " + columnName + " " + mDataType + ";";
            db.execSQL(sql);
        }
    }
}
