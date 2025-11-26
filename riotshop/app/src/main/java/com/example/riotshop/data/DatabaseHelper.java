package com.example.riotshop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "riotshop.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ACCOUNTS = "accounts";
    public static final String TABLE_COMMENTS = "comments";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_CART = "cart";

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";

    // USERS Table
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_NAME = "username";

    // ACCOUNTS Table
    public static final String COLUMN_ACCOUNT_NAME = "name";
    public static final String COLUMN_ACCOUNT_PRICE = "price";
    public static final String COLUMN_ACCOUNT_CATEGORY = "category";
    public static final String COLUMN_ACCOUNT_IMAGE_RES = "image_res_id";
    public static final String COLUMN_ACCOUNT_RATING = "rating";
    public static final String COLUMN_ACCOUNT_RANK = "rank";
    public static final String COLUMN_ACCOUNT_SKINS = "skins";
    public static final String COLUMN_ACCOUNT_CHAMPIONS = "champions";

    // COMMENTS Table
    public static final String COLUMN_COMMENT_RATING = "rating";
    public static final String COLUMN_COMMENT_TEXT = "text";
    public static final String COLUMN_COMMENT_TIMESTAMP = "timestamp";
    public static final String COLUMN_COMMENT_USER_NAME = "user_name";

    // CART Table
    public static final String COLUMN_CART_QUANTITY = "quantity";

    // CREATE TABLE statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_USER_NAME + " TEXT"
            + ");";

    private static final String CREATE_TABLE_ACCOUNTS = "CREATE TABLE " + TABLE_ACCOUNTS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ACCOUNT_NAME + " TEXT NOT NULL, "
            + COLUMN_ACCOUNT_PRICE + " REAL NOT NULL, "
            + COLUMN_ACCOUNT_CATEGORY + " TEXT, "
            + COLUMN_ACCOUNT_IMAGE_RES + " INTEGER, "
            + COLUMN_ACCOUNT_RATING + " REAL DEFAULT 0, "
            + COLUMN_ACCOUNT_RANK + " TEXT, "
            + COLUMN_ACCOUNT_SKINS + " INTEGER, "
            + COLUMN_ACCOUNT_CHAMPIONS + " INTEGER"
            + ");";

    private static final String CREATE_TABLE_COMMENTS = "CREATE TABLE " + TABLE_COMMENTS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ACCOUNT_ID + " INTEGER NOT NULL, "
            + COLUMN_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_COMMENT_USER_NAME + " TEXT, "
            + COLUMN_COMMENT_RATING + " REAL NOT NULL, "
            + COLUMN_COMMENT_TEXT + " TEXT, "
            + COLUMN_COMMENT_TIMESTAMP + " INTEGER NOT NULL, "
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ");";

    private static final String CREATE_TABLE_FAVORITES = "CREATE TABLE " + TABLE_FAVORITES + "("
            + COLUMN_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_ACCOUNT_ID + " INTEGER NOT NULL, "
            + "PRIMARY KEY (" + COLUMN_USER_ID + ", " + COLUMN_ACCOUNT_ID + "),"
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ")"
            + ");";

    private static final String CREATE_TABLE_CART = "CREATE TABLE " + TABLE_CART + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_ID + " INTEGER NOT NULL, "
            + COLUMN_ACCOUNT_ID + " INTEGER NOT NULL, "
            + COLUMN_CART_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
            + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNTS + "(" + COLUMN_ID + ")"
            + ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ACCOUNTS);
        db.execSQL(CREATE_TABLE_COMMENTS);
        db.execSQL(CREATE_TABLE_FAVORITES);
        db.execSQL(CREATE_TABLE_CART);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNTS);
        onCreate(db);
    }
}
