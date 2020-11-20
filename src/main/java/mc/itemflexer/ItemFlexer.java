package mc.itemflexer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.time.format.TextStyle;

public class ItemFlexer implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Flex your items!");
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("flex").executes(context -> {
                Entity entity = context.getSource().getEntityOrThrow();
                if (entity instanceof ServerPlayerEntity) {
                    ItemStack stack = ((ServerPlayerEntity)entity).getMainHandStack();
                    if (stack.getItem() != Items.AIR) {
                        for (Object player : PlayerStream.all(((ServerPlayerEntity)entity).server).toArray()) {
                            ((ServerPlayerEntity)player).sendMessage(((LiteralText)entity.getDisplayName()).append(new LiteralText(" is flexing their ").formatted(Formatting.WHITE)).append(stack.toHoverableText()), false);
                        }
                    }
                }
                return 1;
            }));
        });
    }
}
