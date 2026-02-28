package com.semmelzahntiger.survivalmatic.client.data;

public class SchemBuilderInterfaceData {
    private int state = 0;

    static final SchemBuilderInterfaceData INSTANCE = new SchemBuilderInterfaceData();

    public static SchemBuilderInterfaceData getINSTANCE() {
        return INSTANCE;
    }
}
