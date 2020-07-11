package dev.ngocta.pycharm.odoo;

public class OdooNames {
    public static final String MANIFEST_FILE_NAME = "__manifest__.py";
    public static final String MANIFEST_DEPENDS = "depends";
    public static final String MANIFEST_DATA = "data";
    public static final String MANIFEST_DEMO = "demo";
    public static final String MANIFEST_QWEB = "qweb";
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
    public static final String[] RELATIONAL_FIELD_TYPES = new String[]{
            FIELD_TYPE_MANY2ONE,
            FIELD_TYPE_ONE2MANY,
            FIELD_TYPE_MANY2MANY
    };
    public static final String FIELD_ATTR_COMODEL_NAME = "comodel_name";
    public static final String FIELD_ATTR_INVERSE_NAME = "inverse_name";
    public static final String FIELD_ATTR_RELATED = "related";
    public static final String FIELD_ATTR_COMPUTE = "compute";
    public static final String FIELD_ATTR_INVERSE = "inverse";
    public static final String FIELD_ATTR_SEARCH = "search";
    public static final String FIELD_ATTR_DEFAULT = "default";
    public static final String FIELD_ATTR_DELEGATE = "delegate";
    public static final String FIELD_ATTR_CURRENCY_FIELD = "currency_field";
    public static final String FIELD_ATTR_DOMAIN = "domain";
    public static final String FIELD_ATTR_GROUPS = "groups";
    public static final String MAPPED = "mapped";
    public static final String FILTERED = "filtered";
    public static final String SORTED = "sorted";
    public static final String SEARCH = "search";
    public static final String SEARCH_READ = "search_read";
    public static final String SEARCH_COUNT = "search_count";
    public static final String CREATE = "create";
    public static final String WRITE = "write";
    public static final String UPDATE = "update";
    public static final String BASE_MODEL_CLASS_QNAME = "odoo.models.BaseModel";
    public static final String FIELD_CLASS_QNAME = "odoo.fields.Field";
    public static final String ENVIRONMENT_CLASS_QNAME = "odoo.api.Environment";
    public static final String ENV_REF_FUNC_QNAME = "odoo.api.Environment.ref";
    public static final String API_DEPENDS = "api.depends";
    public static final String API_CONSTRAINS = "api.constrains";
    public static final String API_ONCHANGE = "api.onchange";
    public static final String REQUEST_RENDER_FUNC_QNAME = "odoo.http.HttpRequest.render";
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
    public static final String BASE_MODEL = "base";
    public static final String IR_RULE = "ir.rule";
    public static final String IR_RULE_FIELD_DOMAIN_FORCE = "domain_force";
    public static final String IR_RULE_FIELD_GLOBAL = "global";
    public static final String IR_RULE_FIELD_MODEL_ID = "model_id";
    public static final String IR_ACTIONS_ACT_WINDOW_FIELD_DOMAIN = "domain";
    public static final String IR_ACTIONS_ACT_WINDOW_FIELD_RES_MODEL = "res_model";
    public static final String IR_CRON = "ir.cron";
    public static final String MAIL_TEMPLATE = "mail.template";
    public static final String DB_CURSOR_CLASS_QNAME = "odoo.sql_db.Cursor";
    public static final String REGISTRY_CLASS_QNAME = "odoo.modules.registry.Registry";
    public static final String HTTP_ROUTE_FUNC_QNAME = "odoo.http.route";
}
