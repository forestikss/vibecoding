package ru.etc1337.api.events.impl.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.vector.Vector3d;
import ru.etc1337.api.events.Event;

@AllArgsConstructor @Getter @Setter
public class EventFireworkMotion extends Event {
    private LivingEntity entity;
    private FireworkRocketEntity fireworkRocketEntity;
    private Vector3d vector3d;
}
