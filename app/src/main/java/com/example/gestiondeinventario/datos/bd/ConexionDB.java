package com.example.gestiondeinventario.datos.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gestiondeinventario.modelo.Actividad;

import java.util.Locale;

public class ConexionDB extends SQLiteOpenHelper {

    public static final String NOMBRE_BD = "inventario.db";
    public static final int VERSION_BD = 9; // v9: catálogo de proveedores y proveedor por producto

    // Tabla PRODUCTO
    public static final String TABLA_PRODUCTO = "producto";
    public static final String COL_P_ID = "id";
    public static final String COL_P_NOMBRE = "nombre";
    public static final String COL_P_CODIGO = "codigo";
    public static final String COL_P_CATEGORIA = "categoria";
    public static final String COL_P_STOCK = "stock";
    public static final String COL_P_STOCK_MINIMO = "stock_minimo";
    public static final String COL_P_UBICACION = "ubicacion";
    public static final String COL_P_PRECIO = "precio";
    public static final String COL_P_CODIGO_QR = "codigo_qr"; // NUEVO: para almacenar datos del QR
    public static final String COL_P_PROVEEDOR_ID = "proveedor_id";

    // Tabla PROVEEDOR (marcas o mayoristas que abastecen al almacén)
    public static final String TABLA_PROVEEDOR = "proveedor";
    public static final String COL_PR_ID = "id";
    public static final String COL_PR_NOMBRE = "nombre";
    public static final String COL_PR_TELEFONO = "telefono";
    public static final String COL_PR_CONTACTO = "contacto";
    public static final String COL_PR_ACTIVO = "activo";

    // Tabla USUARIO
    public static final String TABLA_USUARIO = "usuario";
    public static final String COL_U_ID = "id";
    public static final String COL_U_USERNAME = "username";
    public static final String COL_U_PASSWORD = "password";
    public static final String COL_U_NOMBRE = "nombre";
    public static final String COL_U_ROL = "rol";
    public static final String COL_U_TURNO = "turno";

    // Tabla ACTIVIDAD (historial de lo que hace cada usuario)
    public static final String TABLA_ACTIVIDAD = "actividad";
    public static final String COL_A_ID = "id";
    public static final String COL_A_USUARIO = "usuario";
    public static final String COL_A_TIPO = "tipo";
    public static final String COL_A_DESCRIPCION = "descripcion";
    public static final String COL_A_FECHA = "fecha";
    public static final String COL_A_TURNO = "turno";

    // Tabla EMPRESA (empresas a las que el almacén abastece)
    public static final String TABLA_EMPRESA = "empresa";
    public static final String COL_E_ID = "id";
    public static final String COL_E_NOMBRE = "nombre";
    public static final String COL_E_RUC = "ruc";
    public static final String COL_E_TELEFONO = "telefono";
    public static final String COL_E_CONTACTO = "contacto";
    public static final String COL_E_ACTIVO = "activo";

    // Tabla ENVIO (salidas de productos del almacén)
    public static final String TABLA_ENVIO = "envio";
    public static final String COL_ENV_ID = "id";
    public static final String COL_ENV_NUMERO = "numero";
    public static final String COL_ENV_EMPRESA_ID = "empresa_id";
    public static final String COL_ENV_FECHA = "fecha";
    public static final String COL_ENV_ESTADO = "estado";
    public static final String COL_ENV_OBSERVACIONES = "observaciones";
    public static final String COL_ENV_USUARIO = "usuario";

    // Tabla ENVIO_DETALLE
    public static final String TABLA_ENVIO_DETALLE = "envio_detalle";
    public static final String COL_ED_ID = "id";
    public static final String COL_ED_ENVIO_ID = "envio_id";
    public static final String COL_ED_PRODUCTO_ID = "producto_id";
    public static final String COL_ED_CANTIDAD = "cantidad";

    private static final String CREATE_TABLE_PRODUCTO =
            "CREATE TABLE " + TABLA_PRODUCTO + " (" +
                    COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_P_NOMBRE + " TEXT NOT NULL, " +
                    COL_P_CODIGO + " TEXT NOT NULL UNIQUE, " +
                    COL_P_CATEGORIA + " TEXT NOT NULL, " +
                    COL_P_STOCK + " INTEGER NOT NULL DEFAULT 0, " +
                    COL_P_STOCK_MINIMO + " INTEGER NOT NULL DEFAULT 0, " +
                    COL_P_UBICACION + " TEXT, " +
                    COL_P_PRECIO + " REAL NOT NULL DEFAULT 0.0, " +
                    COL_P_CODIGO_QR + " TEXT, " +
                    COL_P_PROVEEDOR_ID + " INTEGER" +
                    ")";

    private static final String CREATE_TABLE_USUARIO =
            "CREATE TABLE " + TABLA_USUARIO + " (" +
                    COL_U_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_U_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COL_U_PASSWORD + " TEXT NOT NULL, " +
                    COL_U_NOMBRE + " TEXT, " +
                    COL_U_ROL + " TEXT DEFAULT 'empleado', " +
                    COL_U_TURNO + " TEXT DEFAULT 'mañana'" +
                    ")";

    private static final String CREATE_TABLE_ACTIVIDAD =
            "CREATE TABLE IF NOT EXISTS " + TABLA_ACTIVIDAD + " (" +
                    COL_A_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_A_USUARIO + " TEXT NOT NULL, " +
                    COL_A_TIPO + " TEXT NOT NULL, " +
                    COL_A_DESCRIPCION + " TEXT NOT NULL, " +
                    COL_A_FECHA + " INTEGER NOT NULL, " +
                    COL_A_TURNO + " TEXT" +
                    ")";

    private static final String CREATE_TABLE_PROVEEDOR =
            "CREATE TABLE IF NOT EXISTS " + TABLA_PROVEEDOR + " (" +
                    COL_PR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PR_NOMBRE + " TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
                    COL_PR_TELEFONO + " TEXT, " +
                    COL_PR_CONTACTO + " TEXT, " +
                    COL_PR_ACTIVO + " INTEGER NOT NULL DEFAULT 1" +
                    ")";

    private static final String CREATE_TABLE_EMPRESA =
            "CREATE TABLE IF NOT EXISTS " + TABLA_EMPRESA + " (" +
                    COL_E_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_E_NOMBRE + " TEXT NOT NULL UNIQUE, " +
                    COL_E_RUC + " TEXT, " +
                    COL_E_TELEFONO + " TEXT, " +
                    COL_E_CONTACTO + " TEXT, " +
                    COL_E_ACTIVO + " INTEGER NOT NULL DEFAULT 1" +
                    ")";

    private static final String CREATE_TABLE_ENVIO =
            "CREATE TABLE IF NOT EXISTS " + TABLA_ENVIO + " (" +
                    COL_ENV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ENV_NUMERO + " TEXT NOT NULL UNIQUE, " +
                    COL_ENV_EMPRESA_ID + " INTEGER NOT NULL, " +
                    COL_ENV_FECHA + " INTEGER NOT NULL, " +
                    COL_ENV_ESTADO + " TEXT NOT NULL DEFAULT 'REGISTRADO', " +
                    COL_ENV_OBSERVACIONES + " TEXT, " +
                    COL_ENV_USUARIO + " TEXT" +
                    ")";

    private static final String CREATE_TABLE_ENVIO_DETALLE =
            "CREATE TABLE IF NOT EXISTS " + TABLA_ENVIO_DETALLE + " (" +
                    COL_ED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ED_ENVIO_ID + " INTEGER NOT NULL, " +
                    COL_ED_PRODUCTO_ID + " INTEGER NOT NULL, " +
                    COL_ED_CANTIDAD + " INTEGER NOT NULL DEFAULT 0" +
                    ")";

    public ConexionDB(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PRODUCTO);
        db.execSQL(CREATE_TABLE_USUARIO);
        db.execSQL(CREATE_TABLE_ACTIVIDAD);
        db.execSQL(CREATE_TABLE_PROVEEDOR);
        db.execSQL(CREATE_TABLE_EMPRESA);
        db.execSQL(CREATE_TABLE_ENVIO);
        db.execSQL(CREATE_TABLE_ENVIO_DETALLE);
        insertarDatosIniciales(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLA_PRODUCTO);
            db.execSQL("DROP TABLE IF EXISTS " + TABLA_USUARIO);
            onCreate(db);
            return;
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE " + TABLA_PRODUCTO + " ADD COLUMN " + COL_P_CODIGO_QR + " TEXT");
            } catch (Exception ignored) {
                // Si la columna ya existe o falla la alteración, continuar de forma segura
            }
        }
        if (oldVersion < 5) {
            // Turnos: se agregan columnas sin borrar datos (antes de insertar usuarios
            // para que el INSERT pueda usar la columna turno)
            try {
                db.execSQL("ALTER TABLE " + TABLA_USUARIO + " ADD COLUMN " + COL_U_TURNO + " TEXT DEFAULT 'mañana'");
            } catch (Exception ignored) {
            }
            try {
                db.execSQL("ALTER TABLE " + TABLA_ACTIVIDAD + " ADD COLUMN " + COL_A_TURNO + " TEXT");
            } catch (Exception ignored) {
            }
        }
        if (oldVersion < 4) {
            // Migración NO destructiva: crea la tabla y agrega usuarios sin borrar datos
            db.execSQL(CREATE_TABLE_ACTIVIDAD);
            insertarUsuariosIniciales(db);
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("UPDATE " + TABLA_USUARIO + " SET " + COL_U_TURNO + " = 'noche' WHERE " + COL_U_USERNAME + " = 'antony'");
            } catch (Exception ignored) {
            }
        }
        if (oldVersion < 6) {
            // Catálogo tecnológico: quita los productos demo antiguos (solo esos códigos)
            // y agrega los nuevos sin tocar los productos que haya creado el usuario.
            try {
                db.execSQL("DELETE FROM " + TABLA_PRODUCTO + " WHERE " + COL_P_CODIGO
                        + " IN ('SKU-001','SKU-002','HER-1042','ELE-3390','SEG-0142')");
            } catch (Exception ignored) {
            }
            insertarProductosIniciales(db);
        }
        if (oldVersion < 8) {
            // v8 reemplaza el módulo de compras por envíos a empresas.
            try {
                db.execSQL("DROP TABLE IF EXISTS orden_compra_detalle");
                db.execSQL("DROP TABLE IF EXISTS orden_compra");
                db.execSQL("DROP TABLE IF EXISTS proveedor");
                db.execSQL("DELETE FROM " + TABLA_ACTIVIDAD + " WHERE " + COL_A_TIPO + " = 'compra'");
            } catch (Exception ignored) {
            }
            db.execSQL(CREATE_TABLE_EMPRESA);
            db.execSQL(CREATE_TABLE_ENVIO);
            db.execSQL(CREATE_TABLE_ENVIO_DETALLE);
            insertarEmpresasIniciales(db);
            insertarEnviosDemo(db);
        }
        if (oldVersion < 9) {
            // v9 agrega proveedores sin borrar productos ni envíos existentes.
            db.execSQL(CREATE_TABLE_PROVEEDOR);
            try {
                db.execSQL("ALTER TABLE " + TABLA_PRODUCTO
                        + " ADD COLUMN " + COL_P_PROVEEDOR_ID + " INTEGER");
            } catch (Exception ignored) {
                // La columna ya existe en instalaciones nuevas.
            }
            insertarProveedoresIniciales(db);
        }
    }

    private void insertarDatosIniciales(SQLiteDatabase db) {
        insertarProveedoresIniciales(db);
        insertarProductosIniciales(db);
        insertarUsuariosIniciales(db);
        insertarActividadesDemo(db);
        insertarEmpresasIniciales(db);
        insertarEnviosDemo(db);
    }

    /**
     * Proveedores o marcas comunes del rubro tecnológico.
     * Se usa INSERT OR IGNORE para que la migración no duplique nombres.
     */
    private void insertarProveedoresIniciales(SQLiteDatabase db) {
        insertarProveedor(db, "Levobo");
        insertarProveedor(db, "1Hora");
        insertarProveedor(db, "Ugreen");
        insertarProveedor(db, "Rizer");
        insertarProveedor(db, "Kingtone");
        insertarProveedor(db, "Marca China / Genérico");
        insertarProveedor(db, "Baseus");
        insertarProveedor(db, "Anker");
        insertarProveedor(db, "Hoco");
        insertarProveedor(db, "Borofone");
    }

    private void insertarProveedor(SQLiteDatabase db, String nombre) {
        ContentValues values = new ContentValues();
        values.put(COL_PR_NOMBRE, nombre);
        values.put(COL_PR_ACTIVO, 1);
        db.insertWithOnConflict(TABLA_PROVEEDOR, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Catálogo inicial de productos tecnológicos del almacén.
     * Se usa INSERT OR IGNORE (por el código único) para no duplicar datos.
     */
    private void insertarProductosIniciales(SQLiteDatabase db) {
        // Cables
        insertarProducto(db, "Extensor de cable USB-C 2m", "TEC-001", "Cables", 35, 10, "Pasillo 1 - Estante A", 25.90);
        insertarProducto(db, "Extensor de cable HDMI 3m", "TEC-002", "Cables", 18, 8, "Pasillo 1 - Estante A", 32.50);
        insertarProducto(db, "Extensor de cable de audio 3.5mm 5m", "TEC-003", "Cables", 22, 10, "Pasillo 1 - Estante B", 18.90);
        insertarProducto(db, "Extensor de cable DisplayPort 4K 2m", "TEC-004", "Cables", 0, 5, "Pasillo 1 - Estante B", 39.90);
        insertarProducto(db, "Extensor de cable de red RJ45 10m", "TEC-005", "Redes", 12, 6, "Pasillo 1 - Estante C", 29.90);

        // Cargadores
        insertarProducto(db, "Cargador rápido USB-C 65W", "TEC-006", "Cargadores", 40, 12, "Pasillo 2 - Estante A", 89.90);
        insertarProducto(db, "Cargador inalámbrico 15W", "TEC-007", "Cargadores", 25, 10, "Pasillo 2 - Estante A", 69.90);
        insertarProducto(db, "Cargador para laptop 90W", "TEC-008", "Cargadores", 7, 10, "Pasillo 2 - Estante B", 129.90);
        insertarProducto(db, "Cable de carga USB-C trenzado 1m", "TEC-009", "Cargadores", 60, 20, "Pasillo 2 - Estante C", 19.90);
        insertarProducto(db, "Estación de carga 5 puertos", "TEC-010", "Cargadores", 14, 6, "Pasillo 2 - Estante C", 149.90);

        // Audio
        insertarProducto(db, "Audífonos Bluetooth over-ear", "TEC-011", "Audio", 28, 10, "Pasillo 3 - Estante A", 149.90);
        insertarProducto(db, "Audífonos inalámbricos TWS", "TEC-012", "Audio", 45, 15, "Pasillo 3 - Estante A", 99.90);
        insertarProducto(db, "Parlante Bluetooth portátil", "TEC-013", "Audio", 16, 8, "Pasillo 3 - Estante B", 119.90);
        insertarProducto(db, "Micrófono USB para streaming", "TEC-014", "Audio", 9, 5, "Pasillo 3 - Estante B", 179.90);
        insertarProducto(db, "Audífonos con cancelación de ruido", "TEC-015", "Audio", 4, 8, "Pasillo 3 - Estante C", 259.90);

        // Cámaras
        insertarProducto(db, "Cámara de seguridad WiFi 1080p", "TEC-016", "Cámaras", 20, 8, "Pasillo 4 - Estante A", 159.90);
        insertarProducto(db, "Cámara web Full HD 1080p", "TEC-017", "Cámaras", 14, 6, "Pasillo 4 - Estante A", 129.90);
        insertarProducto(db, "Cámara domo CCTV 4MP", "TEC-018", "Cámaras", 0, 5, "Pasillo 4 - Estante B", 249.90);
        insertarProducto(db, "Cámara de acción 4K", "TEC-019", "Cámaras", 6, 5, "Pasillo 4 - Estante B", 399.90);

        // Radios
        insertarProducto(db, "Radio portátil FM/AM", "TEC-020", "Radios", 18, 8, "Pasillo 4 - Estante C", 89.90);
        insertarProducto(db, "Radio de comunicación UHF (par)", "TEC-021", "Radios", 11, 6, "Pasillo 4 - Estante C", 199.90);

        // Almacenamiento
        insertarProducto(db, "USB Kingston 64GB", "TEC-022", "Almacenamiento", 80, 25, "Pasillo 5 - Estante A", 39.90);
        insertarProducto(db, "USB SanDisk 128GB", "TEC-023", "Almacenamiento", 55, 20, "Pasillo 5 - Estante A", 59.90);
        insertarProducto(db, "Disco externo SSD 1TB", "TEC-024", "Almacenamiento", 6, 8, "Pasillo 5 - Estante B", 349.90);
        insertarProducto(db, "MicroSD 256GB Clase 10", "TEC-025", "Almacenamiento", 30, 12, "Pasillo 5 - Estante C", 79.90);

        // Periféricos
        insertarProducto(db, "Mouse inalámbrico ergonómico", "TEC-026", "Periféricos", 50, 15, "Pasillo 6 - Estante A", 49.90);
        insertarProducto(db, "Mouse gamer RGB 16000 DPI", "TEC-027", "Periféricos", 30, 10, "Pasillo 6 - Estante A", 129.90);
        insertarProducto(db, "Teclado mecánico RGB", "TEC-028", "Periféricos", 22, 8, "Pasillo 6 - Estante B", 199.90);
        insertarProducto(db, "Teclado inalámbrico + mouse", "TEC-029", "Periféricos", 35, 12, "Pasillo 6 - Estante B", 89.90);

        // Wearables
        insertarProducto(db, "Smartwatch deportivo", "TEC-030", "Wearables", 18, 8, "Pasillo 7 - Estante A", 249.90);
        insertarProducto(db, "Smartwatch GPS premium", "TEC-031", "Wearables", 5, 6, "Pasillo 7 - Estante A", 499.90);
        insertarProducto(db, "Smartband fitness", "TEC-032", "Wearables", 40, 15, "Pasillo 7 - Estante B", 129.90);

        // Accesorios y redes
        insertarProducto(db, "Hub USB-C 7 en 1", "TEC-033", "Accesorios", 26, 10, "Pasillo 7 - Estante C", 99.90);
        insertarProducto(db, "Soporte de laptop ajustable", "TEC-034", "Accesorios", 20, 8, "Pasillo 7 - Estante C", 79.90);
        insertarProducto(db, "Router WiFi 6 AX1800", "TEC-035", "Redes", 10, 5, "Pasillo 1 - Estante C", 289.90);
    }

    private void insertarProducto(SQLiteDatabase db, String nombre, String codigo, String categoria,
                                  int stock, int stockMinimo, String ubicacion, double precio) {
        ContentValues values = new ContentValues();
        values.put(COL_P_NOMBRE, nombre);
        values.put(COL_P_CODIGO, codigo);
        values.put(COL_P_CATEGORIA, categoria);
        values.put(COL_P_STOCK, stock);
        values.put(COL_P_STOCK_MINIMO, stockMinimo);
        values.put(COL_P_UBICACION, ubicacion);
        values.put(COL_P_PRECIO, precio);
        db.insertWithOnConflict(TABLA_PRODUCTO, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Historial de ejemplo para que el dashboard, la campanita y Reportes
     * tengan información desde la primera instalación.
     */
    private void insertarActividadesDemo(SQLiteDatabase db) {
        long ahora = System.currentTimeMillis();
        long minuto = 60L * 1000L;
        long hora = 60L * minuto;
        long dia = 24L * hora;

        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_AJUSTE,
                "ajustó stock de 'USB Kingston 64GB' a 80 uds", ahora - 20 * minuto);
        insertarActividadDemo(db, "Antony Yeren Lezama", "noche", Actividad.TIPO_ENTRADA,
                "registró entrada de 20 uds de 'Cargador rápido USB-C 65W'", ahora - 2 * hora);
        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_PRODUCTO,
                "registró el producto 'Smartwatch deportivo'", ahora - dia - 2 * hora);
        insertarActividadDemo(db, "Antony Yeren Lezama", "noche", Actividad.TIPO_DANO,
                "marcó 2 uds dañadas de 'Audífonos inalámbricos TWS'", ahora - dia - 6 * hora);
        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_AJUSTE,
                "actualizó el stock de 'Mouse inalámbrico ergonómico' a 50 uds", ahora - 2 * dia - 3 * hora);
        insertarActividadDemo(db, "Antony Yeren Lezama", "noche", Actividad.TIPO_ENTRADA,
                "registró entrada de 15 uds de 'Teclado mecánico RGB'", ahora - 2 * dia - 7 * hora);
        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_PRODUCTO,
                "registró el producto 'Cámara web Full HD 1080p'", ahora - 3 * dia - 4 * hora);
        insertarActividadDemo(db, "Antony Yeren Lezama", "noche", Actividad.TIPO_AJUSTE,
                "ajustó stock de 'Radio portátil FM/AM' a 18 uds", ahora - 4 * dia - 5 * hora);
        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_ENTRADA,
                "registró entrada de 10 uds de 'Extensor de cable HDMI 3m'", ahora - 5 * dia - 6 * hora);
        insertarActividadDemo(db, "Antony Yeren Lezama", "noche", Actividad.TIPO_PRODUCTO,
                "registró el producto 'Router WiFi 6 AX1800'", ahora - 6 * dia - 7 * hora);
    }

    private void insertarActividadDemo(SQLiteDatabase db, String usuario, String turno,
                                       String tipo, String descripcion, long fecha) {
        ContentValues values = new ContentValues();
        values.put(COL_A_USUARIO, usuario);
        values.put(COL_A_TURNO, turno);
        values.put(COL_A_TIPO, tipo);
        values.put(COL_A_DESCRIPCION, descripcion);
        values.put(COL_A_FECHA, fecha);
        db.insert(TABLA_ACTIVIDAD, null, values);
    }

    // ------------------------------------------------------------------
    // Empresas y envíos de ejemplo
    // ------------------------------------------------------------------

    private void insertarEmpresasIniciales(SQLiteDatabase db) {
        insertarEmpresa(db, "Hiraoka", "20100055237", "01 614 9000", "Atención a empresas");
        insertarEmpresa(db, "Coolbox", "20512345678", "01 615 8000", "Ventas corporativas");
        insertarEmpresa(db, "La Curacao", "20502086033", "01 615 6000", "Compras");
        insertarEmpresa(db, "Ripley", "20325943365", "01 611 5000", "Abastecimiento");
        insertarEmpresa(db, "Metro", "20493070744", "01 613 7000", "Logística");
    }

    private void insertarEmpresa(SQLiteDatabase db, String nombre, String ruc,
                                 String telefono, String contacto) {
        ContentValues values = new ContentValues();
        values.put(COL_E_NOMBRE, nombre);
        values.put(COL_E_RUC, ruc);
        values.put(COL_E_TELEFONO, telefono);
        values.put(COL_E_CONTACTO, contacto);
        values.put(COL_E_ACTIVO, 1);
        db.insertWithOnConflict(TABLA_EMPRESA, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Envíos demo para que el historial tenga contenido desde el primer inicio.
     * Solo se insertan si la tabla está vacía. No modifican el stock: el stock
     * actual ya representa el inventario después de esos despachos.
     */
    private void insertarEnviosDemo(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLA_ENVIO, null);
        int total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) total = cursor.getInt(0);
            cursor.close();
        }
        if (total > 0) return;

        long ahora = System.currentTimeMillis();
        long hora = 60L * 60L * 1000L;
        long dia = 24L * hora;

        insertarEnvioDemo(db, "ENV-0001", "Hiraoka", "REGISTRADO", ahora - 2 * dia,
                "Junior Ramos Flores", "Reposición de cargadores y mouse",
                new String[][]{
                        {"TEC-006", "6"},
                        {"TEC-026", "10"}
                });
        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_ENVIO,
                "envió 16 uds a Hiraoka (2 productos)", ahora - 2 * dia);

        insertarEnvioDemo(db, "ENV-0002", "Coolbox", "REGISTRADO", ahora - dia,
                "Antony Yeren Lezama", "Pedido de cámaras y memorias USB",
                new String[][]{
                        {"TEC-016", "4"},
                        {"TEC-022", "10"}
                });
        insertarActividadDemo(db, "Antony Yeren Lezama", "noche", Actividad.TIPO_ENVIO,
                "envió 14 uds a Coolbox (2 productos)", ahora - dia);

        insertarEnvioDemo(db, "ENV-0003", "Metro", "ANULADO", ahora - 3 * dia,
                "Junior Ramos Flores", "Envío cancelado por el cliente",
                new String[][]{
                        {"TEC-028", "2"}
                });
        insertarActividadDemo(db, "Junior Ramos Flores", "mañana", Actividad.TIPO_ENVIO,
                "anuló el envío ENV-0003 a Metro (stock restaurado)", ahora - 3 * dia + hora);
    }

    private void insertarEnvioDemo(SQLiteDatabase db, String numero, String empresaNombre,
                                   String estado, long fecha, String usuario,
                                   String observaciones, String[][] items) {
        if (existeEnvio(db, numero)) return;

        long empresaId = idEmpresaPorNombre(db, empresaNombre);
        if (empresaId <= 0) return;

        ContentValues envio = new ContentValues();
        envio.put(COL_ENV_NUMERO, numero);
        envio.put(COL_ENV_EMPRESA_ID, empresaId);
        envio.put(COL_ENV_FECHA, fecha);
        envio.put(COL_ENV_ESTADO, estado);
        envio.put(COL_ENV_OBSERVACIONES, observaciones);
        envio.put(COL_ENV_USUARIO, usuario);
        long envioId = db.insert(TABLA_ENVIO, null, envio);
        if (envioId <= 0) return;

        for (String[] item : items) {
            long productoId = idProductoPorCodigo(db, item[0]);
            if (productoId <= 0) continue;

            ContentValues detalle = new ContentValues();
            detalle.put(COL_ED_ENVIO_ID, envioId);
            detalle.put(COL_ED_PRODUCTO_ID, productoId);
            detalle.put(COL_ED_CANTIDAD, Integer.parseInt(item[1]));
            db.insert(TABLA_ENVIO_DETALLE, null, detalle);
        }
    }

    private boolean existeEnvio(SQLiteDatabase db, String numero) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLA_ENVIO
                + " WHERE " + COL_ENV_NUMERO + " = ?", new String[]{numero});
        boolean existe = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) existe = cursor.getInt(0) > 0;
            cursor.close();
        }
        return existe;
    }

    private long idEmpresaPorNombre(SQLiteDatabase db, String nombre) {
        Cursor cursor = db.rawQuery("SELECT " + COL_E_ID + " FROM " + TABLA_EMPRESA
                + " WHERE " + COL_E_NOMBRE + " = ?", new String[]{nombre});
        long id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) id = cursor.getLong(0);
            cursor.close();
        }
        return id;
    }

    private long idProductoPorCodigo(SQLiteDatabase db, String codigo) {
        Cursor cursor = db.rawQuery("SELECT " + COL_P_ID + " FROM " + TABLA_PRODUCTO
                + " WHERE " + COL_P_CODIGO + " = ?", new String[]{codigo});
        long id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) id = cursor.getLong(0);
            cursor.close();
        }
        return id;
    }

    /**
     * Usuarios iniciales de la app. Se usa INSERT OR IGNORE para que al migrar
     * desde una versión anterior no se dupliquen ni se pierdan usuarios.
     * NOTA: En producción usar hash (BCrypt). Aquí texto plano para simplicidad.
     */
    private void insertarUsuariosIniciales(SQLiteDatabase db) {
        String[] insertsUsuarios = {
                "INSERT OR IGNORE INTO " + TABLA_USUARIO + " (" + COL_U_USERNAME + ", " + COL_U_PASSWORD + ", " + COL_U_NOMBRE + ", " + COL_U_ROL + ", " + COL_U_TURNO + ") VALUES ('empleado', 'empleado11', 'Empleado Almacén', 'empleado', 'mañana')",
                "INSERT OR IGNORE INTO " + TABLA_USUARIO + " (" + COL_U_USERNAME + ", " + COL_U_PASSWORD + ", " + COL_U_NOMBRE + ", " + COL_U_ROL + ", " + COL_U_TURNO + ") VALUES ('junior', 'junior123', 'Junior Ramos Flores', 'empleado', 'mañana')",
                "INSERT OR IGNORE INTO " + TABLA_USUARIO + " (" + COL_U_USERNAME + ", " + COL_U_PASSWORD + ", " + COL_U_NOMBRE + ", " + COL_U_ROL + ", " + COL_U_TURNO + ") VALUES ('antony', 'antony123', 'Antony Yeren Lezama', 'empleado', 'noche')"
        };
        for (String sql : insertsUsuarios) {
            db.execSQL(sql);
        }
    }
}