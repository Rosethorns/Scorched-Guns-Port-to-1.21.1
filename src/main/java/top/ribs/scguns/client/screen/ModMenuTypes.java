package top.ribs.scguns.client.screen;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.Holder;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.screen.widget.ThermolithMenu;


public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(NeoForgeRegistries.MENU_TYPES, Reference.MOD_ID);
    public static final Holder<MenuType<AmmoBoxMenu>> AMMO_BOX =
            registerMenuType("ammo_box", AmmoBoxMenu::new);
    public static final Holder<MenuType<ShellCatcherModuleMenu>> SHELL_CATCHER_MODULE =
            registerMenuType("shell_catcher_module", ShellCatcherModuleMenu::new);
    public static final Holder<MenuType<AmmoModuleMenu>> AMMO_MODULE =
            registerMenuType("ammo_module", AmmoModuleMenu::new);
    public static final Holder<MenuType<CryoniterMenu>> CRYONITER_MENU =
            MENUS.register("cryoniter_menu", () -> IMenuTypeExtension.create(CryoniterMenu::new));
    public static final Holder<MenuType<VentCollectorMenu>> VENT_COLLECTOR_MENU =
            MENUS.register("vent_collector_menu", () -> IMenuTypeExtension.create(VentCollectorMenu::new));
    public static final Holder<MenuType<ThermolithMenu>> THERMOLITH_MENU =
            MENUS.register("thermolith_menu", () -> IMenuTypeExtension.create(ThermolithMenu::new));
    public static final Holder<MenuType<PolarGeneratorMenu>> POLAR_GENERATOR_MENU =
            MENUS.register("polar_generator_menu", () -> IMenuTypeExtension.create(PolarGeneratorMenu::new));
    public static final Holder<MenuType<MaceratorMenu>> MACERATOR_MENU =
            MENUS.register("macerator_menu", () -> IMenuTypeExtension.create(MaceratorMenu::new));
    public static final Holder<MenuType<PoweredMaceratorMenu>> POWERED_MACERATOR_MENU =
            MENUS.register("powered_macerator_menu", () -> IMenuTypeExtension.create(PoweredMaceratorMenu::new));
public static final Holder<MenuType<BasicTurretMenu>> BASIC_TURRET_MENU =
            MENUS.register("basic_turret_menu", () -> IMenuTypeExtension.create(BasicTurretMenu::new));
    public static final Holder<MenuType<ShotgunTurretMenu>> SHOTGUN_TURRET_MENU =
            MENUS.register("shotgun_turret_menu", () -> IMenuTypeExtension.create(ShotgunTurretMenu::new));
    public static final Holder<MenuType<AutoTurretMenu>> AUTO_TURRET_MENU =
            MENUS.register("auto_turret_menu", () -> IMenuTypeExtension.create(AutoTurretMenu::new));
    public static final Holder<MenuType<LightningBatteryMenu>> LIGHTING_BATTERY_MENU =
            MENUS.register("lightning_battery_menu", () -> IMenuTypeExtension.create(LightningBatteryMenu::new));
    public static final Holder<MenuType<MechanicalPressMenu>> MECHANICAL_PRESS_MENU =
            MENUS.register("mechanical_press_menu", () -> IMenuTypeExtension.create(MechanicalPressMenu::new));
    public static final Holder<MenuType<PoweredMechanicalPressMenu>> POWERED_MECHANICAL_PRESS_MENU =
            MENUS.register("powered_mechanical_press_menu", () -> IMenuTypeExtension.create(PoweredMechanicalPressMenu::new));

    public static final Holder<MenuType<GunBenchMenu>> GUN_BENCH
            = registerMenuType("gun_bench", GunBenchMenu::new);

    public static final Holder<MenuType<ChestMenu>> SUPPLY_SCAMP_MENU =
            registerMenuType("supply_scamp_menu", (id, playerInventory, buffer) ->
                    new ChestMenu(MenuType.GENERIC_9x3, id, playerInventory, new SimpleContainer(27), 3));

    private static <T extends AbstractContainerMenu>Holder<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
