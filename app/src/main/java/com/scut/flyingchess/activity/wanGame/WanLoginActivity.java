package com.scut.flyingchess.activity.wanGame;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.scut.flyingchess.Global;
import com.scut.flyingchess.R;
import com.scut.flyingchess.activity.BaseActivity;
import com.scut.flyingchess.activity.ChooseModeActivity;
import com.scut.flyingchess.dataPack.DataPack;
import com.scut.flyingchess.dataPack.Target;
import com.scut.flyingchess.manager.SoundManager;


public class WanLoginActivity extends BaseActivity implements Target {
    Button login, home, register;
    EditText _myName, _pw, _pw2;
    TextInputLayout myName, pw, pw2;
    boolean bRegister;
    ImageView imageView;
    Button waitBackground;
    ImageView waitView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ui setting
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);//Activity切换动画
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Global.activityManager.add(this);
        Global.soundManager.playMusic(SoundManager.BACKGROUND);
        //init
        login = (Button) findViewById(R.id.loginButton);
        myName = (TextInputLayout) findViewById(R.id.myName);
        _myName = (EditText) findViewById(R.id._myName);
        pw = (TextInputLayout) findViewById(R.id.pw);
        _pw = (EditText) findViewById(R.id._pw);
        register = (Button) findViewById(R.id.register);
        home = (Button) findViewById(R.id.home);
        pw2 = (TextInputLayout) findViewById(R.id.pw2);
        _pw2 = (EditText) findViewById(R.id._pw2);
        bRegister = false;
        imageView = (ImageView) findViewById(R.id.imageView);
        waitBackground = (Button) findViewById(R.id.waitbackground);
        waitView = (ImageView) findViewById(R.id.wait);
        //trigger
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                if (_myName.getText().length() == 0) {
                    _myName.setError("Please input you name");
                    _myName.requestFocus();
                    return;
                }
                if (_pw.getText().length() == 0) {
                    _pw.setError("Please input you password");
                    _pw.requestFocus();
                    return;
                }
                if (bRegister) {// sign in
                    if (_pw2.getText().length() == 0) {
                        _pw2.setError("Please confirm you password");
                        _pw2.requestFocus();
                        return;
                    }
                    if (_pw2.getText().toString().compareTo(_pw2.getText().toString()) != 0) {
                        _pw2.setError("Password you input is not same");
                        _pw2.requestFocus();
                        return;
                    }
                    //sign in
                    Global.socketManager.send(DataPack.R_REGISTER, _myName.getText().toString(), _pw.getText().toString());
                    waitBackground.setVisibility(View.VISIBLE);
                    waitView.setVisibility(View.VISIBLE);
                    Global.startWaitAnimation(waitView);
                } else {// login
                    Global.socketManager.send(DataPack.R_LOGIN, _myName.getText().toString(), _pw.getText().toString());
                    waitBackground.setVisibility(View.VISIBLE);
                    waitView.setVisibility(View.VISIBLE);
                    Global.startWaitAnimation(waitView);
                    Global.dataManager.setMyName(_myName.getText().toString());
                    Global.dataManager.setPassword(_pw.getText().toString());
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                if (bRegister) {
                    login.setText("Login");
                    register.setText("Register");
                    bRegister = false;
                    pw2.setVisibility(View.GONE);
                    _pw2.setText("");
                } else {
                    _myName.setText("");
                    _pw.setText("");
                    _pw2.setText("");
                    pw2.setVisibility(View.VISIBLE);
                    login.setText("Register");
                    register.setText("Back");
                    _myName.requestFocus();
                    bRegister = true;
                }
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.soundManager.playSound(SoundManager.BUTTON);
                startActivity(new Intent(getApplicationContext(), ChooseModeActivity.class));
            }
        });
        Global.socketManager.registerActivity(DataPack.A_LOGIN, this);
        Global.socketManager.registerActivity(DataPack.A_REGISTER, this);
        //setting
        pw2.setVisibility(View.GONE);
        if (Global.dataManager.autoLogin()) {
            _myName.setText(Global.dataManager.getMyName());
            _pw.setText(Global.dataManager.getPassword());
        }
        imageView.setImageBitmap(Global.getBitmap(R.raw.cloud));
        waitView.setVisibility(View.INVISIBLE);
        waitBackground.setVisibility(View.INVISIBLE);
        login.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        register.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        myName.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        pw.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        pw2.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        _myName.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        _pw.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
        _pw2.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/comici.ttf"));
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
    public void processDataPack(DataPack dataPack) {
        if (dataPack.getCommand() == DataPack.A_LOGIN) {
            if (dataPack.isSuccessful()) {
                Global.dataManager.setMyId(dataPack.getMessage(0));
                //Global.dataManager.setOnlineScore(dataPack.getMessage(1));
                Global.dataManager.setMyName(dataPack.getMessage(1));
                startActivity(new Intent(getApplicationContext(), WanHallActivity.class));
                Global.dataManager.setAutoLogin(true);
            } else {
                myName.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_SHORT).show();
                        waitBackground.setVisibility(View.INVISIBLE);
                        waitView.setVisibility(View.INVISIBLE);
                    }
                });
            }
            Global.stopWaitAnimation();
        } else if (dataPack.getCommand() == DataPack.A_REGISTER) {
            if (dataPack.isSuccessful()) {
                myName.post(new Runnable() {
                    @Override
                    public void run() {
                        login.setText("Login");
                        register.setText("Register");
                        bRegister = false;
                        pw2.setVisibility(View.GONE);
                        _pw2.setText("");
                        Toast.makeText(getApplicationContext(), "Register successful!", Toast.LENGTH_SHORT).show();
                        waitBackground.setVisibility(View.INVISIBLE);
                        waitView.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                myName.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Register failed!", Toast.LENGTH_SHORT).show();
                        waitBackground.setVisibility(View.INVISIBLE);
                        waitView.setVisibility(View.INVISIBLE);
                    }
                });
            }
            Global.stopWaitAnimation();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//返回按钮
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                startActivity(new Intent(getApplicationContext(), ChooseModeActivity.class));
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
