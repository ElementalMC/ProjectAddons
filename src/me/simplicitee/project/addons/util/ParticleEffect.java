package me.simplicitee.project.addons.util;

import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Lightweight, behaviour-preserving replacement for ProjectKorra's
 * {@code com.projectkorra.projectkorra.util.ParticleEffect}, which was removed
 * in ProjectKorra 1.13.0 in favour of Bukkit's native {@link Particle}.
 *
 * <p>Each constant maps to the {@link Particle} that renders the same underlying
 * {@code minecraft:} particle as the original ParticleEffect constant did. The
 * {@code display(...)} overloads mirror the originals exactly, including the
 * data-type guard, so existing particle calls keep their previous behaviour and
 * still pass data ({@code BlockData}, {@code ItemStack}, {@code Color}, ...) only
 * to particles that accept it.
 */
public enum ParticleEffect {
	BLOCK_CRACK(Particle.BLOCK),
	BUBBLE_POP(Particle.BUBBLE_POP),
	CLOUD(Particle.CLOUD),
	CRIT(Particle.CRIT),
	CRIT_MAGIC(Particle.ENCHANTED_HIT),
	DAMAGE_INDICATOR(Particle.DAMAGE_INDICATOR),
	END_ROD(Particle.END_ROD),
	EXPLOSION_HUGE(Particle.EXPLOSION_EMITTER),
	EXPLOSION_LARGE(Particle.EXPLOSION),
	EXPLOSION_NORMAL(Particle.POOF),
	FLAME(Particle.FLAME),
	HEART(Particle.HEART),
	SMOKE_LARGE(Particle.LARGE_SMOKE),
	SMOKE_NORMAL(Particle.SMOKE),
	SOUL_FIRE_FLAME(Particle.SOUL_FIRE_FLAME),
	SWEEP_ATTACK(Particle.SWEEP_ATTACK),
	WATER_BUBBLE(Particle.BUBBLE);

	private final Particle particle;
	private final Class<?> dataClass;

	ParticleEffect(Particle particle) {
		this.particle = particle;
		this.dataClass = particle.getDataType();
	}

	public Particle getParticle() {
		return particle;
	}

	public void display(Location loc, int amount) {
		display(loc, amount, 0, 0, 0);
	}

	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0);
	}

	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra) {
		loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, null, true);
	}

	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, Object data) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0, data);
	}

	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
		if (dataClass.isAssignableFrom(Void.class) || data == null || !dataClass.isAssignableFrom(data.getClass())) {
			display(loc, amount, offsetX, offsetY, offsetZ, extra);
		} else {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, data, true);
		}
	}
}
