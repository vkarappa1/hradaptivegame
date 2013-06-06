/*
    Copyright 1995-2010, Kirit Saelensminde.
    http://www.kirit.com/Missile%20intercept

    This file is part of Missile intercept.

    Missile intercept is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Missile intercept is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Missile intercept.  If not, see <http://www.gnu.org/licenses/>.
*/


package com.kirit.android;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


public class Sounds implements Setting {
    private class Effect {
        private SoundPool pool;
        private int soundid;
        private int streamid;
        public Effect(Context context, SoundPool pool, int resid) {
            this.pool = pool;
            soundid = pool.load(context, resid, 1);
            streamid = 0;
        }
        void play(int loop) {
            if ( streamid > 0 )
                pool.stop(streamid);
            streamid = pool.play(soundid, 1f, 1f, 0, loop, 1f);
        }
    };
    private Context context;
    private SoundPool pool;
    private boolean on;
    private int toggle;
    private HashMap<Integer, Effect> sounds = new HashMap<Integer, Effect>();

    public Sounds(Context contact) {
        this.context = contact;
        pool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        toggle = 0;
        set(true);
    }

    /**
     * Load a sound at the default priority
     */
    public void load(int resid) {
        sounds.put(resid, new Effect(context, pool, resid));
        if ( toggle == 0 )
            toggle = resid;
    }

    public void play(int resid) {
        play(resid, 0);
    }
    public void play(int resid, int loop) {
        if ( on && sounds.containsKey(resid) )
            sounds.get(resid).play(loop);
    }

    @Override
    public boolean get() {
        return on;
    }

    @Override
    public void set(boolean newValue) {
        on = newValue;
    }

    @Override
    public boolean toggle() {
        on = !on;
        play(toggle);
        return on;
    }
}
