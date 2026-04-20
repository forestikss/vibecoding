package ru.etc1337.client.modules;

import lombok.Getter;

import ru.etc1337.Client;
import ru.etc1337.api.events.EventManager;
import ru.etc1337.client.modules.impl.combat.*;
import ru.etc1337.client.modules.impl.movement.*;
import ru.etc1337.client.modules.impl.player.*;
import ru.etc1337.client.modules.impl.render.*;
import ru.etc1337.client.modules.impl.misc.*;
import ru.etc1337.protection.interfaces.Include;
import ru.kotopushka.compiler.sdk.annotations.Compile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final ConcurrentMap<Class<? extends Module>, Module> instances = new ConcurrentHashMap<>();

    public List<Module> getModules() { return modules; }
    public ConcurrentMap<Class<? extends Module>, Module> getInstances() { return instances; }

    @Include
    public void init() {
        if (EventManager.blocked) return;

        // Combat
        registerModules(
                new KillAura(),
                new TargetStrafe(),
                new ElytraTarget(),
                new Beautifully(),
                new PearlTarget(),
                new KeepSprint(),
                new AimBot(),
                new AutoSwap(),
                new AutoTotem(),
                new NoEntityTrace(),
                new AntiBot(),
                new Criticals(),
                new HitBoxes(),
                new NoVelocity(),
                new AutoExplosion(),
                new NoFriendDamage(),

        // Movement
                new NoSlow(),
                new AutoSprint(),
                new SuperFirework(),
                new GrimGlide(),
                new NoFall(),
                new Freeze(),
                new ElytraFly(),
                new ElytraMotion(),
                new AntiTarget(),
                new NoClip(),
                new Timer(),
                new Speed(),
                new ElytraBounce(),
                new ElytraJump(),
                new NoWeb(),
                new DragonFly(),
                new GuiMove(),

        // Player
                new ElytraHelper(),
                new NoDelay(),
                new NoPush(),
                new AutoTool(),
                new AutoGApple(),
                new TotemAnimation(),
                new FreeCam(),
                new ClickPearl(),
                new ClickFriend(),
                new AutoRespawn(),
                new Blink(),
                new AutoAccept(),
                new AimingItems(),
                new KTLeave(),
                new ItemsFix(),
                new NoServerRotation(),
                new Nuker(),
                new TapeMouse(),

        // Render
                new Interface(),
                new SwingAnimations(),
                new ViewModel(),
                new AspectRatio(),
                new CustomModels(),
                new ChunkAnimator(),
                new ItemPhysics(),
                new Projectiles(),
                new NoRender(),
                new EntityESP(),
                new SeeInvisibles(),
                new CustomWorld(),
                new CustomTime(),
                new FireFly(),
                new Torus(),
                new TargetESP(),
                new Particles(),
                new Arrows(),
                new JumpCircle(),
                new FireworkESP(),
//                new Alerts(),
                new Gamma(),

        // Misc
                new ItemScroller(),
                new LeaveTracker(),
                new ScoreboardHealth(),
                new ClientSounds(),
                new AutoContract(),
                new NameProtect(),
                new DeathCoordinates(),
                // new PacketOpen(),
                new VoiceChat(),
                new AntiAFK(),
                new NoInteract(),
                new TpsSync(),
                new TotemPopCounter(),
                new AutoJoin(),
                new RPSpoofer(),
                new FpsLimit(),
                new Optimization(),
                new Bots());
    }

    @Compile
    public ModuleManager start() {
        init();
        return this;
    }

    @Compile
    public void registerModules(Module... modules) {
        getModules().addAll(Arrays.asList(modules));
    }

    public <T extends Module> T find(final Class<T> clazz) {
        return this.modules.stream()
                .filter(module -> clazz.isAssignableFrom(module.getClass()))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }


    public <T extends Module> T get(Class<T> clazz) {
        return clazz.cast(instances.computeIfAbsent(clazz, this::find));
    }

    public <T extends Module> T get(final String name) {
        return this.modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .map(module -> (T) module)
                .findFirst()
                .orElse(null);
    }
}
