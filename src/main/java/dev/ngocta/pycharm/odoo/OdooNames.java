package dev.ngocta.pycharm.odoo;

public class OdooNames {
    public static final String MANIFEST_FILE_NAME = "__manifest__.py";
    public static final String MANIFEST_DEPENDS = "depends";
    public static final String MANIFEST_DATA = "data";
    public static final String MANIFEST_DEMO = "demo";
    public static final String MANIFEST_QWEB = "qweb";
    public static final String ODOO = "odoo";
    public static final String ADDONS = "addons";
    public static final String ODOO_ADDONS = "odoo.addons";
    public static final String MODEL_NAME = "_name";
    public static final String MODEL_INHERIT = "_inherit";
    public static final String MODEL_INHERITS = "_inherits";
    public static final String FIELD_TYPE_ID = "Id";
    public static final String FIELD_TYPE_BOOLEAN = "Boolean";
    public static final String FIELD_TYPE_INTEGER = "Integer";
    public static final String FIELD_TYPE_FLOAT = "Float";
    public static final String FIELD_TYPE_MONETARY = "Monetary";
    public static final String FIELD_TYPE_CHAR = "Char";
    public static final String FIELD_TYPE_TEXT = "Text";
    public static final String FIELD_TYPE_HTML = "Html";
    public static final String FIELD_TYPE_DATE = "Date";
    public static final String FIELD_TYPE_DATETIME = "Datetime";
    public static final String FIELD_TYPE_BINARY = "Binary";
    public static final String FIELD_TYPE_SELECTION = "Selection";
    public static final String FIELD_TYPE_MANY2ONE = "Many2one";
    public static final String FIELD_TYPE_ONE2MANY = "One2many";
    public static final String FIELD_TYPE_MANY2MANY = "Many2many";
    public static final String FIELD_TYPE_REFERENCE = "Reference";
    public static final String[] FIELD_TYPES = new String[]{
            FIELD_TYPE_ID,
            FIELD_TYPE_MANY2ONE,
            FIELD_TYPE_ONE2MANY,
            FIELD_TYPE_MANY2MANY,
            FIELD_TYPE_REFERENCE,
            FIELD_TYPE_INTEGER,
            FIELD_TYPE_FLOAT,
            FIELD_TYPE_BOOLEAN,
            FIELD_TYPE_INTEGER,
            FIELD_TYPE_FLOAT,
            FIELD_TYPE_MONETARY,
            FIELD_TYPE_CHAR,
            FIELD_TYPE_TEXT,
            FIELD_TYPE_HTML,
            FIELD_TYPE_SELECTION,
            FIELD_TYPE_DATE,
            FIELD_TYPE_DATETIME,
            FIELD_TYPE_BINARY
    };
    public static final String FIELD_ATTR_COMODEL_NAME = "comodel_name";
    public static final String FIELD_ATTR_INVERSE_NAME = "inverse_name";
    public static final String FIELD_ATTR_RELATED = "related";
    public static final String FIELD_ATTR_COMPUTE = "compute";
    public static final String FIELD_ATTR_INVERSE = "inverse";
    public static final String FIELD_ATTR_SEARCH = "search";
    public static final String FIELD_ATTR_DEFAULT = "default";
    public static final String FIELD_ATTR_DELEGATE = "delegate";
    public static final String MAPPED = "mapped";
    public static final String FILTERED = "filtered";
    public static final String BASE_MODEL_QNAME = "odoo.models.BaseModel";
    public static final String FIELD_QNAME = "odoo.fields.Field";
    public static final String ENVIRONMENT_QNAME = "odoo.api.Environment";
    public static final String REF_QNAME = "odoo.api.Environment.ref";
    public static final String DECORATOR_DEPENDS = "api.depends";
    public static final String DECORATOR_CONSTRAINS = "api.constrains";
    public static final String DECORATOR_ONCHANGE = "api.onchange";
    public static final String REQUEST_RENDER_QNAME = "odoo.http.HttpRequest.render";
    public static final String IR_UI_VIEW = "ir.ui.view";
    public static final String IR_ACTIONS_REPORT = "ir.actions.report";
    public static final String IR_UI_MENU = "ir.ui.menu";
    public static final String IR_ACTIONS_ACT_WINDOW = "ir.actions.act_window";
    public static final String IR_ACTIONS_ACT_URL = "ir.actions.act_url";
    public static final String IR_ACTIONS_SERVER = "ir.actions.server";
    public static final String IR_ACTIONS_CLIENT = "ir.actions.client";
    public static final String[] ACTION_MODELS = new String[]{
            IR_ACTIONS_ACT_WINDOW,
            IR_ACTIONS_ACT_URL,
            IR_ACTIONS_SERVER,
            IR_ACTIONS_CLIENT,
            IR_ACTIONS_REPORT,
    };
    public static final String IR_MODEL = "ir.model";
    public static final String RES_GROUPS = "res.groups";
}
