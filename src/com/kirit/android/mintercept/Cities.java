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
import android.view.View;

import com.kirit.android.Element;


public class Cities extends Element {
    private MIntercept context;
    private City [] cities;
    private Explosions explosions;

    public Cities(Game game, MIntercept c, View view) {
        context = c;
        cities = new City [3];
        for ( int n = 0; n != cities.length; ++n )
            cities[n] = new City(game, context, view, n, cities.length);
        explosions = new Explosions(game, cities.length, 0);
    }

    public void reset() {
        for ( City c : cities )
            c.reset();
    }

    public City hasStruck(float x, float y) {
        for ( City c : cities )
            if ( c.hasStruck(x) ) {
                c.explode(explosions.reset(x, y));
                context.sounds.play(R.raw.city_destroyed);
                return c;
            }
        return null;
    }

    @Override
    public boolean tick() {
        boolean alldead = true;
        for ( City city : cities )
            if ( city.tick() )
                alldead = false;
        explosions.tick();
        return alldead;
    }

    @Override
    public void draw(Canvas c, Layer layer) {
        for ( City city : cities )
            city.draw(c, layer);
        explosions.draw(c, layer);
    }
}
