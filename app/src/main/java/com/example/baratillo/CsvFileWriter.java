package com.example.baratillo;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvFileWriter {
    private FileWriter writer;

    public CsvFileWriter(String fileName, String[] columnNames) {
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsFolder, fileName);

            // Check if the file already exists
            if (file.exists()) {
                file.delete(); // Delete the existing file
            }

            writer = new FileWriter(file);

            // Write column names as the first row
            for (String columnName : columnNames) {
                writer.append(columnName).append(",");
            }
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CsvFileWriter", "Error creating FileWriter: " + e.getMessage());
        }
    }

    public void writeRow(List<String> rowData) {
        try {
            // Write data for each column
            for (String data : rowData) {
                writer.append(data).append(",");
            }
            writer.append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CsvFileWriter", "Error writing row: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CsvFileWriter", "Error closing FileWriter: " + e.getMessage());
        }
    }
}
