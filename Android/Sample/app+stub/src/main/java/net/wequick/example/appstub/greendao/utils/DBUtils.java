package net.wequick.example.appstub.greendao.utils;


import net.wequick.example.appstub.gendao.NoteDao;
import net.wequick.example.appstub.greendao.core.DBCore;
import net.wequick.example.appstub.greendao.core.DbTableService;

/**
 * name:DBTools
 * func:数据库操作方法
 * author:
 * date:2017/2/08 10:00
 * copyright:jy
 */
public class DBUtils {


    public static DbTableService getNoteDao() {
        NoteDao mNoteDao = DBCore.getDaoSession().getNoteDao();
        return new DbTableService(mNoteDao);
    }


}
