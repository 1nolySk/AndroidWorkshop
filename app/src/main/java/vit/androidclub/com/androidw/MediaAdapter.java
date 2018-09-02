package vit.androidclub.com.androidw;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.math.BigDecimal;


public class MediaAdapter extends CursorAdapter {


    public MediaAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater=LayoutInflater.from(context);

        return inflater.inflate(R.layout.list,viewGroup,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView name = view.findViewById(R.id.song_name);
        TextView duration = view.findViewById(R.id.song_duration);

        name.setText(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));
        long durationInMS = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));
        double durationInMin = ((double) durationInMS / 1000.0) / 60.0;
        durationInMin = new BigDecimal(Double.toString(durationInMin)).
                setScale(2, BigDecimal.ROUND_UP).doubleValue();
        duration.setText(durationInMin+"m");

        view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));


    }
}
