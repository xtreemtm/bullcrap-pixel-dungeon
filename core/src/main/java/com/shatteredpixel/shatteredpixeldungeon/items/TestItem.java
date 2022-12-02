package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.watabou.utils.Random;
import com.watabou.utils.PathFinder;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class TestItem extends Item {

	private static final float TIME_TO_DRINK = 2f;

    private static final String AC_LOOK = "LOOK";
	private static final String AC_DRINK = "DRINK";

    {
		image = ItemSpriteSheet.LIQUID_METAL;

		stackable = false;

		defaultAction = AC_DRINK;

		bones = false;
	}

    @Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_LOOK );
		actions.add( AC_DRINK );
		return actions;
	}

    @Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_LOOK)) {

			GLog.i( Messages.get(TestItem.class, "look") );

		} else if (action.equals(AC_DRINK)) {

			drink( hero );

		}
	}

    @Override
	protected void onThrow( int cell ) {
		if (Dungeon.level.map[cell] == Terrain.WELL || Dungeon.level.pit[cell]) {

			super.onThrow( cell );

		} else  {

			Dungeon.level.pressCell( cell );
			if (Dungeon.level.heroFOV[cell]) {
				GLog.i( Messages.get(TestItem.class, "throw") );
				Sample.INSTANCE.play( Assets.Sounds.SHATTER );
				Splash.at( cell, 0xBFBFBF, 5 );
				explode( cell );
			}



		}
	}

	protected void drink( Hero hero ) {
		
		detach( hero.belongings.backpack );
		
		hero.spend( TIME_TO_DRINK );
		hero.busy();

		Buff.affect( hero, Vertigo.class, 20f );
		Buff.affect( hero, Healing.class ).setHeal((int) (0.8f * hero.HT + 14), 0.25f, 0);
		
		Sample.INSTANCE.play( Assets.Sounds.DRINK );
		
		hero.sprite.operate( hero.pos );
	}

    @Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return Math.max(1, quantity/2);
	}

	public void explode(int cell) {
		//We're blowing up.

		Sample.INSTANCE.play( Assets.Sounds.BLAST );

		if (true) {
			
			ArrayList<Char> affected = new ArrayList<>();
			
			if (Dungeon.level.heroFOV[cell]) {
				CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
			}
			
			boolean terrainAffected = false;
			for (int n : PathFinder.NEIGHBOURS9) {
				int c = cell + n;
				if (c >= 0 && c < Dungeon.level.length()) {
					if (Dungeon.level.heroFOV[c]) {
						CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
					}
					
					if (Dungeon.level.flamable[c]) {
						Dungeon.level.destroy(c);
						GameScene.updateMap(c);
						terrainAffected = true;
					}
					
					//destroys items / triggers bombs caught in the blast.
					Heap heap = Dungeon.level.heaps.get(c);
					if (heap != null)
						heap.explode();
					
					Char ch = Actor.findChar(c);
					if (ch != null) {
						affected.add(ch);
					}
				}
			}
			
			for (Char ch : affected){

				//if they have already been killed by another bomb
				if(!ch.isAlive()){
					continue;
				}

				int dmg = Random.NormalIntRange(5 + Dungeon.scalingDepth(), 10 + Dungeon.scalingDepth()*2);

				//those not at the center of the blast take less damage
				if (ch.pos != cell){
					dmg = Math.round(dmg*0.67f);
				}

				dmg -= ch.drRoll();

				if (dmg > 0) {
					ch.damage(dmg, this);
				}
				
				if (ch == Dungeon.hero && !ch.isAlive()) {
					GLog.n(Messages.get(this, "ondeath"));
					Dungeon.fail(TestItem.class);
				}
			}
			
			if (terrainAffected) {
				Dungeon.observe();
			}
		}
	}
}