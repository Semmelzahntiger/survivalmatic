package com.semmelzahntiger.survivalmatic.client.data;

import com.semmelzahntiger.survivalmatic.common.schem.SchematicReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientSchematicData {
    public static final ClientSchematicData INSTANCE = new ClientSchematicData();

    private SchematicReader.ClientInstance schematicInstance = null;
    private ClientSchematicData() {}
    public SchematicReader.ClientInstance getSchematicInstance() {
        return schematicInstance;
    }
    public void setSchematicInstance(SchematicReader.ClientInstance schematicInstance) {
        this.schematicInstance = schematicInstance;
    }

}
