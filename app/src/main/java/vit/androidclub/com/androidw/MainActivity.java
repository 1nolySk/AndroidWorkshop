package vit.androidclub.com.androidw;

import android.Manifest;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    ImageButton play, next, previous;
    ListView list;
    TextView selected;
    SeekBar seekBar;

    Boolean isPlaying=false, movingSeek=false;

    String currentSong="";

    MusicPlayer musicPlayer;

    MediaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);

        list = findViewById(R.id.song_list);
        selected = findViewById(R.id.current_song);
        seekBar = findViewById(R.id.seekBar);

        getPermission();

        Intent intent = new Intent(MainActivity.this, MusicPlayer.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }


    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d("===activity","restart");

        MediaPlayer Player = musicPlayer.isworking();
        if (Player != null) {
            seekBar.setMax(Player.getDuration());
            seekBar.setProgress(Player.getCurrentPosition());

        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        musicPlayer.stopPlay();
        musicPlayer.onDestroy();
        unbindService(serviceConnection);
    }

    void fetchdata(){
        Cursor c = getContentResolver().query(MediaStore.Audio.Media.
                EXTERNAL_CONTENT_URI, null, null, null, null);

        if (c != null)
        {
            c.moveToFirst();

            adapter = new MediaAdapter(this, c,0);
            //setListAdapter(adapter);
            list.setAdapter(adapter);
            previous.setOnClickListener(buttonClick);
            play.setOnClickListener(buttonClick);
            next.setOnClickListener(buttonClick);

            list.setOnItemClickListener(selectsong);

        }
    }

    AdapterView.OnItemClickListener selectsong = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Log.d("====clcik",view.getTag().toString());
            currentSong = view.getTag().toString();

            musicPlayer.stopPlay();
            musicPlayer.startPlay(currentSong);
        }

    };



    View.OnClickListener buttonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.play:
                    if (isPlaying)
                    {

                        isPlaying=true;
                        musicPlayer.stopPlay();
                        if (currentSong!=null)
                            musicPlayer.startPlay(currentSong);
                        else
                            Toast.makeText(MainActivity.this,
                                    "No File Selected",Toast.LENGTH_SHORT).show();
                    }else
                    {
                        if (currentSong!=null) {

                            musicPlayer.startPlay(currentSong);
                        }
                        musicPlayer.stopPlay();
                    }
                    Toast.makeText(MainActivity.this,"Playing",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.next:
                    musicPlayer.seeknext();
                    Toast.makeText(MainActivity.this,"Next",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.previous:
                    musicPlayer.seekprevious();
                    Toast.makeText(MainActivity.this,"Previous",Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayer.MyBinder binder = (MusicPlayer.MyBinder) iBinder;
            musicPlayer = binder.getService();

            musicPlayer.initialize(play, next, previous, seekBar, selected);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicPlayer.stopPlay();
        }
    };

    private void getPermission()
    {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchdata();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
