package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.Survivalmatic;
import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicData {
    public static final ClientSchematicData INSTANCE = new ClientSchematicData();

    private SchematicReader.StreamableSchematicInstance schematicInstance = null;
    private final HashMap<String, File> availableSchematics = new HashMap<>();
    private ClientSchematicData() {}
    public SchematicReader.StreamableSchematicInstance getSchematicInstance() {
        return schematicInstance;
    }
    public void setSchematicInstance(SchematicReader.StreamableSchematicInstance schematicInstance) {
        this.schematicInstance = schematicInstance;
    }
    public void reloadSchematics() {
        availableSchematics.clear();
        String[] extensions = {"schem"};
        Collection<File> files = FileUtils.listFiles(Survivalmatic.SCHEMATIC_PATH.toFile(), extensions, false);
        files.forEach(file -> {
            availableSchematics.put(file.getName(), file);
        });
    }
    public Collection<String> getAvailableSchematicNames() {
        return availableSchematics.keySet();
    }
    public Collection<File> getAvailableSchematics() {
        return availableSchematics.values();
    }
    public File getSchematicFile(String schematicName) {
        return availableSchematics.get(schematicName);
    }
}
