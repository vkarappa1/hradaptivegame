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


package com.kirit.android.mintercept;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.kirit.android.Element;
import com.kirit.android.Spectrum;


public class Explosion extends Element {
    private Layer layer;
    private static Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean draw = false;
    private Spectrum colour = new Spectrum(0.0f, 0.75f, 0.75f);
    private int paint_color;
    private float cx, cy, inner_radius, outer_radius;
    private int size, fade;

    public Explosion(int s, Layer l) {
        layer = l;
        size = s;
    }
    public Explosion(Game game, int s, Layer l) {
        this(s, l);
        game.explosion(this);
    }

    public void setSize(int s) {
        size = s;
    }
    private void reset() {
        draw = false;
        inner_radius = 0;
        outer_radius = 0;
    }
    public boolean reset(float x, float y) {
        if ( !draw ) {
            reset();
            draw = true;
            cx = x; cy = y;
            fade = 8;
            return true;
        } else
            return false;
    }

    /**
     * Returns true when the explosion has reached it's maximum size
     */
    public boolean pastZenith() {
        return inner_radius > 1;
    }
    /**
     * Returns true if the location is inside the explosion
     */
    public boolean inside(float x, float y) {
        return draw && fade == 8 && ( cx - x ) * ( cx - x ) + ( cy - y ) * ( cy - y ) < outer_radius * outer_radius;
    }

    @Override
    public boolean tick() {
        if ( draw ) {
            if (inner_radius < size) {
                paint_color = colour.next(size);
                if ( outer_radius > size )
                    ++inner_radius;
                else
                    ++outer_radius;
            } else if ( fade > 0 )
                --fade;
            else
                reset();
        }
        return draw;
    }

    @Override
    public void draw(Canvas c, Layer l) {
        if (layer == l && draw ) {
            if (inner_radius < size) {
                paint.setColor(paint_color);
                c.drawCircle(cx, cy, outer_radius, paint);
                if ( outer_radius > size ) {
                    paint.setColor(0xff404040);
                    c.drawCircle(cx, cy, inner_radius, paint);
                }
            } else if ( fade > 0 ) {
                paint.setColor(0x404040 + fade * 0x02000000 + (fade-1) * 0x20000000);
                c.drawCircle(cx, cy, inner_radius, paint);
            }
        }
    }
}
