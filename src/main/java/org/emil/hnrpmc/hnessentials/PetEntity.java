package org.emil.hnrpmc.hnessentials;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PetEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> SKIN_INDEX =
            SynchedEntityData.defineId(PetEntity.class, EntityDataSerializers.INT);

    protected PetEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_326335_) {
        this.entityData.set(SKIN_INDEX, 0);
    }

    public int getSkinIndex() {
        return this.entityData.get(SKIN_INDEX);
    }

    public void setSkinIndex(int skinIndex) {
        this.entityData.set(SKIN_INDEX, skinIndex);
    }
}