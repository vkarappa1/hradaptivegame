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

import android.graphics.Color;


/**
   This class handles moving through a colour spectrum.
   All movement is along the hue axis of the colour space.
*/
public final class Spectrum {
    float [] hls = new float [3];
    
    /**
        The only constructor
        This is the only constructor available. The parameter values are
        for the hue, luminance and saturation of the required colour. These
        values must all be between 0. and 1.
    */
    public Spectrum( float ch, float cl, float cs ) {
        hls[0] = ch;
        hls[1] = cl;
        hls[2] = cs;
    }

    /**
        Return next colour.
        The parameter passed in specifies how much of the spectrum should
        be stepped. For example a value of 100 would move one hundredth of
        the way through the spectrum and 20 would move one twentieth through
        the spectrum.
        It is only ever the hue that is traversed.
    */
    public int next( int steps ) {
        hls[0] += 360.0f / steps;
        hls[0] %= 360.0f;
        return Color.HSVToColor( hls );
    }
}


