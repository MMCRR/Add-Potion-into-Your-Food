package mods.Hileb.add_potion.api;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.*;
import java.util.stream.Collectors;

public class PotionList extends NonNullList<MobEffectInstance> {
    public static final Codec<PotionList> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(
                    MobEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(PotionList::getEffects),
                    PotionType.CODEC.fieldOf("type").forGetter(PotionList::getType)
            ).apply(instance, PotionList::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PotionList> STREAM_CODEC = StreamCodec.of(
            (o, potionMap) -> {
                PotionType.STREAM_CODEC.encode(o, potionMap.type);
                o.writeInt(potionMap.size()); // indexs
                for (var effect : potionMap) {
                    MobEffectInstance.STREAM_CODEC.encode(o, effect);
                }
            },
            friendlyByteBuf -> {
                PotionType type = PotionType.STREAM_CODEC.decode(friendlyByteBuf);
                int size = friendlyByteBuf.readInt();
                PotionList list = new PotionList(size, type);
                for (int j = 0; j < size; j++) {
                    list.add(MobEffectInstance.STREAM_CODEC.decode(friendlyByteBuf));
                }
                return list;
            }
    );

    public PotionType type;
    public static final MobEffectInstance NULL = new MobEffectInstance(MobEffects.JUMP, 0);

    public PotionList(){
        this(0);
    }

    public PotionList(int size, PotionType type){
        super(new ArrayList<>(size), NULL);
        this.type = type;
    }

    public PotionList(PotionType type) {
        this(0, type);
    }

    public PotionList(int size) {
        this(size, PotionType.DEFAULT);
    }

    public PotionList(List<MobEffectInstance> list, PotionType type) {
        super(toMutable(list), NULL);
        this.type = type;
    }

    public static <T> List<T> toMutable(List<T> list) {
        ArrayList<T> arrayList = new ArrayList<>(list.size());
        arrayList.addAll(list);
        return arrayList;
    }

    public List<MobEffectInstance> getEffects() {
        return this.stream().map(MobEffectInstance::new).collect(Collectors.toList());
    }

    @Override
    public boolean add(MobEffectInstance instance) {
        return super.add(new MobEffectInstance(instance));
    }

    public PotionType getType() {
        return type;
    }

    public PotionList copy() {
        var list = new PotionList(this.size(), this.type);
        list.addAll(this);
        return list;
    }

    public void setType(PotionType type) {
        this.type = type;
    }
}
