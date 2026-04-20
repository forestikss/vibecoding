package de.maxhenkel.voicechat.permission;

import net.minecraft.entity.player.ServerPlayerEntity;

public class FabricPermissionManager extends PermissionManager {

    @Override
    public Permission createPermissionInternal(String modId, String node, PermissionType type) {
        return new Permission() {
            @Override
            public boolean hasPermission(ServerPlayerEntity player) {
                return type.hasPermission(player);
            }

            @Override
            public PermissionType getPermissionType() {
                return type;
            }
        };
    }

    private static Boolean loaded;

    private static boolean isFabricPermissionsAPILoaded() {
        return false;
    }

}
