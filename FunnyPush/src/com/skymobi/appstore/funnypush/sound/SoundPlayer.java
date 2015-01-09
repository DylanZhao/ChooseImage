
package com.skymobi.appstore.funnypush.sound;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.skymobi.appstore.funnypush.R;

/**
 * @ClassName: SoundPlayer
 * @Description: 音效管理
 * @author dylan.zhao;
 * @date 2013-10-18 上午10:12:58
 */
public class SoundPlayer {

    SoundPool soundPool;
    private final HashMap<Integer, Integer> soundPoolMap;
    public final static int CAR_START = 0;
    public final static int CAR_RUNNING = 1;
    public final static int CAR_STOP = 2;

    public SoundPlayer(Context context) {

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap<Integer, Integer>();

        soundPoolMap.put(CAR_RUNNING, soundPool.load(context, R.raw.car_run, 1));
        soundPoolMap.put(CAR_STOP, soundPool.load(context, R.raw.car_stop, 1));

    }

    public void play(final int which) {
        soundPool.play(soundPoolMap.get(which), 1, 1, 0, 0, 1);
    }

    public void exit() {
        soundPool.release();
        soundPool = null;
    }
}
