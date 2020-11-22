package mc.itemflexer;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class ItemFlexer implements ModInitializer {
    @Override
    public void onInitialize() {
        System.out.println("Flex your items!");
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                dispatcher.register(
                    CommandManager.literal("flex")
                        .executes(context -> {
                            ServerPlayerEntity entity = context.getSource().getPlayer();
                            ItemStack stack = entity.getMainHandStack();
                            if (stack.getItem() != Items.AIR) {
                                for (Object player : PlayerStream.all(entity.server).toArray()) {
                                    ((ServerPlayerEntity)player).sendMessage(((LiteralText)entity.getDisplayName()).append(new LiteralText(" is flexing their ").formatted(Formatting.WHITE)).append(stack.toHoverableText()), false);
                                }
                            }
                            return 1;
                        })
                        .then(CommandManager.argument("slot", IntegerArgumentType.integer(1, 9))
                            .executes(context -> {
                                System.out.println(context);
                                int slotIndex = IntegerArgumentType.getInteger(context, "slot");
                                ServerPlayerEntity entity = context.getSource().getPlayer();
                                ItemStack stack = entity.inventory.getStack(slotIndex - 1);
                                System.out.println(stack);
                                System.out.println(entity);
                                if (stack.getItem() != Items.AIR) {
                                    for (Object player : PlayerStream.all(entity.server).toArray()) {
                                        ((ServerPlayerEntity)player).sendMessage(((LiteralText)entity.getDisplayName()).append(new LiteralText(" is flexing their ").formatted(Formatting.WHITE)).append(stack.toHoverableText()), false);
                                    }
                                }
                                return 1;
                            })
                        )
                );
            }
        );
    }
}
