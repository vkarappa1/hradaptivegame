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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;


public class NumberPanel extends Element {
    private Layer layer;
    private int number, digits, left, top, numbers_offset;
    public int alpha;
    private static Paint paint = new Paint();
    private BitmapDrawable prolog, numbers;
    private Rect location = new Rect(), source = new Rect();

    public NumberPanel(Context context, int d, int p, int n, Layer l) {
        layer = l;
        number = 0; digits = d; left = 0; top = 0; alpha = 255;
        prolog = (BitmapDrawable)context.getResources().getDrawable(p);
        numbers = (BitmapDrawable)context.getResources().getDrawable(n);
        numbers_offset = getPrologHeight();
    }

    public void reset(int n) {
        number = n;
    }
    public int getValue() {
        return number;
    }
    public int alter(int by) {
        return number += by;
    }

    public int getWidth() {
        return Math.max(numbers.getMinimumWidth() / 10 * digits, prolog.getMinimumWidth());
    }
    public int getPrologHeight() {
        return prolog.getMinimumHeight();
    }
    public int getTotalHeight() {
        return numbers_offset + numbers.getIntrinsicHeight();
    }
    public void setLeft(int l) {
        left = l;
    }
    public void setTop(int t) {
        top = t;
    }
    public void setNumberOffset(int h) {
        numbers_offset = h;
    }

    @Override
    public boolean tick() {
        return true;
    }

    @Override
    public void draw(Canvas c, Layer l) {
        if ( layer == l && alpha > 0 ) {
            paint.setAlpha(alpha);
            int width = numbers.getMinimumWidth() / 10;
    
            location.top = top;
            location.bottom = top + getPrologHeight();
            location.left = left + ( getWidth() - prolog.getMinimumWidth() ) / 2;
            location.right = location.left + prolog.getMinimumWidth();
            c.drawBitmap(prolog.getBitmap(), null, location, paint);
    
            source.top = 0;
            source.bottom = numbers.getIntrinsicHeight();
            location.top = top + numbers_offset;
            location.bottom = top + getTotalHeight();
            for ( int i = digits - 1, n = number > 0 ? number : 0; i >= 0; --i, n = n / 10 ) {
                location.left = left + ( getWidth() - width * digits ) / 2 + i * width;
                location.right = location.left + width;
                source.left = (n % 10) * width;
                source.right = source.left + width;
                c.drawBitmap(numbers.getBitmap(), source, location, paint);
            }
        }
    }
}
