package mc.itemflexer;

import mc.microconfig.Comment;
import mc.microconfig.ConfigData;

class ItemFlexerConfig implements ConfigData {
    @Comment("Cooldown before /flex can be used again, in ticks")
    public int cooldown = 0;
}
