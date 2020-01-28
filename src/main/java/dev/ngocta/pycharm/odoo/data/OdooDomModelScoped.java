package dev.ngocta.pycharm.odoo.data;

public interface OdooDomModelScoped {
    default String getModel() {
        return null;
    }
}
