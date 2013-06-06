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

import android.content.Context;


public class Vibrator implements Setting {
    private Context context;
    private boolean active;
    private android.os.Vibrator vibrator;

    public Vibrator(Context c) {
        context = c;
        set(true);
    }

    public boolean vibrate(int length) {
        if ( vibrator == null )
            vibrator = (android.os.Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        if ( active && vibrator != null ) {
            vibrator.vibrate(length);
            return true;
        } else
            return false;
    }

    @Override
    public boolean get() {
        return active;
    }
    @Override
    public void set(boolean newValue) {
        active = newValue;
    }
    @Override
    public boolean toggle() {
        active = !active;
        vibrate(80);
        return active;
    }
}
