package ru.hachclient.modules.impl.combat;

import static ru.hachclient.Hachclient.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;
import ru.hachclient.events.impl.*;
import ru.hachclient.mixin.accessors.ClientPlayerInteractionManagerAccessor;
import ru.hachclient.mixin.accessors.FireworkRocketAccesor;
import ru.hachclient.mixin.accessors.LivingEntityAccessor;
import ru.hachclient.modules.Module;
import ru.hachclient.modules.ModuleManager;
import ru.hachclient.modules.ModuleType;
import ru.hachclient.modules.settings.impl.BooleanSetting;
import ru.hachclient.modules.settings.impl.ModeSetting;
import ru.hachclient.modules.settings.impl.MultiBooleanSetting;
import ru.hachclient.modules.settings.impl.NumberSetting;
import ru.hachclient.render.Render;
import ru.hachclient.rotation.GCDUtil;
import ru.hachclient.rotation.RotationUtil;
import ru.hachclient.utils.*;
import ru.hachclient.utils.game.HealthResolver;
import ru.hachclient.utils.game.PlayerUtil;
import ru.hachclient.utils.math.TimerUtil;
import ru.hachclient.utils.mixins.ConnectionMixinHelper;
import ru.hachclient.utils.mixins.ILivingEntity;
import ru.hachclient.utils.rotation.RayTraceUtil;
import ru.hachclient.utils.targets.TargetUtil;
import ru.hachclient.utils.targets.TargetValidator;

// by ls5sq
public class ElytraTarget extends Module {
  public ElytraTarget() {
    super("ElytraTarget", "", ModuleType.COMBAT);
    addSettings(autofirework, fireworkdelay, distance, antiaim, antiaimVector, antiaimCondition, fakelag, sendmisses, delayedAntiaim, freeze, rideEnemy, autoStoyak, fakeRot, shavel, pingAdaptive, debug);
  }

  BooleanSetting autofirework = new BooleanSetting("Auto firework", true);
  NumberSetting fireworkdelay = new NumberSetting("hachclient.elytratarget.fireworkdelay", 500, 50, 1000, 100).setVisible(() -> autofirework.get());
  NumberSetting distance = new NumberSetting("hachclient.elytratarget.distance", 50, 1, 100, 1);

  BooleanSetting antiaim = new BooleanSetting("Anti-aim", true);
  ModeSetting antiaimVector = new ModeSetting("Anti-aim vector", "Perpendicular", "Perpendicular", "Far from target").setVisible(() -> antiaim.get());
  MultiBooleanSetting antiaimCondition = new MultiBooleanSetting("Activate anti-aim when",
      new BooleanSetting("Target on ground", true),
      new BooleanSetting("Elytra pvp", true)
  ).setVisible(() -> antiaim.get());

  BooleanSetting fakelag = new BooleanSetting("Use lag", true);
  BooleanSetting sendmisses = new BooleanSetting("Rage bait target", true);
  BooleanSetting delayedAntiaim = new BooleanSetting("Delay anti-aim", false);
  BooleanSetting freeze = new BooleanSetting("Freeze player", false);
  BooleanSetting rideEnemy = new BooleanSetting("Ride enemy", false);
  public BooleanSetting autoStoyak = new BooleanSetting("Auto motion", false);
  public BooleanSetting fakeRot = new BooleanSetting("Fake rotation", false);
  BooleanSetting shavel = new BooleanSetting("Shavel message", false);
  BooleanSetting pingAdaptive = new BooleanSetting("Ping adaptive", false);
  BooleanSetting debug = new BooleanSetting("Debug", false);

  public static LivingEntity target = null;

  public boolean rotating = true;
  public Vec3d resolved = Vec3d.ZERO;

  final TargetValidator validator = entity -> {
    if (!(entity instanceof AbstractClientPlayerEntity p)) return false;
    if (p instanceof ClientPlayerEntity || instance().getFriendManager().friend(p)) return false;
    return mc.player.distanceTo(entity) <= distance.get() && !entity.isRemoved() && entity.deathTime <= 0;
  };

  final TimerUtil fireworktimer = new TimerUtil();
  final TimerUtil paste = new TimerUtil();
  final TimerUtil timer = new TimerUtil();
  final TimerUtil targetLeaving = new TimerUtil();
  final Random random = new Random();

  final CopyOnWriteArrayList<Packet<?>> packets = new CopyOnWriteArrayList<>();
  Vec3d lastPosPinged, lastResolved;
  LivingEntity lastKilled = null;

  long lastPinged = 0;
  int calculatedPing = 0;

  int missamount = -1;
  int misses = 0, hits = 0;
  final ArrayList<HitData> history = new ArrayList<>();
  private enum HitType { MISS, HIT }
  private record HitData(float health, HitType hit) {}

  Vec3d antiaimvec = Vec3d.ZERO;
  boolean antiaimDirection = false;
  int attacking = 0;

  final ArrayList<Vec3d> antiaimvectors = new ArrayList<>(Arrays.asList(
      new Vec3d(0, 18, 0),
      new Vec3d(0, -18, 0),
      new Vec3d(-18, 0.1, 0),
      new Vec3d(18, 0.1, 0),
      new Vec3d(0, 0.1, -18),
      new Vec3d(0, 0.1, 18)
  ));

  @Override
  public void enabled() {
    super.enabled();
    if (IClient.nullCheck()) return;

    target = null;
    packets.clear();
    timer.reset();
    if (ModuleManager.fakeLag.state()) {
      ModuleManager.fakeLag.state(false);
      instance().getNotificationManager().addNotification("Module FakeLag was disabled due incompatible", IconsMap.WARNING, 1500);
    }

    TIMER = 1.0f;
  }

  @Override
  public void disabled() {
    super.disabled();
    if (IClient.nullCheck()) return;
    target = null;
    send();
    timer.reset();

    TIMER = 1.0f;
  }

  public void onInput(EventInput e) {
    if (PlayerUtil.isElytraEquipped())
      e.setJump(mc.player.age % 2 == 0);
  }

  public void onTick(EventUpdate e) {
    if (System.currentTimeMillis() - lastPinged > 1000) {
      ConnectionMixinHelper.NO_EVENT = true;
      mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(1488, ""));
      ConnectionMixinHelper.NO_EVENT = false;
      lastPinged = System.currentTimeMillis();
    }

    if (target == null)
      return;

    boolean stoyak = isStanding(0.07);
    Vec3d origin = serverOrigin();

    if (freeze.get() && mc.player.getPos().distanceTo(target.getPos()) <= 2.5f) {
      if ((target.getVelocity().x < 0.001 && target.getVelocity().z < 0.001) || (!target.isGliding() && !mc.player.isOnGround()) || stoyak) {
        mc.player.setVelocity(Vec3d.ZERO);
      }
    }

    int predictTicks = leaving() ? lastResolveTicks() + (pingAdaptive.get() ? calculatedPing / 50 : 0) : 0;
    resolved = RotationUtil.predictElytraPos(target, origin, Math.min(3, predictTicks));

    if (mc.player.getEyePos().distanceTo(resolved) < 3 && mc.player.getAttackCooldownProgress(0.5f) >= 0.99) {
      mc.interactionManager.attackEntity(mc.player, target);
      mc.player.swingHand(Hand.MAIN_HAND);
    }
  }

  public void onUpdate(EventMove event) {
    if (lastPosPinged == null)
      lastPosPinged = mc.player.getPos();
    if (lastResolved == null)
      lastResolved = Vec3d.ZERO;

    if (shavel.get() && target != null && target.deathTime > 0 && target != lastKilled) {
      mc.getNetworkHandler().sendChatMessage(String.format("!%s похоже, у вас щавель!", target.getName().getString()));
      lastKilled = target;
    }

    if (target == null || !validator.validate(target)) {
      target = TargetUtil.findTarget(TargetUtil.Sorting.DISTANCE, distance.get(), validator);
      if (target != TargetUtil.prevtarget)
        missamount = -1;
    }
    if (!PlayerUtil.isElytraEquipped() || target == null || !mc.player.isGliding()) {
      send();
      return;
    }

    boolean stoyak = isStanding(0.3);
    int t = mc.player.age - mc.player.getLastAttackTime();

    useFirework();

    if (antiaim.get()) {
      double distToTarget = mc.player.getEyePos().distanceTo(new Vec3d(target.getLerpTargetX(), target.getLerpTargetY(), target.getLerpTargetZ()));
      if ((distToTarget > 3 && mc.player.getAttackCooldownProgress(0.5f) > 0.7 && !rotating) || leaving()) {
        if (!rotating)
          send();
        rotating = true;
      }
      if (mc.player.hurtTime > 5 && mc.player.getAttackCooldownProgress(0.5f) == 1.0f && target.handSwinging && rotating && !leaving()) {
        antiaim(true);
      }
      if (target.handSwinging)
        targetLeaving.reset();
    } else {
      rotating = true;
      send();
    }

    if (t == 2 && delayedAntiaim.get() && antiaim.get() && antiaimAllowed(stoyak)) {
      antiaim(true);
    }

    if (target instanceof AbstractClientPlayerEntity pl) {
      trackMisses(pl);
    }

    // Ride enemy: зависаем над целью, гасим горизонтальную скорость.
    if (rideEnemy.get() && mc.player.distanceTo(target) < 2 && !leaving()) {
      mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
    }
    // Auto motion: если цель стоит или не улетает, а мы над ней, прижимаемся вниз.
    if (autoStoyak.get() && mc.player.distanceTo(target) < 3 && (!leaving() || stoyak) && mc.player.getY() > target.getY()) {
      mc.player.setVelocity(0, -0.08, 0);
      send();
    }

    if (!stoyak) {
      if (t < 10
          || mc.player.getAttackCooldownProgress(0.5f) < 0.2
          || mc.player.getPos().distanceTo(lastPosPinged) > 8
          || timer.hasTimeElapsed(600, true)
          || leaving()) {
        send();
      }
    } else if (mc.player.getPos().distanceTo(resolved) > 6) {
      send();
    }
    if (timer.hasTimeElapsed(700, true) || !fakelag.get() || !antiaimAllowed(stoyak)) {
      send();
    }

    Vec3d origin = serverOrigin();
    Vec3d targetTo = origin.add(target.getVelocity().multiply(leaving() ? 3 : 1));
    Vector2f raw = RotationUtil.lookAt(targetTo.subtract(mc.player.getEyePos()).toVector3f());
    if (!rotating && !leaving()) {
      raw = RotationUtil.lookAt(antiaimvec.toVector3f());
    }
    instance().getRotation().rotateForce((float) GCDUtil.getSensitivity(raw.x), MathHelper.clamp((float) GCDUtil.getSensitivity(raw.y), -90, 90));

    if (attacking > 0 && attacking < 10) {
      boolean hit = target.hurtTime > 0;
      if (hit) hits++; else misses++;
      history.add(new HitData(HealthResolver.resolve(target), hit ? HitType.HIT : HitType.MISS));
      attacking = -1;
    }
    if (attacking == (pingAdaptive.get() ? 20 - Math.min(calculatedPing / 50, 8) : 16))
      send();
    if (attacking > 0)
      attacking--;

    if (mc.player.isGliding())
      event.ground = false;

    if (history.size() > 20)
      history.removeFirst();
  }

  // Ракета запускается, только когда наша прошлая уже догорела, и не чаще fireworkdelay.
  private void useFirework() {
    if (!autofirework.get() || hasActiveFirework() || !fireworktimer.hasTimeElapsed((long) fireworkdelay.get()))
      return;

    int slot = -1;
    for (int i = 0; i < 9; i++) {
      if (mc.player.getInventory().getStack(i).isOf(Items.FIREWORK_ROCKET)) {
        slot = i;
        break;
      }
    }
    if (slot == -1)
      return;

    // Мимо очереди фейклага: ракета должна уйти сразу.
    ConnectionMixinHelper.NO_EVENT = true;
    int last = mc.player.getInventory().selectedSlot;
    mc.player.getInventory().selectedSlot = slot;
    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    mc.player.getInventory().selectedSlot = last;
    ((ClientPlayerInteractionManagerAccessor) mc.interactionManager).hachclient$syncSelectedSlot();
    ConnectionMixinHelper.NO_EVENT = false;

    fireworktimer.reset();
  }

  private boolean hasActiveFirework() {
    for (Entity entity : mc.world.getEntities()) {
      if (entity instanceof FireworkRocketEntity rocket && !rocket.isRemoved()
          && ((FireworkRocketAccesor) rocket).getShooter() instanceof ClientPlayerEntity) {
        return true;
      }
    }
    return false;
  }

  // Fake rotation: во время антиаима ракета тянет в сторону антиаима, а не на цель.
  public void onRocketBoost(EventRocketBoost e) {
    if (fakeRot.get() && target != null && !rotating) {
      Vector2f raw = RotationUtil.calculate(mc.player.getPos().add(antiaimvec));
      e.yaw = raw.x;
      e.pitch = raw.y;
    }
  }

  private void trackMisses(AbstractClientPlayerEntity pl) {
    if (pl.handSwinging && mc.player.hurtTime == 0 && lastResolved.distanceTo(mc.player.getPos()) < 4 && paste.hasTimeElapsed(300)) {
      instance().getNotificationManager().addNotification(pl.getName().getString() + " missed hit due paste issue", IconsMap.TRASH, 1500);
      if (sendmisses.get())
        missamount += 1;
      paste.reset();
    }
    if (leaving() && sendmisses.get() && missamount > 3) {
      String name = pl.getGameProfile().getName();
      int count = missamount + 1;
      String message = switch (random.nextInt(7)) {
        case 0 -> String.format("!%s поздравляем вы набрали %s мисов!", name, count);
        case 1 -> String.format("!%s сударь, вы соизволили миснуть по мне %s раз!", name, count);
        case 2 -> String.format("!%s моё почтение уважаемый, мой калькулятор показывает %s миссов вас по мне!", name, count);
        case 3 -> String.format("!%s это как... даже моя бабка не мисает %s раз подряд", name, count);
        case 4 -> String.format("!%s, по астрологическому калькулятору %s миссов = паста", name, count);
        case 5 -> String.format("!%s, похоже вы сударь с говном? Иначе как вы объясните %s миссов", name, count);
        default -> String.format("!%s кажется вы помацали мой сосок иначе почему вы набираете %s миссов???", name, count);
      };
      mc.getNetworkHandler().sendChatMessage(message);
      missamount = -1;
    }
  }

  public void onAttack(EventAttack e) {
    attacking = 20;
    if (target == null || delayedAntiaim.get()) return;
    if (antiaimAllowed(isStanding(0.07)))
      antiaim(true);
  }

  public void onPacket(EventPacket packet) {
    if (packet.isReceive() && packet.getPacket() instanceof CommandSuggestionsS2CPacket p && p.id() == 1488) {
      calculatedPing = (int) (System.currentTimeMillis() - lastPinged);
    }
    if (mc.player == null || mc.world == null) {
      packets.clear();
      return;
    }
    if (target == null || mc.player.age < 60) {
      // Раньше очередь здесь просто очищалась: задержанные пакеты, включая клики
      // по инвентарю, терялись, и сервер оставлял нагрудник на курсоре.
      flush();
      return;
    }
    if (packet.isSend() && fakelag.get()) {
      Packet<?> p = packet.getPacket();
      if (isInventoryPacket(p)) {
        // Инвентарь никогда не задерживаем: сначала отправляем очередь, чтобы
        // сохранить порядок, потом пропускаем сам пакет.
        flush();
        return;
      }
      if (!antiaimAllowed(isStanding(0.07)))
        return;
      if (p instanceof ClientCommandC2SPacket c
          && (c.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING || c.getMode() == ClientCommandC2SPacket.Mode.STOP_SPRINTING))
        return;
      packets.add(p);
      packet.cancel();
    }
    if (packet.isReceive() && packet.getPacket() instanceof DeathMessageS2CPacket p && p.playerId() == mc.player.getId()) {
      toggle();
      send();
    }
  }

  public void onRender(EventRender e) {
    if (e.is2d() && debug.get()) {
      String predict = target != null ? String.valueOf(lastResolveTicks() + calculatedPing / 50) : "no target";
      Render.text("Elytra Target debug:", 20, 5, "sfmedium", 8, Color.WHITE);
      Render.text("ping: " + calculatedPing + " \\ ticks: " + (calculatedPing / 50) + " \\ predict tick: " + predict, 100, 5, "sfmedium", 8, Color.WHITE);
      Render.text("hits: " + hits + " \\ misses: " + misses, 120, 15, "sfmedium", 8, Color.WHITE);
      Render.text("hit stat: " + ((float) hits / Math.max(1, misses)), 120, 25, "sfmedium", 8, Color.WHITE);
      for (int i = 0; i < history.size(); i++) {
        HitData data = history.get(i);
        boolean success = data.hit() == HitType.HIT;
        Render.text(success ? "hit target, health - " + data.health() : "missed hit target", 20, 20 + i * 12, "sfbold", 7, Color.WHITE);
        Render.text(success ? IconsMap.CHECK.name : IconsMap.XMARK.name, 10, 21 + i * 12, "hudicon", 7, success ? Color.green : Color.red);
      }
      return;
    }
    if (target == null || !e.is3d())
      return;

    RenderSystem.disableCull();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.lineWidth(2);

    Vec3d lookat = serverOrigin().add(target.getVelocity().multiply(3));
    Render.box(e.position, target.getBoundingBox().offset(lookat.subtract(target.getPos())), Color.YELLOW.getRGB());
    if (fakelag.get() && lastPosPinged != null)
      Render.box(e.position, mc.player.getBoundingBox().offset(lastPosPinged.subtract(mc.player.getPos())), Color.GREEN.getRGB());
    Render.box(e.position, target.getBoundingBox().offset(resolved.subtract(target.getPos())), Color.RED.getRGB());
  }

  private static boolean isInventoryPacket(Packet<?> p) {
    return p instanceof ClickSlotC2SPacket
        || p instanceof CloseHandledScreenC2SPacket
        || p instanceof UpdateSelectedSlotC2SPacket
        || p instanceof CreativeInventoryActionC2SPacket
        || p instanceof ButtonClickC2SPacket;
  }

  private void flush() {
    if (packets.isEmpty()) return;
    if (mc.getNetworkHandler() == null) {
      packets.clear();
      return;
    }
    ConnectionMixinHelper.NO_EVENT = true;
    for (Packet<?> queued : packets) {
      mc.getNetworkHandler().sendPacket(queued);
    }
    packets.clear();
    ConnectionMixinHelper.NO_EVENT = false;
  }

  private void send() {
    flush();
    lastPosPinged = mc.player.getLerpedPos(mc.getRenderTickCounter().getTickDelta(true));
    lastResolved = resolved;
  }

  // Цель стоит на месте: не летит или почти не двигается и недавно резолвилась.
  private boolean isStanding(double threshold) {
    Vec3d changed = new Vec3d(target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ);
    return !target.isGliding() || (changed.length() < threshold && lastResolveTicks() < 4);
  }

  private boolean antiaimAllowed(boolean stoyak) {
    return (antiaimCondition.get(0) && stoyak) || antiaimCondition.get(1);
  }

  private int lastResolveTicks() {
    return (int) ((ILivingEntity) target).getLastresolve().getTime() / 50;
  }

  private Vec3d serverOrigin() {
    LivingEntityAccessor accessor = (LivingEntityAccessor) target;
    return new Vec3d(accessor.getServerX(), accessor.getServerY(), accessor.getServerZ())
        .add(0, mc.player.isGliding() ? target.getHeight() : 0, 0);
  }

  public boolean leaving() {
    return targetLeaving.hasTimeElapsed(2000) && target.isGliding();
  }

  public void antiaim(boolean now) {
    if (now)
      rotating = false;

    switch (antiaimVector.getIndex()) {
      case 1 -> {
        // Точка, куда смотрит цель на нашей дистанции: уходим от неё подальше.
        Vec3d point = target.getPos().add(target.getRotationVector().multiply(mc.player.distanceTo(target)));
        ArrayList<Vec3d> sort = new ArrayList<>(antiaimvectors);
        sort.removeIf(vec -> RayTraceUtil.point(target.getEyePos(), target.getEyePos().add(vec.multiply(10))).getType() == HitResult.Type.BLOCK);
        sort.sort(Comparator.comparingDouble(vec -> target.getPos().add(vec).distanceTo(point) + mc.player.getPos().distanceTo(target.getPos().add(vec))));

        if (sort.size() > 2) {
          Vec3d result = sort.get(random.nextInt(sort.size()));
          antiaimvec = new Vec3d(
              result.x,
              antiaimDirection && RayTraceUtil.point(target.getEyePos(), target.getEyePos().add(0, 10, 0)).getType() != HitResult.Type.BLOCK ? 1 : MathHelper.clamp(result.y, 0.2, 999),
              result.z);
        } else {
          antiaimvec = new Vec3d(
              antiaimDirection ? 0 : random.nextInt(-1, 1),
              antiaimDirection && RayTraceUtil.point(target.getEyePos(), target.getEyePos().add(0, 6, 0)).getType() != HitResult.Type.BLOCK ? 1 : MathHelper.clamp(random.nextInt(-1, 1), 0.2, 999),
              antiaimDirection ? 0 : random.nextInt(-1, 1));
        }
      }
      case 0 -> {
        for (Vec3d vec : antiaimvectors) {
          if (vec.y == -18) continue;
          vec = vec.add(0, 0.25, 0);
          if (RayTraceUtil.visible(mc.player.getPos().add(vec)) && vec.y < 400 && !antiaimvec.equals(vec)) {
            antiaimvec = vec;
            return;
          }
        }
      }
    }
  }
}
