package ru.etc1337.client.commands.impl;

import net.minecraft.network.play.client.CPlayerPacket;
import ru.etc1337.client.commands.Command;
import ru.etc1337.client.commands.api.CommandInfo;
import ru.etc1337.client.commands.api.CommandParameter;

@CommandInfo(name = "VClip", description = "Позволяет телепортироваться вверх и вниз по Y координате", aliases = {"vclip", "yclip"})
public final class VClipCommand extends Command {

    @Override
    public void execute(String[] args) {
        if (args.length == 2) {
            try {
                int delay = Integer.parseInt(args[1]);
                execute(delay, false);
            } catch (NumberFormatException e) {
                error(".vclip requires a number");
            }
        } else if (args.length == 3) {
            try {
                int delay = Integer.parseInt(args[1]);
                execute(delay, true);
            } catch (NumberFormatException e) {
                error(".vclip requires a number and value");
            }
        }  else {
            error(".vclip y [.vclip 5 false / .vclip -5 true] (extra - false or true)");
        }

    }



    private void execute(float y, boolean extra) {
        if (extra) {
            for (int i = 0; i < 10; ++i)
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));

            for (int i = 0; i < 10; ++i)
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + y, mc.player.getPosZ(), false));
        }

        mc.player.setPosition(mc.player.getPosX(), mc.player.getPosY() + (double) y, mc.player.getPosZ());
    }
}