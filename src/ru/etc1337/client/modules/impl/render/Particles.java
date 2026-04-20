package ru.etc1337.client.modules.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import ru.etc1337.api.TempColor;

import ru.etc1337.api.animations.advanced.Animation;
import ru.etc1337.api.animations.advanced.Easing;
import ru.etc1337.api.color.FixColor;
import ru.etc1337.api.events.Event;
import ru.etc1337.api.events.EventManager;
import ru.etc1337.api.events.impl.game.*;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.game.Chat;
import ru.etc1337.api.game.Maths;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.game.Translit;
import ru.etc1337.api.mods.fastrandom.FastRandom;
import ru.etc1337.api.render.Rect;
import ru.etc1337.api.render.Render;
import ru.etc1337.api.render.shaders.impl.Glow;
import ru.etc1337.api.settings.api.Parent;
import ru.etc1337.api.settings.impl.BooleanSetting;
import ru.etc1337.api.settings.impl.ModeSetting;
import ru.etc1337.api.settings.impl.MultiModeSetting;
import ru.etc1337.api.settings.impl.SliderSetting;
import ru.etc1337.api.timer.Timer;
import ru.etc1337.client.modules.Module;
import ru.etc1337.client.modules.api.ModuleCategory;
import ru.etc1337.client.modules.api.ModuleInfo;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleInfo(name = "Particles", description = "Частицы в мире", category = ModuleCategory.RENDER)
public class Particles extends Module {

    // Настройки
    private final MultiModeSetting types = new MultiModeSetting("Виды", this, "Звезда", "Снежинка", "Крест", "Луна", "Ромб", "Треугольник", "Круг", "Стрелка");
    private final MultiModeSetting typesSpawn = new MultiModeSetting("Спавнить", this, "Всегда", "Ходьба", "Удар", "Прыжок", "Снос Тотема");
    private final MultiModeSetting terms = new MultiModeSetting("Условия", this, "Кучковать при ходьбе", "Только игроки при ударе", "Только игроки при убийстве", "Взлетать при убийстве");
    private final BooleanSetting physic = new BooleanSetting("Физика", this);
    private final BooleanSetting glow = new BooleanSetting("Свечение", this);
    private final BooleanSetting smartCulling = new BooleanSetting("Умная отсечка", this);
    private final BooleanSetting limitParticles = new BooleanSetting("Лимит частиц", this);
    private final BooleanSetting particleCollision = new BooleanSetting("Столкновения частиц", this);

    private final SliderSetting скорость_всегда = new SliderSetting("Скорость Всегда", this, 2, 0.1f, 5, 0.1f);
    private final SliderSetting дальность_всегда = new SliderSetting("Дальность Всегда", this, 64, 1, 100, 1);
    private final SliderSetting count = new SliderSetting("Количество", this, 16, 1, 32, 1);
    private final SliderSetting liveTime = new SliderSetting("Время жизни", this, 4, 1, 5, 0.1f);
    private final SliderSetting strength = new SliderSetting("Сила", this, 0.8f, 0.1f, 1.0f, 0.1f);
    private final SliderSetting size = new SliderSetting("Размер", this, 0.5f, 0.01f, 0.5f, 0.01f);
    private final SliderSetting maxParticles = new SliderSetting("Макс. частиц", this, 200, 50, 500, 1);
    private final SliderSetting renderDistance = new SliderSetting("Дистанция рендера", this, 32, 8, 64, 1);

    // Оптимизированные коллекции
    private final ConcurrentLinkedQueue<Particle> particles = new ConcurrentLinkedQueue<>();
    private final List<Particle> renderList = new ArrayList<>();
    private final List<Particle> collisionList = new ArrayList<>();

    // Кэш для оптимизации
    private final Map<String, ResourceLocation> textureCache = new HashMap<>();
    private final FastRandom random = new FastRandom();

    private long lastSpawnTime = 0;

    private static final int SPAWN_INTERVAL = 50;
    private static final double GRAVITY = 0.00005;
    private static final double VELOCITY_MULTIPLIER = 0.01;
    private static final float PHYSICS_DAMPING = 0.999f;
    private static final float BOUNCE_DAMPING = 0.8f;
    private static final float GROUND_BOUNCE_DAMPING = 0.6f;
    private static final float PARTICLE_BOUNCE_DAMPING = 0.7f;
    private static final double COLLISION_DISTANCE = 0.3; // Дистанция для столкновений частиц
    private static final double COLLISION_DISTANCE_SQ = COLLISION_DISTANCE * COLLISION_DISTANCE;

    private void clear() {
        particles.clear();
        renderList.clear();
        collisionList.clear();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventWorldChanged) {
            clear();
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Ограничиваем частоту спавна частиц
        if (currentTime - lastSpawnTime < SPAWN_INTERVAL) {
            if (!(event instanceof EventRender3D)) return;
        }

        FixColor pColor = TempColor.getClientColor();

        // Оптимизированные обработчики событий
        handleSpawnEvents(event, pColor, currentTime);

        if (event instanceof EventRender3D e) {
            handleRenderEvent(e, currentTime);
        }
    }

    private void handleSpawnEvents(Event event, FixColor pColor, long currentTime) {
        // Проверяем лимит частиц
        if (limitParticles.isEnabled() && particles.size() >= maxParticles.getValue()) {
            return;
        }

        if (typesSpawn.get("Всегда").isEnabled() && event instanceof EventMotion) {
            spawnAlwaysParticles(pColor, currentTime);
        }

        if (typesSpawn.get("Ходьба").isEnabled() && event instanceof EventMotion &&
                mc.player.getMotion().lengthSquared() > 0.01) { // Используем lengthSquared для оптимизации
            spawnWalkingParticles(pColor);
        }

        if (typesSpawn.get("Удар").isEnabled() && event instanceof EventAttack e) {
            spawnAttackParticles(e, pColor);
        }

        if (typesSpawn.get("Прыжок").isEnabled() && event instanceof EventJump) {
            spawnJumpParticles(pColor);
        }

        if (typesSpawn.get("Снос Тотема").isEnabled() && event instanceof EventTotemPop e) {
            spawnTotemParticles(e, pColor);
        }
    }

    private void spawnAlwaysParticles(FixColor pColor, long currentTime) {
        // Ограничиваем частоту спавна
        if (currentTime - lastSpawnTime < SPAWN_INTERVAL) return;

        float range = дальность_всегда.getValue();
        int particleCount = Math.min((int) count.getValue(), 8); // Ограничиваем количество

        for (int i = 0; i < particleCount; i++) {
            Vector3d playerPos = mc.player.getPositionVec();
            Vector3d additional = new Vector3d(
                    playerPos.x + random.nextFloat(-range, range),
                    playerPos.y,
                    playerPos.z + random.nextFloat(-range, range)
            );

            BlockPos pos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(additional));
            Vector3d spawnPos = new Vector3d(
                    pos.getX() + random.nextFloat(0, 1),
                    mc.player.getPosY() + random.nextFloat(mc.player.getHeight(), range),
                    pos.getZ() + random.nextFloat(0, 1)
            );

            Vector3d velocity = calculateAlwaysVelocity();
            spawnParticle(spawnPos, velocity, pColor, physic.isEnabled());
        }

        lastSpawnTime = currentTime;
    }

    private Vector3d calculateAlwaysVelocity() {
        float mul = скорость_всегда.getValue();
        double yaw = Math.toRadians(mc.player.rotationYaw);

        double xY = Math.sin(yaw);
        double zY = -Math.cos(yaw);
        double xX = -Math.sin(yaw + Math.PI / 2);
        double zX = Math.cos(yaw + Math.PI / 2);

        double dirY = random.nextFloat(-5, 5);
        double dirX = random.nextFloat(-5, 5);

        return new Vector3d(
                (xY * dirY + xX * dirX) * mul,
                0,
                (zY * dirY + zX * dirX) * mul
        );
    }

    private void spawnWalkingParticles(FixColor pColor) {
        LivingEntity entity = mc.player;
        float motion = terms.get("Кучковать при ходьбе").isEnabled() ? 0.1f : 0.5f;
        int particleCount = Math.min((int) count.getValue(), 6);

        for (int i = 0; i < particleCount; i++) {
            Vector3d pos = entity.getPositionVec().add(
                    0,
                    terms.get("Кучковать при ходьбе").isEnabled() ? entity.getHeight() / 3F : random.nextFloat(0, entity.getHeight()),
                    0
            );

            Vector3d velocity = new Vector3d(
                    random.nextFloat(-motion, motion),
                    terms.get("Кучковать при ходьбе").isEnabled() && !physic.isEnabled() ?
                            random.nextFloat(-0.05F, 0.05F) : random.nextFloat(-1, 0),
                    random.nextFloat(-motion, motion)
            );

            spawnParticle(pos, velocity, pColor, physic.isEnabled());
        }
    }

    private void spawnAttackParticles(EventAttack e, FixColor pColor) {
        LivingEntity entity = e.getTarget();
        if (!(entity instanceof PlayerEntity) && terms.get("Только игроки при ударе").isEnabled()) return;

        int particleCount = Math.min((int) count.getValue(), 8);

        for (int i = 0; i < particleCount; i++) {
            Vector3d pos = entity.getPositionVec().add(0, random.nextFloat(0, entity.getHeight()), 0);
            Vector3d velocity = new Vector3d(
                    random.nextFloat(-1, 1),
                    random.nextFloat(-1, physic.isEnabled() ? 1 : 0),
                    random.nextFloat(-1, 1)
            );

            spawnParticle(pos, velocity, pColor, physic.isEnabled());
        }
    }

    private void spawnJumpParticles(FixColor pColor) {
        LivingEntity entity = mc.player;
        int particleCount = Math.min((int) count.getValue(), 6);

        for (int i = 0; i < particleCount; i++) {
            Vector3d pos = entity.getPositionVec().add(0, 0.01f, 0);
            Vector3d velocity = new Vector3d(
                    random.nextFloat(-1, 1),
                    physic.isEnabled() ? random.nextFloat(0, 1) : 0,
                    random.nextFloat(-1, 1)
            );

            spawnParticle(pos, velocity, pColor, true);
        }
    }

    private void spawnTotemParticles(EventTotemPop e, FixColor pColor) {
        LivingEntity entity = e.getEntity();
        int particleCount = Math.min((int) count.getValue(), 10);

        for (int i = 0; i < particleCount; i++) {
            Vector3d pos = entity.getPositionVec().add(0, random.nextFloat(0, entity.getHeight()), 0);
            Vector3d velocity = new Vector3d(
                    random.nextFloat(-1, 1),
                    terms.get("Взлетать при убийстве").isEnabled() ?
                            random.nextFloat(1, 3) : random.nextFloat(-1, physic.isEnabled() ? 1 : 0),
                    random.nextFloat(-1, 1)
            );

            spawnParticle(pos, velocity, pColor, physic.isEnabled());
        }
    }

    private void handleRenderEvent(EventRender3D e, long currentTime) {
        cleanupParticles();


          updateParticles(currentTime);
        updateParticleCollisions();


        renderOptimizedParticles(e.getMatrixStack());
    }

    private void cleanupParticles() {
        long lifetime = (long) (liveTime.getValue() * 1000);
        particles.removeIf(particle -> particle != null && particle.isExpired(lifetime));
    }

    private void updateParticles(long currentTime) {
        for (Particle particle : particles) {
            if (particle != null) {
                particle.optimizedUpdate(currentTime);
            }
        }
    }

    private void updateParticleCollisions() {
        if (!particleCollision.isEnabled()) return;

        collisionList.clear();
        collisionList.addAll(particles);

        // Проверяем коллизии между частицами
        for (int i = 0; i < collisionList.size(); i++) {
            Particle p1 = collisionList.get(i);
            if (p1 == null) continue;

            for (int j = i + 1; j < collisionList.size(); j++) {
                Particle p2 = collisionList.get(j);
                if (p2 == null) continue;

                // Проверяем дистанцию между частицами
                double distanceSq = p1.position.squareDistanceTo(p2.position);
                if (distanceSq < COLLISION_DISTANCE_SQ) {
                    handleParticleCollision(p1, p2);
                }
            }
        }
    }

    private void handleParticleCollision(Particle p1, Particle p2) {
        // Вычисляем направление столкновения
        Vector3d direction = p2.position.subtract(p1.position).normalize();

        // Вычисляем относительную скорость
        Vector3d relativeVelocity = p1.velocity.subtract(p2.velocity);
        double velocityAlongNormal = relativeVelocity.dotProduct(direction);

        // Если частицы уже удаляются друг от друга, не обрабатываем столкновение
        if (velocityAlongNormal > 0) return;

        // Коэффициент упругости
        float restitution = PARTICLE_BOUNCE_DAMPING;

        // Вычисляем импульс столкновения
        double impulse = -(1 + restitution) * velocityAlongNormal / 2; // Предполагаем равные массы

        // Применяем импульс к скоростям частиц
        Vector3d impulseVector = direction.mul(impulse);
        p1.velocity = p1.velocity.add(impulseVector);
        p2.velocity = p2.velocity.subtract(impulseVector);

        // Разделяем частицы, чтобы они не застревали друг в друге
        double overlap = COLLISION_DISTANCE - Math.sqrt(p1.position.squareDistanceTo(p2.position));
        if (overlap > 0) {
            Vector3d separation = direction.mul(overlap / 2);
            p1.position = p1.position.subtract(separation);
            p2.position = p2.position.add(separation);
        }
    }

    private void renderOptimizedParticles(MatrixStack matrix) {
        if (particles.isEmpty()) return;

        // Подготавливаем список для рендера с фрустум кулингом
        renderList.clear();
        Vector3d playerPos = mc.player.getPositionVec();
        double renderDistSq = renderDistance.getValue() * renderDistance.getValue();

        for (Particle particle : particles) {
            if (particle != null && shouldRenderParticle(particle, playerPos, renderDistSq)) {
                renderList.add(particle);
            }
        }

        if (renderList.isEmpty()) return;

        // Батчим рендер
        setupRenderState();
        matrix.push();

        for (Particle particle : renderList) {
            try {
                renderParticleOptimized(matrix, particle);
            } catch (Exception e) {
                // Игнорируем ошибки рендера отдельных частиц
            }
        }

        matrix.pop();
        resetRenderState();
    }

    private boolean shouldRenderParticle(Particle particle, Vector3d playerPos, double renderDistSq) {
        if (!smartCulling.isEnabled()) return true;

        // Проверяем расстояние
        double distSq = particle.position.squareDistanceTo(playerPos);
        if (distSq > renderDistSq) return false;

        // Проверяем, находится ли частица в поле зрения
        return Render.isInView(particle.position);
    }

    private void renderParticleOptimized(MatrixStack matrix, Particle particle) {
        float alpha = particle.getAlpha();
        if (alpha < 0.01f) return; // Не рендерим почти прозрачные частицы

        FixColor color = particle.getColor(alpha);
        Vector3d pos = particle.position;
        float particleSize = size.getValue();

        matrix.push();
        try {
            Render.setupOrientationMatrix(matrix, (float) pos.x, (float) pos.y, (float) pos.z);
            matrix.rotate(mc.getRenderManager().getCameraOrientation());
            matrix.push();
            try {
                matrix.rotate(Vector3f.ZP.rotationDegrees(180F));

                // Оптимизированная ротация
                if (particle.shouldRotate()) {
                    matrix.rotate(Vector3f.ZP.rotationDegrees(particle.getRotation()));
                }

                matrix.push();
                try {
                    matrix.translate(0, -particleSize, -particleSize);

                    // Кэшированная текстура
                    ResourceLocation texture = getParticleTexture(particle.type);

                    if (glow.isEnabled()) {
                        float glowSize = 1;
                        Glow.draw(matrix, new Rect(
                                        -particleSize,
                                        -particleSize,
                                        0.25f,
                                        0.25f
                                ), glowSize, 0.55f, glowSize / 2,
                                color.alpha(55),
                                color.alpha(55),
                                color.alpha(55),
                                color.alpha(55));
                    }

                    Render.drawImage(matrix, texture, -particleSize, -particleSize, 0,
                            particleSize * 2, particleSize * 2, color);
                } finally {
                    matrix.pop();
                }
            } finally {
                matrix.pop();
            }
        } finally {
            matrix.pop();
        }
    }

    private ResourceLocation getParticleTexture(String type) {
        return textureCache.computeIfAbsent(type, t -> {
            String particleName = Translit.translitRuToEn(t);
            return new ResourceLocation("minecraft", String.format("dreamcore/images/particles/%s.png", particleName));
        });
    }

    private ResourceLocation getGlowTexture() {
        return textureCache.computeIfAbsent("glow", t ->
                new ResourceLocation("minecraft", "dreamcore/images/glow.png"));
    }

    @Override
    public void onDisable() {
        clear();
        textureCache.clear();
        super.onDisable();
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableCull();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    private void resetRenderState() {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.clearCurrentColor();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
    }

    private void spawnParticle(Vector3d position, Vector3d velocity, FixColor color, boolean physic) {
        if (limitParticles.isEnabled() && particles.size() >= maxParticles.getValue()) {
            return;
        }

        BooleanSetting randomElement = types.getRandomEnabledElement();
        if (randomElement == null) return;

        float particleSize = 0.03f;
        particles.add(new Particle(
                randomElement.getName(),
                position.add(0, particleSize, 0),
                velocity,
                color,
                particleSize,
                physic,
                strength.getValue(),
                liveTime.getValue()
        ));
    }

    // Оптимизированный класс частицы
    private class Particle {
        private final long spawnTime;
        private final long addTime;
        private final String type;
        private Vector3d position;
        private Vector3d velocity;
        private final FixColor baseColor;
        private final float size;
        private final boolean physic;
        private final float strength;
        private final float liveTime;
        private final Timer timer;
        private final Animation animation;
        private long lastUpdateTime;

        // Кэшированные значения для оптимизации
        private final boolean rotatable;
        private final float strengthMultiplier;
        private final long lifetimeMs;

        public Particle(String type, Vector3d position, Vector3d velocity, FixColor color,
                        float size, boolean physic, float strength, float liveTime) {
            this.spawnTime = System.currentTimeMillis();
            this.addTime = random.nextLong(0, 2000);
            this.type = type;
            this.position = position;
            this.velocity = velocity.mul(VELOCITY_MULTIPLIER);
            this.baseColor = color;
            this.size = size;
            this.physic = physic;
            this.strength = strength;
            this.liveTime = liveTime;
            this.timer = new Timer();
            this.animation = new Animation().setEasing(Easing.BOTH_SINE);
            this.lastUpdateTime = spawnTime;

            // Кэшируем значения
            this.rotatable = type.equals("Треугольник") || type.equals("Ромб") || type.equals("Крест") ||
                    type.equals("Звезда") || type.equals("Снежинка") || type.equals("Луна");
            this.strengthMultiplier = 10.0f - 8 * strength;
            this.lifetimeMs = (long) (liveTime * 1000);
        }

        public boolean isExpired(long lifetime) {
            return timer.passed(lifetime + addTime);
        }

        public void optimizedUpdate(long currentTime) {
            float deltaTime = (currentTime - lastUpdateTime) / strengthMultiplier;

            if (physic) {
                // Оптимизированная физика
                updatePhysics(deltaTime);
            }

            position = position.add(velocity.mul(deltaTime));
            lastUpdateTime = currentTime;
        }

        private void updatePhysics(float deltaTime) {
            // Проверка коллизий с блоками оптимизирована
            if (Player.isBlockSolid(position.x, position.y, position.z + velocity.z)) {
                velocity = velocity.mul(1, 1, -BOUNCE_DAMPING);
            }
            if (Player.isBlockSolid(position.x, position.y + velocity.y, position.z)) {
                velocity = velocity.mul(PHYSICS_DAMPING, -GROUND_BOUNCE_DAMPING, PHYSICS_DAMPING);
            }
            if (Player.isBlockSolid(position.x + velocity.x, position.y, position.z)) {
                velocity = velocity.mul(-BOUNCE_DAMPING, 1, 1);
            }

            // Применяем гравитацию и затухание
            velocity = velocity.mul(PHYSICS_DAMPING).subtract(new Vector3d(0, GRAVITY, 0));
        }

        public float getAlpha() {
            long currentTime = System.currentTimeMillis();
            animation.setSpeed((int) (liveTime * 100));
            animation.setForward(!timer.passed(lifetimeMs + addTime));

            float animationAlpha = animation.get();
            float pulseAlpha = 0.3f + 0.7f * (float) ((Math.sin((currentTime - spawnTime + addTime) / 200.0) + 1.0) / 2.0);

            return animationAlpha * pulseAlpha;
        }

        public FixColor getColor(float alpha) {
            return baseColor.alpha((int) (alpha * 255));
        }

        public boolean shouldRotate() {
            return rotatable;
        }

        public float getRotation() {
            return (float) ((System.currentTimeMillis() - spawnTime) / 20.0);
        }
    }
}