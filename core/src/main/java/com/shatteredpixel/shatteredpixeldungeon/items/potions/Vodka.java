/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * Slavic Pixel Dungeon
 * Copyright (C) 2022-2022 Kyrylo Semakas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;


public class Vodka extends Potion {

	{
		icon = ItemSpriteSheet.Icons.POTION_HASTE;

		bones = true;
	}
	
	@Override
	public void apply( Hero hero ) {
		identify();
		// new Flare( 6, 32 ).color(0xFFFF00, true).show( curUser.sprite, 2f );
        Buff.prolong( hero, Vertigo.class, Vertigo.DURATION);
	}
	
	@Override
	public int value() {
		return isKnown() ? 100 * quantity : super.value();
	}

	@Override
	public int energyVal() {
		return isKnown() ? 12 * quantity : super.energyVal();
	}
}
