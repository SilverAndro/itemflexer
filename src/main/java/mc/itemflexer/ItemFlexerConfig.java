package mc.itemflexer;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "itemflexer")
class ItemFlexerConfig implements ConfigData {
    @Comment("Delay before /flex can be used again (in ticks)")
    int cooldown = 0;
}
