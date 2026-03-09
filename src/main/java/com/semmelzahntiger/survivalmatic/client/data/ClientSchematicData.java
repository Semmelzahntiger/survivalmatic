package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicData {
    public static final ClientSchematicData INSTANCE = new ClientSchematicData();

    private SchematicReader.StreamableSchematicInstance schematicInstance = null;
    private ClientSchematicData() {}
    public SchematicReader.StreamableSchematicInstance getSchematicInstance() {
        return schematicInstance;
    }
    public void setSchematicInstance(SchematicReader.StreamableSchematicInstance schematicInstance) {
        this.schematicInstance = schematicInstance;
    }

}
