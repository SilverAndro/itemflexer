package dev.silverandro.itemflexer;

import mc.microconfig.Comment;
import mc.microconfig.ConfigData;

public class ItemFlexerConfig implements ConfigData {
    @Comment("Cooldown before /flex can be used again, in ticks")
    public int cooldown = 0;
    
    @Comment("The string that will be used in chat\nUse %player:displayname% for the player name, and %itemflexer:item% for the item")
    public String chatMessage = "%player:displayname% is flexing their %itemflexer:item%";

    @Comment("The string that will be used in chat when a player includes the item count\nUse %player:displayname% for the player name, and %itemflexer:item% for the item")
    public String chatMessageWithCount = "%player:displayname% is flexing their %itemflexer:count% %itemflexer:item%";

    @Comment("The string for trying to flex nothing")
    public String failureNoItem = "Can't flex empty item";
    
    @Comment("The message for trying to flex on cooldown\nUse %itemflexer:cooldown% for the cooldown")
    public String failureOnCooldown = "On Cooldown: %itemflexer:cooldown% seconds left";
    
    @Comment("The name of the command to flex items")
    public String commandName = "flex";
}
