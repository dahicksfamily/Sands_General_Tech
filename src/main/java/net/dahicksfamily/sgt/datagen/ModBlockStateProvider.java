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
        // General
        objBlockWithItem(ModBlocks.SPHERE);

        //  Ore

        // Functional

        // Non-Block Shapes

    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void objBlockWithItem(RegistryObject<Block> blockRegistryObject) {
        String name = blockRegistryObject.getId().getPath();

        // Build the OBJ model
        BlockModelBuilder model = models().getBuilder(name)
                .parent(models().getExistingFile(mcLoc("block/block"))) // parent must be ModelFile
                .customLoader(net.minecraftforge.client.model.generators.loaders.ObjModelBuilder::begin)
                .modelLocation(modLoc("models/" + name + ".obj")) // path to OBJ
                .flipV(true)
                .automaticCulling(true)
                .end()
                .texture(name + "_texture", modLoc("block/" + name)) // maps "sphere_texture" in JSON
                .texture("particle", modLoc("block/" + name));      // maps "particle" in JSON

        // Assign the model to block variant
        getVariantBuilder(blockRegistryObject.get())
                .partialState()
                .modelForState()
                .modelFile(model)
                .addModel();
    }
}
