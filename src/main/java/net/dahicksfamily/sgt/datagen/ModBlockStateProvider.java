package net.dahicksfamily.sgt.datagen;

import net.dahicksfamily.sgt.SGT;
import net.dahicksfamily.sgt.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, SGT.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
 
        objBlockWithItem(ModBlocks.SPHERE);

 

 

 

    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void objBlockWithItem(RegistryObject<Block> blockRegistryObject) {
        String name = blockRegistryObject.getId().getPath();

 
        BlockModelBuilder model = models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block"))) 
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(modLoc("models/" + name + ".obj")) 
                .flipV(true)
                .automaticCulling(true)
                .end()
                .texture(name + "_texture", modLoc("block/" + name)) 
                .texture("particle", modLoc("block/" + name)); 

 
        getVariantBuilder(blockRegistryObject.get())
                .partialState()
                .modelForState()
                .modelFile(model)
                .addModel();
    }
}
