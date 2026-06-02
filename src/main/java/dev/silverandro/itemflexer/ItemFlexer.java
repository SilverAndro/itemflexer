package dev.silverandro.itemflexer;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import mc.microconfig.MicroConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.HashMap;

public class ItemFlexer implements ModInitializer {
    ItemFlexerConfig config = MicroConfig.getOrCreate("itemflexer", new ItemFlexerConfig());

    private final HashMap<ServerPlayer, Integer> cooldowns = new HashMap<>();

    public static ItemStack stack;

    @Override
    public void onInitialize() {
        System.out.println("Flex your items!");

        Placeholders.registerServer(
                Identifier.fromNamespaceAndPath("itemflexer", "item"),
                (ctx, _s) -> PlaceholderResult.value(stack.getDisplayName()));

        Placeholders.registerServer(
                Identifier.fromNamespaceAndPath("itemflexer", "count"),
                (ctx, _s) -> PlaceholderResult.value(String.valueOf(stack.getCount())));

        Placeholders.registerServer(
                Identifier.fromNamespaceAndPath("itemflexer", "cooldown"),
                (ctx, _s) -> PlaceholderResult.value(Component.empty().append(cooldowns.get(ctx.player()) / 20f + "")));

        ServerTickEvents.END_SERVER_TICK.register((world) -> {
            // Reduce the cooldown count of all players on cooldown
            for (ServerPlayer player : cooldowns.keySet()) {
                int current = cooldowns.get(player);
                if (current >= 0) {
                    cooldowns.put(player, current - 1);
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, _env) -> dispatcher.register(
                Commands.literal(config.commandName)
                        .executes(context -> {
                            // Get the current held item and pass it to the logic
                            ServerPlayer entity = context.getSource().getPlayerOrException();
                            stack = entity.getMainHandItem();

                            return flexItem(stack, entity, false, context.getSource());
                        }).then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                .executes(context -> {
                                    // Get the item in that slot and pass it
                                    int slotIndex = IntegerArgumentType.getInteger(context, "slot");
                                    ServerPlayer entity = context.getSource().getPlayerOrException();
                                    stack = entity.getInventory().getItem(slotIndex - 1);

                                    return flexItem(stack, entity, false, context.getSource());
                                }))
                        .then(Commands.literal("showCount").executes(context -> {
                            ServerPlayer entity = context.getSource().getPlayerOrException();
                            stack = entity.getMainHandItem();
                            return this.flexItem(stack, entity, true, context.getSource());
                        }).then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                .executes(context -> {
                                    // Get the item in that slot and pass it
                                    int slotIndex = IntegerArgumentType.getInteger(context, "slot");
                                    ServerPlayer entity = context.getSource().getPlayerOrException();
                                    stack = entity.getInventory().getItem(slotIndex - 1);

                                    return flexItem(stack, entity, true, context.getSource());
                                })))));
    }

    private int flexItem(ItemStack stack, ServerPlayer player, boolean showCount, CommandSourceStack source) {
        // Make sure they can't flex items if on cooldown and send feedback
        if (cooldowns.containsKey(player) && cooldowns.get(player) > 0) {
            source.sendFailure(
                    NodeParser.builder().serverPlaceholders().build().parseComponent(config.failureOnCooldown,
                            PlaceholderContext.of(player).asParserContext()));
            return 0;
        }

        // Make sure they're actually holding something
        if (stack.getItem() != Items.AIR) {
            // Put them on cooldown
            cooldowns.put(player, config.cooldown);

            // Construct and broadcast the message to all users
            String text;
            if (showCount) {
                text = this.config.chatMessageWithCount;
            } else {
                text = this.config.chatMessage;
            }

            Component message = NodeParser.builder().serverPlaceholders().build().parseComponent(text,
                    PlaceholderContext.of(player).asParserContext());
            source.getServer().getPlayerList().broadcastSystemMessage(message, false);
            return 1;
        } else {
            source.sendFailure(Component.empty().append(config.failureNoItem));
            return 0;
        }
    }
}
