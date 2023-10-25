package dev.silverandro.itemflexer;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import mc.microconfig.MicroConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class ItemFlexer implements ModInitializer {
    ItemFlexerConfig config = MicroConfig.getOrCreate("itemflexer", new ItemFlexerConfig());

    private final HashMap<ServerPlayerEntity, Integer> cooldowns = new HashMap<>();

    public static ItemStack stack;

    @Override
    public void onInitialize() {
        System.out.println("Flex your items!");

        Placeholders.register(
            new Identifier("itemflexer", "item"),
            (ctx, _s) -> PlaceholderResult.value(stack.toHoverableText())
        );

        Placeholders.register(
                new Identifier("itemflexer", "count"),
                (ctx, _s) -> PlaceholderResult.value(String.valueOf(stack.getCount()))
        );

        Placeholders.register(
            new Identifier("itemflexer", "cooldown"),
            (ctx, _s) -> PlaceholderResult.value(Text.empty().append(cooldowns.get(ctx.player()) / 20f + ""))
        );

        ServerTickEvents.END_SERVER_TICK.register((world) -> {
            // Reduce the cooldown count of all players on cooldown
            for (ServerPlayerEntity player : cooldowns.keySet()) {
                int current = cooldowns.get(player);
                if (current >= 0) {
                    cooldowns.put(player, current - 1);
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, _env) -> dispatcher.register(
            CommandManager.literal(config.commandName)
                .executes(context -> {
                    // Get the current held item and pass it to the logic
                    ServerPlayerEntity entity = context.getSource().getPlayerOrThrow();
                    stack = entity.getMainHandStack();

                    return flexItem(stack, entity, false, context.getSource());
                }).then(CommandManager.argument("slot", IntegerArgumentType.integer(1, 9))
                    .executes(context -> {
                        // Get the item in that slot and pass it
                        int slotIndex = IntegerArgumentType.getInteger(context, "slot");
                        ServerPlayerEntity entity = context.getSource().getPlayerOrThrow();
                        stack = entity.getInventory().getStack(slotIndex - 1);

                        return flexItem(stack, entity, false, context.getSource());
                    }))
                .then(CommandManager.literal("showCount").executes(context -> {
                    ServerPlayerEntity entity = context.getSource().getPlayerOrThrow();
                    stack = entity.getMainHandStack();
                    return this.flexItem(stack, entity, true, context.getSource());
                }).then(CommandManager.argument("slot", IntegerArgumentType.integer(1, 9))
                    .executes(context -> {
                        // Get the item in that slot and pass it
                        int slotIndex = IntegerArgumentType.getInteger(context, "slot");
                        ServerPlayerEntity entity = context.getSource().getPlayerOrThrow();
                        stack = entity.getInventory().getStack(slotIndex - 1);

                        return flexItem(stack, entity, true, context.getSource());
                    })))
            )
        );
    }

    private int flexItem(ItemStack stack, ServerPlayerEntity player, boolean showCount, ServerCommandSource source) {
        // Make sure they can't flex items if on cooldown and send feedback
        if (cooldowns.containsKey(player) && cooldowns.get(player) > 0) {
            source.sendError(Placeholders.parseText(Text.empty().append(config.failureOnCooldown), PlaceholderContext.of(player)));
            return 0;
        }

        // Make sure their actually holding something
        if (stack.getItem() != Items.AIR) {
            // Put them on cooldown
            cooldowns.put(player, config.cooldown);

            // Construct and broadcast the message to all users
            Text text;
            if (showCount) {
                text = Text.of(this.config.chatMessageWithCount);
            } else {
                text = Text.of(this.config.chatMessage);
            }

            Text message = Placeholders.parseText(text, PlaceholderContext.of(player));
            for (ServerPlayerEntity other : player.server.getPlayerManager().getPlayerList()) {
                other.sendMessage(message, false);
            }
            return 1;
        } else {
            source.sendError(Text.empty().append(config.failureNoItem));
            return 0;
        }
    }
}
