package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.math.RoundingMode;

public class StorageActivity extends AppCompatActivity implements StoreCallBack {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private TextView noFilesText;
    private TextView storageUsageTextView;

    private AdapterActivity mAdapter;
    private File mRootFile;

    private MenuItem mMenuSelect;

    private static final String title_storage = "Storage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        recyclerView = findViewById(R.id.recycler_view);
        noFilesText = findViewById(R.id.empty_text);
        storageUsageTextView = findViewById(R.id.storage_usage_text_view);

        String path = getIntent().getStringExtra("path");
        mRootFile = new File(path);

        File[] filesAndFolders = mRootFile.listFiles();

        if (filesAndFolders == null || filesAndFolders.length == 0) {
            noFilesText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noFilesText.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            setupRecyclerView(mRootFile, filesAndFolders);
        }

        // Calculate and display storage usage for the root folder
        long rootFolderSize = calculateStorageUsage(mRootFile);
        if (rootFolderSize != 0) {
            storageUsageTextView.setText("Storage Usage: " + formatSize(rootFolderSize));
        } else {
            storageUsageTextView.setText("Storage Usage: N/A");
        }
    }

    private long calculateStorageUsage(File rootFile) {
        long size = 0;
        if (rootFile != null && rootFile.exists()) {
            File[] files = rootFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += calculateStorageUsage(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }

    private void setupRecyclerView(File rootFile, File[] filesAndFolders) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mAdapter = new AdapterActivity(getApplicationContext(), rootFile, filesAndFolders, this, builder);
        recyclerView.setAdapter(mAdapter);
    }

    // This method will format file sizes in kilobytes, megabytes, or gigabytes, depending on their size.
    private String formatSize(long size) {
        if (size < 1024) {
            return String.valueOf(size);
        } else if (size < 1048576) {
            return String.format("%.2fk", size / 1024.0); // 1 k = 1024 bytes
        } else if (size < 1073741824) {
            return String.format("%.2fM", size / 1048576.0); // 1 M = 1048576 bytes
        } else {
            return String.format("%.2fG", size / 1073741824.0); // 1 GB = 1073741824 bytes
        }
    }

    @Override
    public void startChooseDestination() {
        mMenuSelect.setVisible(true);
        updateTitle(getString(R.string.title_start_choose_destination));
    }

    private void updateTitle(String newTitle) {
        getSupportActionBar().setTitle(newTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //Add the storage usage item to the menu
        MenuItem storageUsageItem = menu.findItem(R.id.menu_storage_usage);
        storageUsageItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showStorageUsage();
                return true;
            }
        });

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_select) {
            mAdapter.moveFileToSelectedFolder();
            // Hide menu select
            mMenuSelect.setVisible(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null) {
            if (!mAdapter.goBack(mRootFile)) {
                super.onBackPressed();
            }
        }
    }

    private void showStorageUsage() {

        //Get the total storage
        long totalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getTotalSpace();

        //Get the used storage
        long usedStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getUsableSpace();

        //Get the free storage
        long freeStorage = totalStorage - usedStorage;

        //Inflate the storage_usage.xml layout file
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View storageUsageView = inflater.inflate(R.layout.storage_usage, null);

        //Get the storage usage text view
        TextView storageUsageTextView = storageUsageView.findViewById(R.id.storage_usage_text_view);

        //Test text for the popup window
        // storageUsageTextView.setText("Total Storage: 100 GB\nUsed Storage: 50 GB\nFree Storage: 50 GB");

        //Convert the storage usage to gigabytes with more precision
        BigDecimal totalStorageInGB = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            totalStorageInGB = new BigDecimal(totalStorage).divide(new BigDecimal(1024 * 1024 * 1024), 10, RoundingMode.HALF_UP.ordinal());
        }
        BigDecimal usedStorageInGB = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            usedStorageInGB = new BigDecimal(usedStorage).divide(new BigDecimal(1024 * 1024 * 1024), 10, RoundingMode.HALF_UP.ordinal());
        }
        BigDecimal freeStorageInGB = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            freeStorageInGB = new BigDecimal(freeStorage).divide(new BigDecimal(1024 * 1024 * 1024), 10, RoundingMode.HALF_UP.ordinal());
        }

        //Show the storage usage
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Storage Usage");
        builder.setMessage("Total Storage: " + totalStorageInGB + " GB\nUsed Storage: " + usedStorageInGB + " GB\nFree Storage: " + freeStorageInGB + " GB");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

}