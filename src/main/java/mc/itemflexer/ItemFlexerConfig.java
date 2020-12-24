package mc.itemflexer;

import mc.microconfig.Comment;
import mc.microconfig.ConfigData;

class ItemFlexerConfig implements ConfigData {
    @Comment("Cooldown before /flex can be used again, in ticks")
    int cooldown = 0;
}
