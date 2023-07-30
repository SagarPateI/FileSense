package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenuSelect = menu.findItem(R.id.menu_select);
        mMenuSelect.setVisible(false);
        return super.onCreateOptionsMenu(menu);
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

}