package com.example.baratillo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String databasename = "Signup.db";

    public DatabaseHelper(@Nullable Context context) {
        super(context, databasename, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        MyDatabase.execSQL("create Table allusers(email TEXT primary key, name TEXT, password TEXT)");
        MyDatabase.execSQL("create Table products(id INTEGER primary key autoincrement, user_email TEXT, name TEXT, price FLOAT, image_uri TEXT, FOREIGN KEY (user_email) REFERENCES allusers(email))");
        MyDatabase.execSQL("create Table transactions(trans_id INTEGER primary key autoincrement, user_email TEXT, amount FLOAT, discount FLOAT, total_amount FLOAT, paid_amount FLOAT, change FLOAT, date TEXT, time TEXT, FOREIGN KEY (user_email) REFERENCES allusers(email))");
        MyDatabase.execSQL("create Table transactionItems(iTrans_id INTEGER primary key, trans_id INTEGER, item_name TEXT, quantity INTEGER, price FLOAT, product FLOAT, FOREIGN KEY (trans_id) REFERENCES transactions(trans_id))");
    }


    @Override
    public void onUpgrade(SQLiteDatabase MyDatabase, int i, int i1) {
        MyDatabase.execSQL("drop Table if exists allusers");
    }

    public Boolean insertData(String email, String password, String name){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("name", name);
        contentValues.put("password", password);
        long result = MyDatabase.insert("allusers", null, contentValues);

        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Boolean checkEmail (String email) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from allusers where email = ?", new String[]{email});

        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean checkName (String name) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from allusers where name = ?", new String[]{name});

        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean checkEmailNamePassword(String email, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from allusers where email = ? and " + "password = ?", new String[]{email, password});

        if (cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public String getUserNameByEmail(String email) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT name FROM allusers WHERE email = ?", new String[]{email});

        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");

            if (nameIndex >= 0) {
                return cursor.getString(nameIndex);
            } else {
                // Handle the case where "name" column is not found
                return "NameColumnNotFound";
            }
        } else {
            // Handle the case where no rows are returned for the specified email
            return "NoRowsForEmail";
        }
    }

    public Boolean insertProduct(String userEmail, String name, Float price, String imageUri){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_email", userEmail);
        contentValues.put("name", name);
        contentValues.put("price", price);
        contentValues.put("image_uri", imageUri);
        long result = MyDatabase.insert("products", null, contentValues);

        if (result == -1){
            return false;
        } else {
            return true;
        }
    }

    public Cursor getProductsByUser(String userEmail) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        return MyDatabase.rawQuery("SELECT * FROM products WHERE user_email = ?", new String[]{userEmail});
    }

    // Method to delete a product from the database
    public boolean deleteProduct(int productId) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(productId)};

        // Log the delete query for debugging
        Log.d("DatabaseHelper", "Deleting product with ID: " + productId);

        int result = MyDatabase.delete("products", whereClause, whereArgs);

        // Log the result of the delete operation
        Log.d("DatabaseHelper", "Delete result: " + result);

        return result > 0;
    }


    // Method to update a product in the database
    public boolean updateProduct(int productId, String newName, Float newPrice, String imageUri) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", newName);
        contentValues.put("price", newPrice);
        contentValues.put("image_uri", imageUri);

        String whereClause = "id = ?";
        String[] whereArgs = {String.valueOf(productId)};

        int result = MyDatabase.update("products", contentValues, whereClause, whereArgs);
        return result > 0;
    }

    public List<String> getProductNames(String userEmail) {
        List<String> productNames = new ArrayList<>();
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT name FROM products WHERE user_email = ?", new String[]{userEmail});

        int nameIndex = cursor.getColumnIndex("name");

        if (nameIndex >= 0) {
            if (cursor.moveToFirst()) {
                do {
                    String productName = cursor.getString(nameIndex);
                    productNames.add(productName);
                } while (cursor.moveToNext());
            }
        } else {
            // Handle the case where "name" column is not found
            Log.e("DatabaseHelper", "Column 'name' not found in cursor.");
        }

        cursor.close();
        return productNames;
    }



    public double getProductPrice(String productName) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT price FROM products WHERE name = ?", new String[]{productName});

        double productPrice = 0.0;
        if (cursor.moveToFirst()) {
            productPrice = cursor.getDouble(cursor.getColumnIndex("price"));
        }

        Log.d("Debug", "Product Price: " + productPrice);

        cursor.close();
        return productPrice;
    }

    // Method to insert data into transactions table
    public boolean insertTransaction(String userEmail, double amount, double discount, double totalAmount,
                                     double paidAmount, double change, String date, String time) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_email", userEmail);
        contentValues.put("amount", amount);
        contentValues.put("discount", discount);
        contentValues.put("total_amount", totalAmount);
        contentValues.put("paid_amount", paidAmount);
        contentValues.put("change", change);
        contentValues.put("date", date);
        contentValues.put("time", time);

        long result = MyDatabase.insert("transactions", null, contentValues);

        return result != -1;
    }

    // Method to get the last inserted transaction ID for a specific user, date, and time
    public int getLastTransactionId(String userEmail, String date, String time) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT trans_id FROM transactions WHERE user_email = ? AND date = ? AND time = ?",
                new String[]{userEmail, date, time});

        int transactionId = -1;
        if (cursor.moveToLast()) {
            transactionId = cursor.getInt(cursor.getColumnIndex("trans_id"));
        }

        cursor.close();
        return transactionId;
    }

    // Method to insert data into transactionItems table
    public boolean insertTransactionItem(int transId, String itemName, int quantity, double price, double product) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("trans_id", transId);
        contentValues.put("item_name", itemName);
        contentValues.put("quantity", quantity);
        contentValues.put("price", price);
        contentValues.put("product", product);

        long result = MyDatabase.insert("transactionItems", null, contentValues);

        return result != -1;
    }

    public void exportTableAsCsv(String tableName, String userEmail) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();

        // Query to fetch data from the table
        String query = "SELECT * FROM " + tableName + " WHERE user_email = ?";
        Cursor cursor = MyDatabase.rawQuery(query, new String[]{userEmail});

        // Get column names
        String[] columnNames = cursor.getColumnNames();

        // Create CSV file
        String csvFileName = tableName + "_exp.csv";
        CsvFileWriter csvFileWriter = new CsvFileWriter(csvFileName, columnNames);

        // Write data to CSV
        while (cursor.moveToNext()) {
            List<String> rowData = new ArrayList<>();
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                rowData.add(cursor.getString(columnIndex));
            }
            csvFileWriter.writeRow(rowData);
        }

        // Close resources
        cursor.close();
        csvFileWriter.close();
    }

    public void exportTableAsCsvAllUsers(String tableName, String userEmail) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();

        // Query to fetch data from the table
        String query = "SELECT * FROM " + tableName + " WHERE email = ?";
        Cursor cursor = MyDatabase.rawQuery(query, new String[]{userEmail});

        // Get column names
        String[] columnNames = cursor.getColumnNames();

        // Create CSV file
        String csvFileName = tableName + "_exp.csv";
        CsvFileWriter csvFileWriter = new CsvFileWriter(csvFileName, columnNames);

        // Write data to CSV
        while (cursor.moveToNext()) {
            List<String> rowData = new ArrayList<>();
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                rowData.add(cursor.getString(columnIndex));
            }
            csvFileWriter.writeRow(rowData);
        }

        // Close resources
        cursor.close();
        csvFileWriter.close();
    }


    public void exportTransactionItemsAsCsv(String userEmail) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();

        // Fetch data from transactionItems based on user_email
        String query = "SELECT ti.* FROM transactionItems ti " +
                "JOIN transactions t ON ti.trans_id = t.trans_id " +
                "WHERE t.user_email = ?";
        Cursor cursor = MyDatabase.rawQuery(query, new String[]{userEmail});

        // Create CSV file, write data to CSV, and close resources
        String csvFileName = "transactionItems_exp.csv";
        String[] columnNames = cursor.getColumnNames();
        CsvFileWriter csvFileWriter = new CsvFileWriter(csvFileName, columnNames);

        // Write data to CSV
        while (cursor.moveToNext()) {
            List<String> rowData = new ArrayList<>();
            for (String columnName : columnNames) {
                int columnIndex = cursor.getColumnIndex(columnName);
                rowData.add(cursor.getString(columnIndex));
            }
            csvFileWriter.writeRow(rowData);
        }

        // Close the cursor for transactionItems
        cursor.close();
        csvFileWriter.close();
    }




}
