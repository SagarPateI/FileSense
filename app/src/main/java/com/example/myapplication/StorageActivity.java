package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class StorageActivity extends AppCompatActivity implements StoreCallBack {
    private RecyclerView recyclerView;
    private TextView noFilesText;
    private TextView storageUsageTextView;

    private AdapterActivity mAdapter;
    private File mRootFile;

    private MenuItem mMenuSelect;

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
        long rootFolderSize = getFolderSize(mRootFile);
        storageUsageTextView.setText("Storage Usage: " + formatSize(rootFolderSize));
    }

    private void setupRecyclerView(File rootFile, File[] filesAndFolders) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mAdapter = new AdapterActivity(getApplicationContext(), rootFile, filesAndFolders, this, builder);
        recyclerView.setAdapter(mAdapter);
    }

    private String formatSize(long size) {
        return Formatter.formatFileSize(this, size);
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

    private long getFolderSize(File folder) {
        long size = 0;
        if (folder != null && folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getFolderSize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        }
        return size;
    }
}
