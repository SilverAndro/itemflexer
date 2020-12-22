package mc.itemflexer;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Iterator;

public class ItemFlexer implements ModInitializer {
    private final HashMap<ServerPlayerEntity, Integer> cooldowns = new HashMap<>();
    
    @Override
    public void onInitialize() {
        System.out.println("Flex your items!");
    
        AutoConfig.register(ItemFlexerConfig.class, JanksonConfigSerializer::new);
        
        ServerTickEvents.END_SERVER_TICK.register((world) -> {
            for (ServerPlayerEntity player : cooldowns.keySet()) {
                int current = cooldowns.get(player);
                cooldowns.put(player, current - 1);
            }
    
            for (ServerPlayerEntity player : cooldowns.keySet()) {
                int current = cooldowns.get(player);
                if (current <= 0) {
                    cooldowns.remove(player);
                }
            }
        });
        
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                dispatcher.register(
                    CommandManager.literal("flex")
                        .executes(context -> {
                            ServerPlayerEntity entity = context.getSource().getPlayer();
                            ItemStack stack = entity.getMainHandStack();
                            
                            return flexItem(stack, entity, context.getSource());
                        })
                        .then(CommandManager.argument("slot", IntegerArgumentType.integer(1, 9))
                            .executes(context -> {
                                int slotIndex = IntegerArgumentType.getInteger(context, "slot");
                                ServerPlayerEntity entity = context.getSource().getPlayer();
                                ItemStack stack = entity.inventory.getStack(slotIndex - 1);
                                
                                return flexItem(stack, entity, context.getSource());
                            })
                        )
                );
            }
        );
    }
    
    private int flexItem(ItemStack stack, ServerPlayerEntity player, ServerCommandSource source) {
        ItemFlexerConfig config = AutoConfig.getConfigHolder(ItemFlexerConfig.class).getConfig();
        
        if (cooldowns.containsKey(player)) {
            source.sendError(new LiteralText("On cooldown: " + cooldowns.get(player) / 20.0f + " seconds left.").formatted(Formatting.RED));
            return 0;
        }
        
        if (stack.getItem() != Items.AIR) {
            cooldowns.put(player, config.cooldown);
            for (Iterator<ServerPlayerEntity> it = PlayerStream.all(player.server).iterator(); it.hasNext(); ) {
                ServerPlayerEntity other = it.next();
                other.sendMessage(((LiteralText)player.getDisplayName()).append(new LiteralText(" is flexing their ").formatted(Formatting.WHITE)).append(stack.toHoverableText()), false);
            }
            return 1;
        }
        else {
            source.sendError(new LiteralText("Can't flex empty item").formatted(Formatting.RED));
            return 0;
        }
    }
}
