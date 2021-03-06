package com.scut.flyingchess.activity.wanGame;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.scut.flyingchess.Global;
import com.scut.flyingchess.R;
import com.scut.flyingchess.activity.BaseActivity;
import com.scut.flyingchess.dataPack.DataPack;
import com.scut.flyingchess.dataPack.Target;
import com.scut.flyingchess.entity.Role;
import com.scut.flyingchess.manager.SoundManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class WanRoomActivity extends BaseActivity implements Target {

    Button startButton, backButton, site[], addRobotButton[];
    int[] siteState;// -1 none   0 robot    1 people
    ListView idlePlayerView;
    LinkedList<HashMap<String, String>> idlePlayerListData;
    SimpleAdapter idlePlayerListAdapter;
    TextView title;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);//Activity切换动画
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Global.activityManager.add(this);
        Global.soundManager.playMusic(SoundManager.BACKGROUND);
        //init
        startButton = (Button) findViewById(R.id.start);
        backButton = (Button) findViewById(R.id.back);
        site = new Button[4];
        site[0] = (Button) findViewById(R.id.R);
        site[1] = (Button) findViewById(R.id.G);
        site[2] = (Button) findViewById(R.id.B);
        site[3] = (Button) findViewById(R.id.Y);
        addRobotButton = new Button[4];
        addRobotButton[0] = (Button) findViewById(R.id.jr);
        addRobotButton[1] = (Button) findViewById(R.id.jg);
        addRobotButton[2] = (Button) findViewById(R.id.jb);
        addRobotButton[3] = (Button) findViewById(R.id.jy);
        siteState = new int[4];
        idlePlayerView = (ListView) findViewById(R.id.playerInRoom);
        idlePlayerListData = new LinkedList<>();
        idlePlayerListAdapter = new SimpleAdapter(getApplicationContext(), idlePlayerListData, R.layout.content_player_list_item, new String[]{"name", "score"}, new int[]{R.id.nameInRoom, R.id.scoreInRoom});
        idlePlayerView.setAdapter(idlePlayerListAdapter);
        title = (TextView) findViewById(R.id.title);
        //trigger
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                if (idlePlayerListData.size() > 1)
                    Toast.makeText(getApplicationContext(), "some one is not ready!", Toast.LENGTH_SHORT).show();
                else {


                    if (Global.dataManager.getHostId().compareTo(Global.dataManager.getMyId()) != 0) {
                        Toast.makeText(getApplicationContext(), "wait for room host", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Global.socketManager.send(DataPack.R_GAME_START, Global.dataManager.getMyId(), Global.dataManager.getRoomId());

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);

                Global.socketManager.send(DataPack.R_ROOM_EXIT, Global.dataManager.getMyId(), Global.dataManager.getRoomId(), Global.playersData.get(Global.dataManager.getMyId()).color);
                startActivity(new Intent(getApplicationContext(), WanHallActivity.class));


            }
        });

        site[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                chooseSite(0);
            }
        });

        site[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                chooseSite(1);
            }
        });

        site[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                chooseSite(2);
            }
        });

        site[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                chooseSite(3);
            }
        });

        addRobotButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRobot(0);
            }
        });

        addRobotButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRobot(1);
            }
        });

        addRobotButton[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRobot(2);
            }
        });

        addRobotButton[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRobot(3);
            }
        });
        //internet init

        Global.socketManager.registerActivity(DataPack.E_ROOM_POSITION_SELECT, this);
        Global.socketManager.registerActivity(DataPack.E_GAME_START, this);
        Global.socketManager.registerActivity(DataPack.A_ROOM_ENTER, this);
        Global.socketManager.registerActivity(DataPack.E_ROOM_ENTER, this);
        Global.socketManager.registerActivity(DataPack.A_ROOM_EXIT, this);
        Global.socketManager.registerActivity(DataPack.E_ROOM_EXIT, this);

        //setting
        siteState[0] = -1;
        siteState[1] = -1;
        siteState[2] = -1;
        siteState[3] = -1;
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "Name");
        map.put("score", "Score");
        idlePlayerListData.addLast(map);
        Global.playersData.clear();
        Bundle bundle = getIntent().getExtras();
        ArrayList<String> players = bundle.getStringArrayList("msgs");
        //添加玩家数据
        Global.dataManager.setHostId(players.get(0));
        //Global.playersData.put(players.get(0), new Role(players.get(0), players.get(1), players.get(2), Integer.valueOf(players.get(3)), Role.PLAYER, true));
        Global.playersData.put(players.get(0), new Role(players.get(0), players.get(1), "0", Integer.valueOf(players.get(2)), Role.PLAYER, true));
        for (int i = 3; i < players.size(); ) {
            System.out.println(players.get(i) + " " + players.get(i+1) + " " + players.get(i+2));
            int type = (Integer.valueOf(players.get(i)) < 0) ? Role.AI : Role.PLAYER;
            Global.playersData.put(players.get(i), new Role(players.get(i), players.get(i + 1), "0", Integer.valueOf(players.get(i + 2)), type, false));
            i += 3;
        }
        Global.playersData.get(Global.dataManager.getMyId()).type = Role.ME;

        for (int i = 0; i < players.size(); ) {//添加玩家到指定的位置
            int color = Integer.valueOf(players.get(i + 2));
            if (Integer.valueOf(players.get(i)) < 0) {//机器人
                siteState[color] = 0;
                site[color].setText("AI");
                addRobotButton[color].setText("-");
            } else {//玩家
                if (color == -1) {
                    HashMap<String, String> map2 = new HashMap<>();
                    map2.put("name", players.get(i + 1));
                    map2.put("score", "0");
                    idlePlayerListData.addLast(map2);
                } else {
                    site[color].setText(players.get(i + 1));
                    siteState[color] = 1;
                }
            }
            i += 3;
        }
        idlePlayerListAdapter.notifyDataSetChanged();
        ////////////setting
        title.setTypeface(Global.getFont());
        for (int i = 0; i < 4; i++) {
            site[i].setTypeface(Global.getFont());
            addRobotButton[i].setTypeface(Global.getFont());
        }
        startButton.setTypeface(Global.getFont());
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
                exit();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void processDataPack(final DataPack dataPack) {
        if (dataPack.getCommand() == DataPack.E_ROOM_ENTER) {
            HashMap<String, String> map = new HashMap<>();
            map.put("name", dataPack.getMessage(1));
            //map.put("score", dataPack.getMessage(2));
            map.put("score", "0");
            //Global.playersData.put(dataPack.getMessage(0), new Role(dataPack.getMessage(0), dataPack.getMessage(1), dataPack.getMessage(2), Integer.valueOf(dataPack.getMessage(3)), Role.PLAYER, false));
            Global.playersData.put(dataPack.getMessage(0), new Role(dataPack.getMessage(0), dataPack.getMessage(1), "0", Integer.valueOf(dataPack.getMessage(2)), Role.PLAYER, false));
            idlePlayerListData.addLast(map);
            site[0].post(new Runnable() {
                @Override
                public void run() {
                    idlePlayerListAdapter.notifyDataSetChanged();
                }
            });
        } else if (dataPack.getCommand() == DataPack.E_ROOM_EXIT) {
            if (dataPack.getMessageList() != null) {//不是我退出了
                if (dataPack.getMessage(0).compareTo(Global.dataManager.getHostId()) == 0)//是房主
                {
                    Intent intent = new Intent(getApplicationContext(), WanHallActivity.class);
                    startActivity(intent);
                } else {
                    for (HashMap<String, String> map : idlePlayerListData) {
                        if (map.get("name").compareTo(dataPack.getMessage(1)) == 0) {
                            idlePlayerListData.remove(map);
                            site[0].post(new Runnable() {
                                @Override
                                public void run() {
                                    idlePlayerListAdapter.notifyDataSetChanged();
                                }
                            });
                            return;
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        if (site[i].getText().toString().compareTo(dataPack.getMessage(1)) == 0) {
                            final int tmp = i;
                            site[i].post(new Runnable() {
                                @Override
                                public void run() {
                                    site[tmp].setText("JOIN");
                                    siteState[tmp] = -1;
                                }
                            });
                        }
                    }
                    Global.playersData.remove(dataPack.getMessage(0));
                }
            }
        } else if (dataPack.getCommand() == DataPack.E_ROOM_POSITION_SELECT) {
            if (dataPack.isSuccessful()) {
                final int id = Integer.valueOf(dataPack.getMessage(0));
                //final int np = Integer.valueOf(dataPack.getMessage(3));
                final int np = Integer.valueOf(dataPack.getMessage(2));
                if (id < 0) {//robot choose
                    if (np != -1) {//如果添加
                        siteState[np] = 0;
                        site[0].post(new Runnable() {
                            @Override
                            public void run() {
                                site[np].setText("AI");
                                addRobotButton[np].setText("-");
                            }
                        });
                        Global.playersData.put(dataPack.getMessage(0), new Role(dataPack.getMessage(0), "AI", "0", np, Role.AI, false));
                    } else {
                        siteState[-id - 1] = -1;
                        site[0].post(new Runnable() {
                            @Override
                            public void run() {
                                site[-id - 1].setText("JOIN");
                                addRobotButton[-id - 1].setText("+");
                            }
                        });
                        Global.playersData.remove(dataPack.getMessage(0));
                    }
                } else {//human
                    for (HashMap<String, String> map : idlePlayerListData) {
                        if (map.get("name").compareTo(dataPack.getMessage(1)) == 0) {
                            idlePlayerListData.remove(map);
                            break;
                        }
                    }
                    for (int i = 0; i < 4; i++) {
                        if (site[i].getText().toString().compareTo(dataPack.getMessage(1)) == 0) {
                            final int tmp = i;
                            site[0].post(new Runnable() {
                                @Override
                                public void run() {
                                    site[tmp].setText("JOIN");
                                    siteState[tmp] = -1;
                                }
                            });
                            break;
                        }
                    }
                    if (np == -1) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("name", dataPack.getMessage(1));
                        //map.put("score", dataPack.getMessage(2));
                        map.put("score", "0");
                        idlePlayerListData.addLast(map);
                    } else {
                        site[np].post(new Runnable() {
                            @Override
                            public void run() {
                                siteState[np] = 1;
                                site[np].setText(dataPack.getMessage(1));
                            }
                        });
                    }
                    Global.playersData.get(dataPack.getMessage(0)).color = np;
                    site[np].post(new Runnable() {
                        @Override
                        public void run() {
                            idlePlayerListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } else {
                idlePlayerView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "position select failed!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (dataPack.getCommand() == DataPack.E_GAME_START) {
            Intent intent = new Intent(getApplicationContext(), WanGamingActivity.class);
            startActivity(intent);
        }
    }

    private void chooseSite(int color) {
        if (siteState[color] == -1) {

            Global.socketManager.send(DataPack.R_ROOM_POSITION_SELECT, Global.dataManager.getMyId(), Global.dataManager.getRoomId(), Global.playersData.get(Global.dataManager.getMyId()).name, Global.playersData.get(Global.dataManager.getMyId()).score, color);

        } else {
            Toast.makeText(getApplicationContext(), "座位已被占!", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRobot(int color) {
        if (Global.dataManager.getHostId().compareTo(Global.dataManager.getMyId()) == 0) {
            if (siteState[color] == 1) {
                Toast.makeText(getApplicationContext(), "添加AI失败!", Toast.LENGTH_SHORT).show();
            } else {

                LinkedList<String> msgs = new LinkedList<>();
                msgs.addLast(String.format("%d", -color - 1));
                msgs.addLast(Global.dataManager.getRoomId());
                msgs.addLast("AI");
                msgs.addLast("0");
                if (siteState[color] == -1) {
                    msgs.addLast(String.format("%d", color));
                } else {
                    msgs.addLast("-1");
                }
                Global.socketManager.send(new DataPack(DataPack.R_ROOM_POSITION_SELECT, msgs));

            }
        } else {
            Toast.makeText(getApplicationContext(), "只有房主可以增删AI", Toast.LENGTH_SHORT).show();
        }
    }

    private void exit() {
        Global.socketManager.send(DataPack.R_ROOM_EXIT, Global.dataManager.getMyId(), Global.dataManager.getRoomId(), Global.playersData.get(Global.dataManager.getMyId()).color);

        Intent intent = new Intent(getApplicationContext(), WanHallActivity.class);
        startActivity(intent);


    }
}
