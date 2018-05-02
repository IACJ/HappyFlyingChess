package com.flashminds.flyingchess.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.flashminds.flyingchess.Global;
import com.flashminds.flyingchess.manager.DataManager;
import com.flashminds.flyingchess.dataPack.DataPack;
import com.flashminds.flyingchess.R;
import com.flashminds.flyingchess.manager.SoundManager;
import com.flashminds.flyingchess.dataPack.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Edited by IACJ on 2018/4/18
 */

public class GameInfoActivity extends BaseActivity implements Target {
    Button createButton, joinButton, backButton;
    ListView roomListView;
    LinearLayout onlineLayout;
    SimpleAdapter roomListAdapter;
    LinkedList<HashMap<String, String>> roomListData;
    Worker worker;
    String roomId;
    int roomIndex;
    TextView title;
    LinkedList<String> roomUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ui setting
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hall);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);//Activity切换动画
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Global.activityManager.add(this);
        Global.soundManager.playMusic(SoundManager.BACKGROUND);

        // 查找 View
        createButton = (Button) findViewById(R.id.create);
        backButton = (Button) findViewById(R.id.back);
        joinButton = (Button) findViewById(R.id.join);
        roomListView = (ListView) findViewById(R.id.roomList);

        roomListData = new LinkedList<>();
        String[] t = {"room", "id", "player", "state"};
        int[] t2 = {R.id.roomName, R.id.roomId, R.id.player, R.id.roomState};
        roomListAdapter = new SimpleAdapter(getApplicationContext(), roomListData, R.layout.content_room_list_item, t, t2);
        roomListView.setAdapter(roomListAdapter);
        worker = new Worker();
        onlineLayout = (LinearLayout) findViewById(R.id.onlineLayout);
        roomId = "";
        roomIndex = -1;
        title = (TextView) findViewById(R.id.room_title);
        roomUUID = new LinkedList<>();

        // 点击事件
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//start a new game
                Global.soundManager.playSound(SoundManager.BUTTON);

                if (Global.dataManager.getGameMode() == DataManager.GM_WLAN) {
                    Global.socketManager.send(DataPack.R_ROOM_CREATE, Global.dataManager.getMyId(), Global.dataManager.getMyName() + "'s Room");
                } else if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {
                    Global.localServer.startHost();
                    Global.socketManager.connectToLocalServer();
                    Global.delay(500);
                    Global.socketManager.send(DataPack.R_LOGIN, new Build().MODEL, "123");
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                goBack();
            }
        });
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                roomId = roomListData.get(position).get("id");
                roomIndex = position;
                view.setSelected(true);
            }
        });
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                if (roomUUID.size() == 0){
                    Toast.makeText(GameInfoActivity.this,"未选择可进入的房间",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {
                    Global.socketManager.connectLanServer(Global.localServer.getRoomIp(roomUUID.get(roomIndex)));
                    Global.delay(500);
                    Global.socketManager.send(DataPack.R_LOGIN, new Build().MODEL, "123");
                } else {
                    boolean find = false;
                    synchronized (roomListData) {
                        for (HashMap<String, String> map : roomListData) {
                            if (map.get("id").compareTo(roomId) == 0) {
                                if (map.get("state").compareTo("waiting") == 0) {
                                    Global.socketManager.send(DataPack.R_ROOM_ENTER, Global.dataManager.getMyId(), roomId);
                                    find = true;
                                }
                                break;
                            }
                        }
                        if (!find)
                            Toast.makeText(getApplicationContext(), "join room failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //network init
        if (Global.dataManager.getGameMode() != DataManager.GM_LOCAL) {
            Global.socketManager.registerActivity(DataPack.A_ROOM_LOOKUP, this);
            Global.socketManager.registerActivity(DataPack.A_ROOM_CREATE, this);
            Global.socketManager.registerActivity(DataPack.A_ROOM_ENTER, this);
            new Thread(worker).start();
            if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {
                Global.localServer.registerMsg(this);
            }
        }
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        joinButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        createButton.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//返回按钮
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                goBack();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void goBack() {
        if (Global.dataManager.getGameMode() == DataManager.GM_WLAN) {
            Global.socketManager.send(DataPack.R_LOGOUT, Global.dataManager.getMyId());
        }
        startActivity(new Intent(getApplicationContext(), ChooseModeActivity.class));
        if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {
            Global.localServer.stop();
        }
    }

    @Override
    public void processDataPack(DataPack dataPack) {
        if (dataPack.getCommand() == DataPack.A_ROOM_LOOKUP) {
            synchronized (roomListData) {
                roomListData.clear();
                roomUUID.clear();
                for (int i = 0; i < dataPack.getMessageList().size(); ) {
                    HashMap<String, String> data = new HashMap<>();
                    String str = dataPack.getMessage(i);
                    roomUUID.addLast(str);
                    data.put("room", dataPack.getMessage(i + 1));
                    if (str.length() > 3) {
                        data.put("id", str.substring(str.length() - 3));
                    } else {
                        data.put("id", str);
                    }
                    data.put("player", dataPack.getMessage(i + 2));
                    if (dataPack.getMessage(i + 3).compareTo("0") == 0) {
                        data.put("state", "waiting");
                    } else {
                        data.put("state", "flying");
                    }
                    roomListData.addLast(data);
                    i += 4;
                }
                roomListView.post(new Runnable() {
                    @Override
                    public void run() {
                        roomListAdapter.notifyDataSetChanged();
                    }
                });
            }
        } else if (dataPack.getCommand() == DataPack.A_ROOM_CREATE) {
            if (dataPack.isSuccessful()) {
                Global.dataManager.setRoomId(dataPack.getMessage(0));
                Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
                ArrayList<String> msgs = new ArrayList<>();
                msgs.add(Global.dataManager.getMyId());
                msgs.add(Global.dataManager.getMyName());
                msgs.add(Global.dataManager.getOnlineScore());
                msgs.add("-1");
                intent.putStringArrayListExtra("msgs", msgs);
                startActivity(intent);//switch wo chess board activity
            } else {
                createButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "create room failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (dataPack.getCommand() == DataPack.A_ROOM_ENTER) {
            if (dataPack.isSuccessful()) {
                if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {
                    Global.dataManager.setMyId(dataPack.getMessage(0));
                }
                Global.dataManager.setRoomId(roomId);
                Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
                ArrayList<String> msgs = new ArrayList<>(dataPack.getMessageList());
                if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {//取出自己的ID
                    msgs.remove(0);
                }
                intent.putStringArrayListExtra("msgs", msgs);
                startActivity(intent);//switch wo chess board activity
            } else {
                createButton.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "join room failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    class Worker implements Runnable {
        private boolean exit;

        @Override
        public void run() {
            if (Global.dataManager.getGameMode() == DataManager.GM_WLAN) {
                DataPack dataPack = new DataPack(DataPack.R_ROOM_LOOKUP, null);
                Global.socketManager.send(dataPack);
            } else if (Global.dataManager.getGameMode() == DataManager.GM_LAN) {
                Global.localServer.updateRoomListImmediately();
            }
            System.out.println("卧槽");
        }
    }
}

