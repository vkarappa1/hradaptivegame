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

import com.kirit.android.Element;


public class Explosions extends Element {
    private Explosion[] explosions;

    public Explosions(int number, int size) {
        explosions = new Explosion [number];
        for ( int i = 0; i != explosions.length; ++i )
            explosions[i] = new Explosion(size, Layer.EXPLOSIONS);
    }

    public Explosions(Game game, int number, int size) {
        explosions = new Explosion [number];
        for ( int i = 0; i != explosions.length; ++i )
            explosions[i] = new Explosion(game, size, Layer.EXPLOSIONS);
    }
    
    public Explosion reset(float x, float y) {
        if ( explosions[0].reset(x, y) ) {
            Explosion e = explosions[0];
            for ( int i = 0; i != explosions.length-1; ++i )
                explosions[i] = explosions[i+1];
            explosions[explosions.length-1] = e;
            return e;
        } else
            return null;
    }

    @Override
    public boolean tick() {
        for ( Explosion explosion : explosions )
            explosion.tick();
        return true;
    }

    @Override
    public void draw(Canvas c, Layer layer) {
        for ( Explosion explosion : explosions )
            explosion.draw(c, layer);
    }
}
