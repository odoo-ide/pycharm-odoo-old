package dev.ngocta.pycharm.odoo.data;

public enum OdooRecordSubType {
    QWEB(1);

    private final int myId;

    OdooRecordSubType(int id) {
        myId = id;
    }

    public int getId() {
        return myId;
    }

    public static OdooRecordSubType getById(int id) {
        for (OdooRecordSubType type : OdooRecordSubType.values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        return null;
    }
}
