package com.example.riotshop.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.riotshop.models.Account;
import com.example.riotshop.models.Comment;
import com.example.riotshop.models.User;

import java.util.ArrayList;
import java.util.List;

public class DataSource {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DataSource(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // ========== Account Methods ===========
    public List<Account> getFilteredAccounts(String category, float minPrice, float maxPrice) {
        List<Account> accounts = new ArrayList<>();
        String selection = DatabaseHelper.COLUMN_ACCOUNT_PRICE + " >= ? AND " + DatabaseHelper.COLUMN_ACCOUNT_PRICE + " <= ?";
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(minPrice));
        selectionArgs.add(String.valueOf(maxPrice));

        if (category != null && !"Tất cả".equals(category)) {
            selection += " AND " + DatabaseHelper.COLUMN_ACCOUNT_CATEGORY + " = ?";
            selectionArgs.add(category);
        }

        Cursor cursor = database.query(DatabaseHelper.TABLE_ACCOUNTS, null, selection, selectionArgs.toArray(new String[0]), null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                accounts.add(cursorToAccount(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return accounts;
    }

    public List<Account> searchAccountsByName(String query) {
        List<Account> accounts = new ArrayList<>();
        String selection = DatabaseHelper.COLUMN_ACCOUNT_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        Cursor cursor = database.query(DatabaseHelper.TABLE_ACCOUNTS, null, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                accounts.add(cursorToAccount(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return accounts;
    }
    
    public Account getAccountById(String accountId) {
        Account account = null;
        Cursor cursor = database.query(DatabaseHelper.TABLE_ACCOUNTS, null, DatabaseHelper.COLUMN_ID + " = ?", new String[]{accountId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            account = cursorToAccount(cursor);
            cursor.close();
        }
        return account;
    }

    // ========== Comment Methods ===========
    public long addComment(String accountId, String userId, String userName, float rating, String text) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ACCOUNT_ID, accountId);
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_COMMENT_USER_NAME, userName);
        values.put(DatabaseHelper.COLUMN_COMMENT_RATING, rating);
        values.put(DatabaseHelper.COLUMN_COMMENT_TEXT, text);
        values.put(DatabaseHelper.COLUMN_COMMENT_TIMESTAMP, System.currentTimeMillis());
        return database.insert(DatabaseHelper.TABLE_COMMENTS, null, values);
    }

    public List<Comment> getCommentsForAccount(String accountId) {
        List<Comment> comments = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_COMMENTS, null, DatabaseHelper.COLUMN_ACCOUNT_ID + " = ?", new String[]{accountId}, null, null, DatabaseHelper.COLUMN_COMMENT_TIMESTAMP + " DESC");

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                comments.add(cursorToComment(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return comments;
    }

    // ========== Favorite Methods ===========
    public void addFavorite(String userId, String accountId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_ACCOUNT_ID, accountId);
        database.insertWithOnConflict(DatabaseHelper.TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeFavorite(String userId, String accountId) {
        database.delete(DatabaseHelper.TABLE_FAVORITES, DatabaseHelper.COLUMN_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_ACCOUNT_ID + " = ?", new String[]{userId, accountId});
    }

    public List<Account> getFavoriteAccounts(String userId) {
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT A.* FROM " + DatabaseHelper.TABLE_ACCOUNTS + " A INNER JOIN " + DatabaseHelper.TABLE_FAVORITES + " F ON A."
                + DatabaseHelper.COLUMN_ID + " = F." + DatabaseHelper.COLUMN_ACCOUNT_ID + " WHERE F." + DatabaseHelper.COLUMN_USER_ID + " = ?";
        Cursor cursor = database.rawQuery(query, new String[]{userId});

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                accounts.add(cursorToAccount(cursor));
                cursor.moveToNext();
            }
            cursor.close();
        }
        return accounts;
    }


    // ========== Cursor to Object Mappers ===========
    private Account cursorToAccount(Cursor cursor) {
        // Assuming column order matches DatabaseHelper constants
        return new Account(
                String.valueOf(cursor.getInt(0)),
                cursor.getString(1),
                cursor.getLong(2),
                cursor.getString(3),
                cursor.getInt(4),
                cursor.getFloat(5),
                cursor.getString(6),
                cursor.getInt(7),
                cursor.getInt(8)
        );
    }

    private Comment cursorToComment(Cursor cursor) {
        // ... implementation for converting cursor to Comment object
        return new Comment();
    }
}
