package ru.etc1337.api.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import ru.etc1337.api.TempColor;
import ru.etc1337.api.events.impl.render.EventRender3D;
import ru.etc1337.api.game.Player;
import ru.etc1337.api.interfaces.QuickImports;
import java.util.ArrayList;

public class GhostRenderer3D implements QuickImports {

    private Vector3d prevPosition = Vector3d.ZERO;
    @Getter
    @Setter
    private Vector3d position, motion;
    private final ArrayList<Vector4f> tail = new ArrayList<>();
    @Getter @Setter
    private float alpha = 1;
    private final float size;

    public GhostRenderer3D(Vector3d position, Vector3d motion, float size) {
        this.position = position;
        this.motion = motion;
        this.size = size;
    }

    public void render(EventRender3D e) {
        this.prevPosition = new Vector3d(this.position.x, this.position.y, this.position.z);
        this.position = this.position.add(motion);

        MatrixStack ms = e.getMatrixStack();
        float size = this.size;
        float length = 30;

        double x = this.position.x;
        double y = this.position.y;
        double z = this.position.z;

        tail.add(new Vector4f((float) x, (float) y + 0.7f, (float) z, length));

        ArrayList<Vector4f> vec4f = new ArrayList<>();
        for (int i = 0; i < this.tail.size(); i++) {
            Vector4f vec = this.tail.get(i);
            if (vec.getW() > 0) {
                float miniSize = size * vec.getW() / length;

                double posX = vec.getX() - mc.getRenderManager().info.getProjectedView().x;
                double posY = vec.getY() - mc.getRenderManager().info.getProjectedView().y;
                double posZ = vec.getZ() - mc.getRenderManager().info.getProjectedView().z;

                if (Render.isInView(new Vector3d(vec.getX(), vec.getY(), vec.getZ()))) {
                    ms.push();
                    ms.translate(posX, posY, posZ);
                    ms.rotate(mc.getRenderManager().info.getRotation());
                    Render.drawCleanImage(ms, (float) -miniSize / 2, -miniSize / 2, (float) -miniSize / 2, miniSize, miniSize, TempColor.getClientColor().alpha((vec.getW() / length * alpha) * 255));
                    ms.pop();
                }

                vec.set(vec.getX(), vec.getY() + 0.004f / Math.max((float) Minecraft.getDebugFPS(), 5) * 300, vec.getZ(), vec.getW() - 0.3f / Math.max((float) Minecraft.getDebugFPS(), 5) * 300);
                if (vec.getW() <= 0)
                    vec4f.add(vec);
            }
        }

        if (this.alpha < 0) {
            //this.motion = this.motion.mul(-1,-1,-1);
            //this.alpha = 1;
        }

        for (Vector4f vec : vec4f) {
            this.tail.remove(vec);
        }
    }

    public void update() {
        this.alpha -= 0.001 / Math.max((float) Minecraft.getDebugFPS(), 5) * 300;
        this.motion = this.motion.mul(0.95 / Math.max((float) Minecraft.getDebugFPS(), 5) * 300, 0.95 / Math.max((float) Minecraft.getDebugFPS(), 5) * 300, 0.95 / Math.max((float) Minecraft.getDebugFPS(), 5) * 300);
    }

}
