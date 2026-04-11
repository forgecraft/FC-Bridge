package net.forgecraft.mods.bridge.mixins.client.accessors;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(TeleportCommand.class)
public interface TeleportCommandMixin {
    @Invoker("performTeleport")
    static void fcbridge$performTeleport(CommandSourceStack source, Entity victim, ServerLevel level, double x, double y, double z, Set<Relative> relatives, float yRot, float xRot, @Nullable LookAt lookAt) throws CommandSyntaxException {
        throw new AssertionError();
    }
}
