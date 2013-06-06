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


public class Player extends Element {
    private static final int BASE_EXPLOSION_SIZE = 35;
    private MIntercept context;
    private View view;
    private int hitbonus;
    private Game game;
    private Explosions shots;
    private Cities cities;

    public Player(MIntercept context, View view, Game game) {
        this.context = context;
        this.view = view;
        this.game = game;
        shots = new Explosions(game, 10, 35);
        cities = new Cities(game, context, view);
        reset();
    }

    public void reset() {
        cities.reset();
    }

    public City struckCity(float x, float y) {
        return cities.hasStruck(x, y);
    }

    /**
     * The player has exploded a missile.
     */
    public void hit() {
        if ( !game.isOver() ) {
            context.vibrator.vibrate(40 * hitbonus);
            game.award(5 * game.level.getValue() * hitbonus++);
        }
    }

    public boolean tap(float x, float y) {
        int height = view.getHeight();
        if ( !game.isOver() && y < height - 40 ) {
            Explosion explosion = shots.reset(x, y);
            if ( explosion != null ) {
                int scale_height = height * 2 / 3 - 40;
                if ( y > view.getHeight() * 2 / 3 )
                    explosion.setSize(BASE_EXPLOSION_SIZE - ((int)y - scale_height) * BASE_EXPLOSION_SIZE / (scale_height));
                else
                    explosion.setSize(BASE_EXPLOSION_SIZE);
                hitbonus = 1;
                game.award(-1);
                context.sounds.play(R.raw.player_launch);
                return true;
            } else
                context.sounds.play(R.raw.player_error);
        }
        return false;
    }

    @Override
    public boolean tick() {
        if ( cities.tick() )
            game.over();
        shots.tick();
        return !game.isOver();
    }

    @Override
    public void draw(Canvas c, Layer layer) {
        cities.draw(c, layer);
        shots.draw(c, layer);
    }
}
