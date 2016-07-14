package ch.supsi.dti.e_missionconsumes.carconnection;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.InputType;
import android.widget.EditText;

/**
 * Created by Niko on 7/4/2016.
 */
public class CarInfo {
    private static CarInfo instance;
    private final Context context;
    private CarDBHelper carDBHelper = null;

    private CarInfo(Context context) {
        this.context = context;
        this.carDBHelper = new CarDBHelper(context);
    }

    public static CarInfo getInstance() {
        if (instance == null) {
            throw new RuntimeException("CarInfo was not initialized!");
        }
        return instance;
    }

    public String getCarModel(String vin) {
        SQLiteDatabase db = null;
        Cursor c = null;
        try {
            db = this.carDBHelper.getReadableDatabase();
            c = db.rawQuery("select carModel from carStorage where vin =?;", new String[]{vin});
            if (c.getCount() > 0) {
                c.moveToFirst();
                return c.getString(0);
            }
            else {
                return "";
            }
        } finally {
            if (db != null) {
                db.close();
            }
            if (c != null) {
                c.close();
            }
        }
    }

    public void promptCarModel(final String vin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setTitle("Input your car model");

        // Set up the input
        final EditText input = new EditText(this.context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputModel = input.getText().toString();
                insertModel(inputModel, vin);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);

        //TODO: verify that runOnUiThread isn't required
        builder.show();
    }

    public boolean insertModel(String model, String vin) {
        ContentValues values = new ContentValues();
        values.put("vin", vin);
        values.put("carModel", model);
        return this.carDBHelper.getWritableDatabase().insert("carStorage", null, values) != -1L;
    }

    public static CarInfo init(Context context) {
        if (instance == null) {
            instance = new CarInfo(context);
        }
        return instance;
    }

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "carStorage";
        public static final String COLUMN_NAME_ENTRY_ID = "vin";
        public static final String COLUMN_NAME_TITLE = "carModel";
    }

    private static final String TEXT_TYPE = " VARCHAR";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" + FeedEntry._ID + TEXT_TYPE + " PRIMARY KEY," + FeedEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP + FeedEntry.COLUMN_NAME_TITLE + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public class CarDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "FeedReader.db";

        public CarDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
