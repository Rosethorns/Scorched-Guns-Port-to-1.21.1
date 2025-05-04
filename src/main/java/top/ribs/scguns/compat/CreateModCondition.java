package top.ribs.scguns.compat;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.IConditionSerializer;
import net.neoforged.fml.ModList;

public class CreateModCondition implements ICondition {
    private static final ResourceLocation NAME = new ResourceLocation("scguns", "create_mod_loaded");

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext iContext) {
        return ModList.get().isLoaded("create");
    }

    public static class Serializer implements IConditionSerializer<CreateModCondition> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, CreateModCondition value) {
            // No extra data to write
        }

        @Override
        public CreateModCondition read(JsonObject json) {
            return new CreateModCondition();
        }

        @Override
        public ResourceLocation getID() {
            return CreateModCondition.NAME;
        }
    }
}