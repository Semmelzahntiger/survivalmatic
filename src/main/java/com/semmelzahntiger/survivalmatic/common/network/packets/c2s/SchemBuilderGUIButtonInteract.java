package com.semmelzahntiger.survivalmatic.common.network.packets.c2s;

public class SchemBuilderGUIButtonInteract {
    public enum INTERACTIONS {
        SELECT_SCHEMATIC(0),
        SCAN_READ(1),
        SCAN_PREPARATION(2),
        BUILD(3),
        CANCEL(4),
        CLEAR_DUMP_BUFFER(5);
        private final int id;
        INTERACTIONS(int id) {
            this.id = id;
        }
    }
    private final int interactionID;
    public SchemBuilderGUIButtonInteract(int interactionID) {
        this.interactionID = interactionID;
    }
    public int getInteractionID() {
        return interactionID;
    }
}
