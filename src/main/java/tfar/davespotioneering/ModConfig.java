package tfar.davespotioneering;

import net.minecraftforge.common.ForgeConfigSpec;
import tfar.davespotioneering.client.GauntletHUD;

public class ModConfig {

    public static class Client {

        //couldn't this be a resource pack?
      //  public static ForgeConfigSpec.BooleanValue play_block_brewing_stand_brew;

        public static ForgeConfigSpec.IntValue gauntlet_hud_x;
        public static ForgeConfigSpec.IntValue gauntlet_hud_y;
        public static ForgeConfigSpec.EnumValue<GauntletHUD.HudPresets> gauntlet_hud_preset;
        public static ForgeConfigSpec.IntValue particle_drip_rate;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("general");
         //   play_block_brewing_stand_brew = builder.define("play_block_brewing_stand_brew",true);
            gauntlet_hud_x = builder.translation("config.gauntlet_hud_x").defineInRange("gauntlet_hud_x", -120, Integer.MIN_VALUE, Integer.MAX_VALUE);
            gauntlet_hud_y = builder.translation("config.gauntlet_hud_y").defineInRange("gauntlet_hud_y", -92, Integer.MIN_VALUE, Integer.MAX_VALUE);
            gauntlet_hud_preset = builder.translation("config.gauntlet_hud_preset").defineEnum("gauntlet_hud_preset", GauntletHUD.HudPresets.FREE_MOVE);
            particle_drip_rate = builder.defineInRange("particle_drip_rate",10,1,Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static class Server {

        public static ForgeConfigSpec.IntValue potion_switch_cooldown;
        public static ForgeConfigSpec.IntValue potion_throw_cooldown;
        public static ForgeConfigSpec.BooleanValue show_spiked_food;
        public static ForgeConfigSpec.BooleanValue milk;
        public static ForgeConfigSpec.BooleanValue coat_tools;
        public static ForgeConfigSpec.BooleanValue spike_food;
        public static ForgeConfigSpec.BooleanValue coat_anything;
        public static ForgeConfigSpec.IntValue gauntlet_cooldown;
        public static ForgeConfigSpec.IntValue potion_stack_size;
        public static ForgeConfigSpec.IntValue splash_potion_stack_size;
        public static ForgeConfigSpec.IntValue lingering_potion_stack_size;
        public static final String pot_throw_key = "config.davespotioneering.potion_throw_cooldown";
        public static final String pot_switch_key = "config.davespotioneering.potion_switch_cooldown";

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            milk = builder.define("milk",true);
            coat_tools = builder.define("coat_tools",true);
            spike_food = builder.define("spike_food",true);
            coat_anything = builder.define("coat_anything",false);
            show_spiked_food = builder.define("show_spiked_food",true);
            gauntlet_cooldown = builder.defineInRange("gauntlet_cooldown", 600, 1, Integer.MAX_VALUE);

            potion_switch_cooldown = builder.comment("Cooldown in ticks when switching to potions").translation(pot_switch_key)
                    .defineInRange("potion_switch_cooldown", 30, 0, 20000);
            potion_throw_cooldown = builder.comment("Cooldown in ticks when throwing potions").translation(pot_throw_key)
                    .defineInRange("potion_throw_cooldown", 30, 0, 20000);

            potion_stack_size = builder.defineInRange("potion_stack_size", 16, 1, 64);
            splash_potion_stack_size = builder.defineInRange("splash_potion_stack_size", 4, 1, 64);
            lingering_potion_stack_size = builder.defineInRange("lingering_potion_stack_size", 4, 1, 64);


            builder.pop();
        }
    }
}
