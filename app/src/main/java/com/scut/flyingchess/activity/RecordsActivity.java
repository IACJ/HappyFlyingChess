package com.scut.flyingchess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.scut.flyingchess.activity.replay.ReplayGameActivity;
import com.scut.flyingchess.Global;
import com.scut.flyingchess.R;
import com.scut.flyingchess.manager.SoundManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Edited by IACJ on 2018/4/9.
 *
 * 弹出式窗口，管理回放记录。
 */
public class RecordsActivity extends BaseActivity {
    ListView recordList;
    ArrayList<String> records;
    ArrayAdapter<String> recordsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Global.soundManager.playMusic(SoundManager.BACKGROUND);

        recordList = (ListView) findViewById(R.id.recordList);
        records = searchRecords(Global.replayManager.PATH);
        recordsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, records);
        if (records.isEmpty()) {
            Toast.makeText(getApplicationContext(), "没有回放记录!", Toast.LENGTH_SHORT).show();
        } else {
            recordList.setAdapter(recordsAdapter);
            recordList.setOnItemClickListener(onItemClickLis);
            recordList.setOnItemLongClickListener(onItemLongClickLis);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Global.soundManager.resumeMusic(SoundManager.BACKGROUND);
    }

    @Override
    public void onStop() {
        super.onStop();
        Global.soundManager.pauseMusic();
    }


    AdapterView.OnItemClickListener onItemClickLis = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String fileName = ((TextView) view).getText().toString();
            Global.replayManager.openFile(Global.replayManager.PATH + fileName);
            Global.replayManager.startReplay();
            Global.playersData.clear();
            int playNum = Global.replayManager.getPlayerNum();
            for (int i = 0; i < playNum; i++) {
                Global.playersData.put(Global.replayManager.getSavedKey(), Global.replayManager.getSavedRole());
            }
            Global.dataManager.setMyId("0");
            startActivity(new Intent(getApplicationContext(), ReplayGameActivity.class));
        }
    };

    AdapterView.OnItemLongClickListener onItemLongClickLis = new AdapterView.OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            deleteFile(view, position);
            return true;
        }
    };

    private ArrayList<String> searchRecords(String path) {
        ArrayList<String> records = new ArrayList<>();
        File dir = new File(path);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.getName().endsWith(".record")){
                    records.add(0,file.getName());
                }
            }
        }

        return records;
    }

    public void deleteFile(final View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(this, view);
        // menu布局
        popupMenu.getMenuInflater().inflate(R.menu.menu_delete_record, popupMenu.getMenu());
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String fileName = ((TextView) view).getText().toString();
                File file = new File(Global.replayManager.PATH + fileName);
                file.delete();
                records.remove(pos);
                recordsAdapter.notifyDataSetChanged();
                return false;
            }
        });

        popupMenu.show();
    }
}
