package d.project.dswebview.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JeongKuk on 2016-02-21.
 */
public class DBManager extends SQLiteOpenHelper {
    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE CONTENT_LIST(content_id TEXT PRIMARY KEY, desc TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<ContentVO> selectList() {
        SQLiteDatabase db = getReadableDatabase();
        List<ContentVO> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM CONTENT_LIST", null);
        while (cursor.moveToNext()) {

            ContentVO vo = new ContentVO();
            vo.setContentId(cursor.getString(0));
            vo.setDesc(cursor.getString(1));
            list.add(vo);
        }

        return list;
    }

    public void insert(String contentId, String desc) {
        String sql = "INSERT INTO CONTENT_LIST VALUES ('" + contentId +"', '" + desc +"')";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public void update(String contentId, String desc) {
        String sql = "UPDATE CONTENT_LIST SET DESC = '" + desc + "' WHERE CONTENT_ID = '" + contentId + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public void delete(String contentId) {
        String sql = "DELETE FROM CONTENT_LIST WHERE CONTENT_ID = '" + contentId + "'";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(sql);
        db.close();
    }
}
