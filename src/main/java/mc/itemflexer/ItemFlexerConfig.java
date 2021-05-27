package mc.itemflexer;

import mc.microconfig.Comment;
import mc.microconfig.ConfigData;

public class ItemFlexerConfig implements ConfigData {
    @Comment("Cooldown before /flex can be used again, in ticks")
    public int cooldown = 0;
    
    @Comment("The string that will be used in chat\nUse %player:displayname% for the player name, and %itemflexer:item% for the item")
    public String chatMessage = "%player:displayname% is flexing their %itemflexer:item%";
}
