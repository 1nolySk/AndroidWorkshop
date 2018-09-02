package vit.androidclub.com.androidw;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.MutableShort;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class MusicPlayer extends Service {

    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;

    IBinder binder = new MyBinder();

    ImageButton play, prev, next;
    SeekBar seekBar;

    boolean isStarted = true;
    boolean movingSeek = false;
    String currentFile="";

    MediaPlayer player;
    TextView selected;

    final Handler handler = new Handler();

    final Runnable updatePositinRunnable = new Runnable() {
        @Override
        public void run() {
            updatePosition();
        }
    };

    void initialize(ImageButton play, ImageButton next, ImageButton prev, SeekBar seek, TextView selected)
    {
        this.play = play;
        this.prev = prev;
        this.next = next;
        seekBar = seek;
        this.selected=selected;

    }

    void updatePosition()
    {
        handler.removeCallbacks(updatePositinRunnable);
        seekBar.setProgress(player.getCurrentPosition());
        handler.postDelayed(updatePositinRunnable, UPDATE_FREQUENCY);
    }

    void startPlay(String file)
    {
        selected.setText(file);
        seekBar.setProgress(0);
        reset();
        currentFile = file;
        createAndShowForegroundNotification(MusicPlayer.this,12);
        try {
            player.setDataSource(file);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar.setMax(player.getDuration());
        play.setImageResource(android.R.drawable.ic_media_pause);
        updatePosition();
        isStarted = true;
    }

    void stopPlay()
    {
        stopForeground(true);
        player.stop();
        currentFile ="none";
        play.setImageResource(android.R.drawable.ic_media_play);
        handler.removeCallbacks(updatePositinRunnable);
        seekBar.setProgress(0);
        isStarted= false;
    }

    void seeknext()
    {
        int seekto = player.getCurrentPosition() + STEP_VALUE;
        if (seekto > player.getDuration())
            seekto = player.getDuration();
        player.pause();
        player.seekTo(seekto);
        player.start();
    }

    public void seekprevious() {
        int seekto = player.getCurrentPosition() - STEP_VALUE;
        if (seekto < 0)
            seekto = 0;
        player.pause();
        player.seekTo(seekto);
        player.start();
    }

    void reset()
    {
        isStarted=false;
        stopForeground(true);
        player.stop();
        player.reset();
    }
    void seek(int dur)
    {
        player.pause();
        //player.seekTo(dur);
        //player.start();
    }

    String getCurrentSong()
    {
        return currentFile;
    }

    int getSeek()
    {
        return player.getDuration();
    }

    MediaPlayer isworking()
    {
        if (isStarted)
            return player;
        else
            return null;
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener= new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            if (movingSeek)
            {
                Log.d("====seek","worki");
                seek(i);
            }else {
                seekBar.setProgress(0);
            }
            Log.d("====seek","worki  222");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            movingSeek=true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            movingSeek=false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("===service","runninf");
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlay();
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                return false;
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
        player.release();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder
    {
        MusicPlayer getService()
        {
            return MusicPlayer.this;
        }

    }

    private void createAndShowForegroundNotification(Service yourService, int notificationId) {

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,0);

        final NotificationCompat.Builder builder = getNotificationBuilder(yourService,
                "com.example.your_app.notification.CHANNEL_ID_FOREGROUND", // Channel id
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top
                builder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                .setContentTitle("Music");

        Log.d("===service", "work");
        Notification notification = builder.build();

        yourService.startForeground(notificationId, notification);


    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String description = "Playing";
        final NotificationManager nm = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);
                nm.createNotificationChannel(nChannel);
            }
        }
    }
}
