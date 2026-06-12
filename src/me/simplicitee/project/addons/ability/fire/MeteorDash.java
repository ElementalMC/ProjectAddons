package me.simplicitee.project.addons.ability.fire;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.CombustionAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.DamageHandler;
import me.simplicitee.project.addons.ProjectAddons;
import me.simplicitee.project.addons.util.ParticleEffect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class MeteorDash extends CombustionAbility implements AddonAbility {

    private static final String PATH = "Abilities.Fire.MeteorDash.";

    private Location location;
    private double maxHeightAfterExplosion;
    private Location right;
    private Location left;

    @Attribute("ExplosionsCount")
    private int count; // count of explosions
    @Attribute("MeteorDamage")
    private double meteorDamage;
    @Attribute(Attribute.KNOCKUP)
    private double userKnockback;
    @Attribute(Attribute.KNOCKBACK)
    private double enemiesKnockback;
    @Attribute(Attribute.CHARGE_DURATION)
    private long timeBetweenExplosions;
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    @Attribute(Attribute.DAMAGE)
    private double auraDamage;
    @Attribute(Attribute.FIRE_TICK)
    private int auraFireTicks;
    private boolean meteoriteSetsCooldown;
    @Attribute(Attribute.HEIGHT)
    private double minHeightToMeteorite;
    @Attribute(Attribute.SELECT_RANGE)
    private double tooCloseDistance;
    private long minTimeBetweenExplosions;
    @Attribute(Attribute.SPEED)
    private double doAuraSpeed;
    private double doMeteoriteSpeed;
    private boolean fireInHands;
    private boolean fireAuraOnlyWhenSelected;
    private double fireAuraRadius;

    private ArrayList<Entity> hitEntities;
    private Long lastBlastedTime = null;
    private boolean charged = false;
    private boolean bigSpeed = false;
    private double speed;

    public MeteorDash(Player player) {
        super(player);

        if (bPlayer.isOnCooldown(this)) {
            return;
        }
        if (!bPlayer.canBend(this)) {
            return;
        }
        if (hasAbility(player, FireJet.class)) {
            return;
        }
        if (hasAbility(player, MeteorDash.class)) {
            MeteorDash md = getAbility(player, MeteorDash.class);
            if (md.isStarted()) {
                return;
            }
        }

        setFields();
        start();
    }

    private void setFields() {
        count = ProjectAddons.instance.getConfig().getInt(PATH + "ExplosionsCount");
        meteorDamage = ProjectAddons.instance.getConfig().getDouble(PATH + "MeteorDamage");
        auraDamage = ProjectAddons.instance.getConfig().getDouble(PATH + "AuraDamage");
        auraFireTicks = ProjectAddons.instance.getConfig().getInt(PATH + "AuraFireTicks");
        userKnockback = ProjectAddons.instance.getConfig().getDouble(PATH + "UserKnockback");
        enemiesKnockback = ProjectAddons.instance.getConfig().getDouble(PATH + "EnemiesKnockback");
        timeBetweenExplosions = ProjectAddons.instance.getConfig().getLong(PATH + "TimeBetweenExplosions");
        cooldown = ProjectAddons.instance.getConfig().getLong(PATH + "Cooldown");
        meteoriteSetsCooldown = ProjectAddons.instance.getConfig().getBoolean(PATH + "MeteoriteSetsCooldown");
        minHeightToMeteorite = ProjectAddons.instance.getConfig().getDouble(PATH + "MinHeightToMeteorite");
        tooCloseDistance = ProjectAddons.instance.getConfig().getDouble(PATH + "TooCloseDistance");
        doAuraSpeed = ProjectAddons.instance.getConfig().getDouble(PATH + "DoAuraSpeed");
        doMeteoriteSpeed = ProjectAddons.instance.getConfig().getDouble(PATH + "DoMeteoriteSpeed");
        minTimeBetweenExplosions = ProjectAddons.instance.getConfig().getLong(PATH + "MinTimeBetweenExplosions");
        fireInHands = ProjectAddons.instance.getConfig().getBoolean(PATH + "FireInHands");
        fireAuraOnlyWhenSelected = ProjectAddons.instance.getConfig().getBoolean(PATH + "FireAuraOnlyWhenSelected");
        fireAuraRadius = ProjectAddons.instance.getConfig().getDouble(PATH + "FireAuraRadius");
        maxHeightAfterExplosion = player.getLocation().getY();
        hitEntities = new ArrayList<>();
        applyDamageModifiers(meteorDamage);
    }

    private void applyDamageModifiers(double damage) {
        int damageMod = (int) (this.getDayFactor(damage) - damage);
        damageMod = (int) (bPlayer.canUseSubElement(Element.BLUE_FIRE) ? (BlueFireAbility.getDamageFactor() * damage - damage) + damageMod : damageMod);

        this.meteorDamage += damageMod;
        this.auraDamage += damageMod;
    }

    private void fireAura(Player player) {
        Random random = new Random();
        speed = player.getVelocity().length() * 20;
        bigSpeed = speed > doMeteoriteSpeed;
        double speedParticles = (int) speed;
        if (speedParticles > 40) speedParticles = 40;
        if (fireInHands) {
            right = player.getLocation().add(0, 0.65, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getRightHeadDirection(player).multiply(0.6D));
            left = player.getLocation().add(0, 0.65, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getLeftHeadDirection(player).multiply(0.6D));
        }
        if (fireInHands && speed > 10 && bPlayer.getBoundAbilityName().equalsIgnoreCase("MeteorDash")) {
            if (!isWater(left.getBlock()))
                if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                    ParticleEffect.SOUL_FIRE_FLAME.display(left, (int) speedParticles, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, speedParticles / 1000);
                else
                    ParticleEffect.FLAME.display(left, (int) speedParticles, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, speedParticles / 1000);
            else ParticleEffect.WATER_BUBBLE.display(left, 15, 0.25, 0.25, 0.25, 0.5);
            if (!isWater(right.getBlock()))
                if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                    ParticleEffect.SOUL_FIRE_FLAME.display(right, (int) speedParticles, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, speedParticles / 1000);
                else
                    ParticleEffect.FLAME.display(right, (int) speedParticles, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, speedParticles / 1000);
            else ParticleEffect.WATER_BUBBLE.display(right, 15, 0.25, 0.25, 0.25, 0.5);

            if (random.nextDouble() < 0.5) {
                playFirebendingSound(left);
                playFirebendingSound(right);
            }
        }
        if (speed > doAuraSpeed) {
            Vector pMoveDirection = player.getVelocity().normalize();
            Location auraLocation = player.getLocation().add(0, 0.9, 0).add(pMoveDirection.multiply(2));
            double auraRadius = 2D;
            player.setFallDistance(0);
            if (fireInHands) {
                if (bigSpeed)
                    ParticleEffect.SMOKE_NORMAL.display(left, 10, 0.01, 0.01, 0.01, 0.7);
                if (!isWater(left.getBlock()))
                    playFirebendingParticles(left, (int) speedParticles, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10);
                else ParticleEffect.BUBBLE_POP.display(left, 15, 0.25, 0.25, 0.25);

                if (bigSpeed)
                    ParticleEffect.SMOKE_NORMAL.display(right, 10, 0.01, 0.01, 0.01, 0.7);
                if (!isWater(right.getBlock()))
                    playFirebendingParticles(right, (int) speedParticles, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10, (speedParticles / 1000) * speedParticles / 10);
                else ParticleEffect.BUBBLE_POP.display(right, 15, 0.25, 0.25, 0.25);

                if (random.nextDouble() < 0.5) {
                    playFirebendingSound(player.getEyeLocation());
                }
                if (bigSpeed)
                    player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_DEATH, 2, 0.5F);
                else
                    player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_DEATH, 1, 0.1F);
            } else {
                for (double theta = 0.0D; theta < 180.0D; theta += 20D) {
                    for (double phi = 0.0D; phi < 360.0D; phi += 20D) {
                        double rphi = Math.toRadians(phi);
                        double rtheta = Math.toRadians(theta);
                        Location display = auraLocation.clone().add(auraRadius / 1.5D * Math.cos(rphi) * Math.sin(rtheta), auraRadius / 1.5D * Math.cos(rtheta), auraRadius / 1.5D * Math.sin(rphi) * Math.sin(rtheta));

                        if (random.nextDouble() < 0.20) {
                            if (!isWater(auraLocation.getBlock())) {
                                if (bPlayer.canUseSubElement(Element.BLUE_FIRE)) {
                                    if (bigSpeed && random.nextDouble() < 0.10)
                                        ParticleEffect.SMOKE_NORMAL.display(display, 10, 0.2, 0.2, 0.2, 0.3);
                                    ParticleEffect.SOUL_FIRE_FLAME.display(display, 1, 0.1, 0.1, 0.1, 0.5);
                                } else {
                                    if (bigSpeed && random.nextDouble() < 0.10)
                                        ParticleEffect.SMOKE_NORMAL.display(display, 10, 0.2, 0.2, 0.2, 0.3);
                                    ParticleEffect.FLAME.display(display, 1, 0.1, 0.1, 0.1, 0.5);
                                }
                            } else {
                                ParticleEffect.WATER_BUBBLE.display(display, 10, 0.1, 0.1, 0.1, 0.7);
                            }
                        }

                        if (random.nextDouble() < 0.01) {
                            playFirebendingSound(display);
                        }
                    }
                }
            }
            for (Entity entity : GeneralMethods.getEntitiesAroundPoint(auraLocation, fireAuraRadius)) {
                if ((entity instanceof LivingEntity) && entity.getUniqueId() != player.getUniqueId() && !hitEntities.contains(entity)) {
                    DamageHandler.damageEntity(entity, auraDamage, this);
                    entity.setFireTicks(auraFireTicks);
                    ParticleEffect.EXPLOSION_HUGE.display(((LivingEntity) entity).getEyeLocation(), 1, 0.3, 0.3, 0.3);
                    ParticleEffect.EXPLOSION_NORMAL.display(((LivingEntity) entity).getEyeLocation(), 10, 0.3, 0.3, 0.3, 0.6);
                    Vector dir = GeneralMethods.getDirection(player.getEyeLocation(), ((LivingEntity) entity).getEyeLocation()).normalize().multiply(enemiesKnockback);
                    entity.setVelocity(dir);
                    hitEntities.add(entity);
                    player.getWorld().playSound(((LivingEntity) entity).getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 2.0F);
                }
            }
        }
    }

    private void meteorite(Location craterLoc) {
        craterLoc.add(0, 2, 0);
        ParticleEffect.EXPLOSION_LARGE.display(craterLoc, 5, 1, 1, 1, 1);
        ParticleEffect.EXPLOSION_NORMAL.display(craterLoc, 10, 1, 1, 1, 1);
        if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
            ParticleEffect.SOUL_FIRE_FLAME.display(craterLoc, 50, 3, 3, 3, 1);
        else
            ParticleEffect.FLAME.display(craterLoc, 50, 3, 3, 3, 1);
        for (Entity e : GeneralMethods.getEntitiesAroundPoint(craterLoc, 5)) {
            if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
                Vector direction = GeneralMethods.getDirection(craterLoc, ((LivingEntity) e).getEyeLocation()).normalize().multiply(enemiesKnockback);
                DamageHandler.damageEntity(e, meteorDamage, this);
                e.setVelocity(direction);
            }
        }
    }

    private void explosion(Location explodeCenter) {
        boolean explodeWater = isWater(explodeCenter.getBlock());
        Location right = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getRightHeadDirection(player).multiply(0.6D));
        Location left = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getLeftHeadDirection(player).multiply(0.6D));
        if (!explodeWater)
            if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                ParticleEffect.SOUL_FIRE_FLAME.display(explodeCenter, 10, 0.1, 0.1, 0.1, 0.5);
            else
                ParticleEffect.FLAME.display(explodeCenter, 10, 0.1, 0.1, 0.1, 0.5);
        if (!explodeWater)
            ParticleEffect.EXPLOSION_HUGE.display(explodeCenter, 1);
        else
            ParticleEffect.EXPLOSION_LARGE.display(explodeCenter, 1);
        if (!explodeWater)
            ParticleEffect.EXPLOSION_LARGE.display(left, 1);
        else
            ParticleEffect.EXPLOSION_NORMAL.display(explodeCenter, 1);
        if (!explodeWater)
            if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                ParticleEffect.SOUL_FIRE_FLAME.display(left, 5, 0.1, 0.1, 0.1, 0.1);
            else
                ParticleEffect.FLAME.display(left, 5, 0.1, 0.1, 0.1, 0.1);
        if (!explodeWater)
            ParticleEffect.EXPLOSION_LARGE.display(right, 1);
        else
            ParticleEffect.EXPLOSION_NORMAL.display(explodeCenter, 1);
        if (!explodeWater)
            if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                ParticleEffect.SOUL_FIRE_FLAME.display(right, 5, 0.1, 0.1, 0.1, 0.1);
            else
                ParticleEffect.FLAME.display(right, 5, 0.1, 0.1, 0.1, 0.1);
        player.getWorld().playSound(explodeCenter, Sound.ENTITY_GENERIC_EXPLODE, 2.0F, 3.0F);
        for (Entity e : GeneralMethods.getEntitiesAroundPoint(explodeCenter, 4)) {
            if (e instanceof LivingEntity) {
                Vector direction;
                if (!explodeWater)
                    if (e.getUniqueId() == player.getUniqueId())
                        direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(userKnockback);
                    else
                        direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(enemiesKnockback);
                else if (e.getUniqueId() == player.getUniqueId())
                    direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(userKnockback / 2);
                else
                    direction = GeneralMethods.getDirection(explodeCenter, ((LivingEntity) e).getEyeLocation()).normalize().multiply(enemiesKnockback / 2);
                if (e.getUniqueId() != player.getUniqueId())
                    DamageHandler.damageEntity(e, meteorDamage, this);
                e.setVelocity(direction);
            }
        }
    }

    public Vector getRightHeadDirection(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        return (new Vector(-direction.getZ(), 0.0D, direction.getX())).normalize();
    }

    public Vector getLeftHeadDirection(Player player) {
        Vector direction = player.getLocation().clone().getDirection().normalize();
        return (new Vector(direction.getZ(), 0.0D, -direction.getX())).normalize();
    }

    public Vector getBackHeadDirection(Player player) {
        Vector direction = player.getLocation().clone().getDirection().normalize();
        return (new Vector(-direction.getX(), 0.0D, -direction.getZ())).normalize();
    }

    private void endMD() {
        bPlayer.addCooldown(this);
        remove();
    }

    @Override
    public void progress() {
        if (player.isDead() || !player.isOnline() || bPlayer.isChiBlocked() || !bPlayer.isToggled()) {
            endMD();
            return;
        }
        if (lastBlastedTime != null) {
            if (!bPlayer.isOnCooldown("FireJet"))
                bPlayer.addCooldown("FireJet", 50);
            if (System.currentTimeMillis() > lastBlastedTime + timeBetweenExplosions) {
                endMD();
                return;
            }
        }
        if (player.getLocation().getY() >= maxHeightAfterExplosion)
            maxHeightAfterExplosion = player.getLocation().getY();
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 1.0D);
        double landingHeight = player.getLocation().getY();
        if (GeneralMethods.isSolid(loc.getBlock())) {
            if (speed > doAuraSpeed && maxHeightAfterExplosion - landingHeight > minHeightToMeteorite) {
                Location destination = player.getEyeLocation().add(1.5, 0.0D, 1.5);
                Vector vec = GeneralMethods.getDirection(this.player.getLocation(), destination.clone());
                for (int i = 0; i <= 360; i += 5) {
                    vec = GeneralMethods.rotateXZ(vec, i - 180);
                    vec.setY(0);
                    if (bPlayer.canUseSubElement(Element.BLUE_FIRE))
                        ParticleEffect.SOUL_FIRE_FLAME.display(player.getLocation().add(vec), 5, 0.1, 0.1, 0.1, 0.5);
                    else
                        ParticleEffect.FLAME.display(player.getLocation().add(vec), 5, 0.1, 0.1, 0.1, 0.5);
                }
                player.getWorld().playSound(destination, Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.5F);
            }
            if (bigSpeed && maxHeightAfterExplosion - landingHeight > minHeightToMeteorite) {
                meteorite(loc);
                if (meteoriteSetsCooldown) {
                    endMD();
                    return;
                }
            }
            if (count == 0 && System.currentTimeMillis() > lastBlastedTime + minTimeBetweenExplosions) {
                endMD();
                return;
            }
            maxHeightAfterExplosion = player.getLocation().getY();
        }
        if (lastBlastedTime != null) {
            if (fireAuraOnlyWhenSelected) {
                if (bPlayer.getBoundAbilityName().equalsIgnoreCase("MeteorDash"))
                    fireAura(player);
            } else
                fireAura(player);
        }
        if (bPlayer.getBoundAbilityName().equalsIgnoreCase("MeteorDash")) {
            location = player.getEyeLocation();
            Vector direction = location.getDirection().normalize();
            boolean tooClose = false;
            for (double i = 0; i < tooCloseDistance; i += 0.4D) {
                if (GeneralMethods.isSolid(location.clone().add(direction.clone().multiply(i)).getBlock())) {
                    tooClose = true;
                    i = tooCloseDistance;
                }
            }
            if (count > 0 && !tooClose) {
                Vector directionBack = direction.clone().multiply(-1).normalize(); // opposite direction
                Location explodeCenter = location.clone().add(directionBack); // explosion center
                right = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getRightHeadDirection(player).multiply(0.6D));
                left = player.getLocation().add(0, 0.5, 0).add(getBackHeadDirection(player).multiply(0.3D)).add(getLeftHeadDirection(player).multiply(0.6D));
                if (player.isSneaking()) {
                    if (lastBlastedTime == null || (System.currentTimeMillis() > lastBlastedTime + minTimeBetweenExplosions)) {
                        charged = true;
                        ParticleEffect.CRIT.display(location.clone().add(direction.clone().multiply(tooCloseDistance).add(getRightHeadDirection(player).multiply(0.6D))), 1, 0.1, 0.1, 0.1, 0);
                        ParticleEffect.CRIT.display(location.clone().add(direction.clone().multiply(tooCloseDistance).add(getLeftHeadDirection(player).multiply(0.6D))), 1, 0.1, 0.1, 0.1, 0);
                        Random rand = new Random();
                        if (rand.nextDouble() < 0.15) {
                            ParticleEffect.SMOKE_LARGE.display(left, 1, 0.1, 0.1, 0.1, 0.03);
                            ParticleEffect.SMOKE_LARGE.display(right, 1, 0.1, 0.1, 0.1, 0.03);
                        }
                        ParticleEffect.SMOKE_NORMAL.display(left, 1, 0.1, 0.1, 0.1, 0.1);
                        ParticleEffect.SMOKE_NORMAL.display(right, 1, 0.1, 0.1, 0.1, 0.1);
                    }
                } else if (charged) {
                    charged = false;
                    count--;
                    hitEntities.clear();
                    explosion(explodeCenter);
                    lastBlastedTime = System.currentTimeMillis();
                }
            } else {
                charged = false;
                if (lastBlastedTime == null)
                    remove();
            }
        } else {
            charged = false;
            if (lastBlastedTime == null)
                remove();
        }
    }

    @Override
    public long getCooldown() {
        return cooldown;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getName() {
        return "MeteorDash";
    }

    @Override
    public String getDescription() {
        return "Having solved the secret of subordinating explosions to their will, combustionbenders are able to "
                + "create explosions from their limbs, pushing themselves in the air over long distances. Accelerating "
                + "to high speed, they are able to create a fiery shell in front of them, like meteors (this shell "
                + "additionally protects the benders from falling damage). If using this technique to fall from a big "
                + "height at high speed, then the combustionbender will create a strong explosion around them, pushing "
                + "everyone to the sides.";
    }

    @Override
    public String getInstructions() {
        return "To blast yourself in the direction you look, hold and release Sneak (default Shift). For some time "
                + "after the explosion, you can create another one and so on up to a certain limit. Falling from great "
                + "heights at high speed will automatically create a powerful explosion upon landing.";
    }

    @Override
    public String getAuthor() {
        return ChatColor.DARK_RED + "Dreig_Michihi";
    }

    @Override
    public String getVersion() {
        return ChatColor.GOLD + "1.5.4";
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ProjectAddons.instance.getConfig().getBoolean(PATH + "Enabled");
    }

    @Override
    public void load() {}

    @Override
    public void stop() {}
}
