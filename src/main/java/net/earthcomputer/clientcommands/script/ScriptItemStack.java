package net.earthcomputer.clientcommands.script;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ScriptItemStack {
    private final ItemStack stack;

    ScriptItemStack(ItemStack stack) {
        this.stack = stack;
    }

    static ScriptItemStack ofUnchecked(Value obj) {
        NbtElement itemNbt = ScriptUtil.toNbt(obj);
        if (!(itemNbt instanceof NbtCompound)) {
            Identifier itemId = new Identifier(ScriptUtil.asString(obj));
            if (!Registry.ITEM.containsId(itemId))
                throw new IllegalArgumentException("Cannot convert " + obj + " to item");
            return new ScriptItemStack(new ItemStack(Registry.ITEM.get(itemId)));
        }
        ItemStack stack = ItemStack.fromNbt((NbtCompound) itemNbt);
        return new ScriptItemStack(stack);
    }

    public static Object of(Value obj) {
        return BeanWrapper.wrap(ofUnchecked(obj));
    }

    public Object getStack() {
        return ScriptUtil.fromNbtCompound(stack.writeNbt(new NbtCompound()));
    }

    public float getMiningSpeed(String block) {
        return getMiningSpeed(ScriptBlockState.uncheckedDefaultState(block));
    }

    public float getMiningSpeed(ScriptBlockState block) {
        return stack.getMiningSpeedMultiplier(block.state);
    }

    public boolean isEffectiveOn(String block) {
        return isEffectiveOn(ScriptBlockState.uncheckedDefaultState(block));
    }

    public boolean isEffectiveOn(ScriptBlockState block) {
        return stack.isSuitableFor(block.state);
    }

    public int getMaxCount() {
        return stack.getMaxCount();
    }

    public int getMaxDamage() {
        return stack.getMaxDamage();
    }

    public boolean isIsFood() {
        return stack.isFood();
    }

    public int getHungerRestored() {
        FoodComponent food = stack.getItem().getFoodComponent();
        return food == null ? 0 : food.getHunger();
    }

    public float getSaturationRestored() {
        FoodComponent food = stack.getItem().getFoodComponent();
        return food == null ? 0 : food.getSaturationModifier();
    }

    public boolean isIsMeat() {
        FoodComponent food = stack.getItem().getFoodComponent();
        return food != null && food.isMeat();
    }

    public boolean isAlwaysEdible() {
        FoodComponent food = stack.getItem().getFoodComponent();
        return food != null && food.isAlwaysEdible();
    }

    public boolean isIsSnack() {
        FoodComponent food = stack.getItem().getFoodComponent();
        return food != null && food.isSnack();
    }

    public List<String> getTags() {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            return new ArrayList<>();
        }
        //noinspection StaticPseudoFunctionalStyleMethod
        return Lists.transform(
                new ArrayList<>(ItemTags.getTagGroup().getTagsFor(stack.getItem())),
                ScriptUtil::simplifyIdentifier);
    }

}
