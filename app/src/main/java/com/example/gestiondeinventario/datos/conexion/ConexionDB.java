package com.example.gestiondeinventario.datos.conexion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ConexionDB extends SQLiteOpenHelper {

    public static final String NOMBRE_BD = "inventario.db";
    public static final int VERSION_BD = 3; // Incrementada para columna codigo_qr

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

    // Tabla USUARIO
    public static final String TABLA_USUARIO = "usuario";
    public static final String COL_U_ID = "id";
    public static final String COL_U_USERNAME = "username";
    public static final String COL_U_PASSWORD = "password";
    public static final String COL_U_NOMBRE = "nombre";
    public static final String COL_U_ROL = "rol";

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
                    COL_P_CODIGO_QR + " TEXT" +  // NUEVO: al final, nullable
                    ")";

    private static final String CREATE_TABLE_USUARIO =
            "CREATE TABLE " + TABLA_USUARIO + " (" +
                    COL_U_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_U_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COL_U_PASSWORD + " TEXT NOT NULL, " +
                    COL_U_NOMBRE + " TEXT, " +
                    COL_U_ROL + " TEXT DEFAULT 'empleado'" +
                    ")";

    public ConexionDB(Context context) {
        super(context, NOMBRE_BD, null, VERSION_BD);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PRODUCTO);
        db.execSQL(CREATE_TABLE_USUARIO);
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
    }

    private void insertarDatosIniciales(SQLiteDatabase db) {
        // Productos
        String[] insertsProductos = {
                "INSERT INTO " + TABLA_PRODUCTO + " (" + COL_P_NOMBRE + ", " + COL_P_CODIGO + ", " + COL_P_CATEGORIA + ", " + COL_P_STOCK + ", " + COL_P_STOCK_MINIMO + ", " + COL_P_UBICACION + ", " + COL_P_PRECIO + ") VALUES ('Puzo Clásico 60ml', 'SKU-001', 'Higiene', 8, 10, 'Pasillo 3 - Estante B', 8.50)",
                "INSERT INTO " + TABLA_PRODUCTO + " (" + COL_P_NOMBRE + ", " + COL_P_CODIGO + ", " + COL_P_CATEGORIA + ", " + COL_P_STOCK + ", " + COL_P_STOCK_MINIMO + ", " + COL_P_UBICACION + ", " + COL_P_PRECIO + ") VALUES ('Medicación 100ml', 'SKU-002', 'Farmacia', 45, 15, 'Pasillo 1 - Estante A', 35.00)",
                "INSERT INTO " + TABLA_PRODUCTO + " (" + COL_P_NOMBRE + ", " + COL_P_CODIGO + ", " + COL_P_CATEGORIA + ", " + COL_P_STOCK + ", " + COL_P_STOCK_MINIMO + ", " + COL_P_UBICACION + ", " + COL_P_PRECIO + ") VALUES ('Taladro inalámbrico 20V', 'HER-1042', 'Herramientas', 8, 10, 'Pasillo 3 - Estante B', 189.90)",
                "INSERT INTO " + TABLA_PRODUCTO + " (" + COL_P_NOMBRE + ", " + COL_P_CODIGO + ", " + COL_P_CATEGORIA + ", " + COL_P_STOCK + ", " + COL_P_STOCK_MINIMO + ", " + COL_P_UBICACION + ", " + COL_P_PRECIO + ") VALUES ('Cable eléctrico THW 12AWG', 'ELE-3390', 'Eléctrico', 120, 30, 'Pasillo 2 - Estante A', 3.20)",
                "INSERT INTO " + TABLA_PRODUCTO + " (" + COL_P_NOMBRE + ", " + COL_P_CODIGO + ", " + COL_P_CATEGORIA + ", " + COL_P_STOCK + ", " + COL_P_STOCK_MINIMO + ", " + COL_P_UBICACION + ", " + COL_P_PRECIO + ") VALUES ('Guantes de nitrilo (caja x100)', 'SEG-0142', 'Seguridad', 0, 25, 'Pasillo 1 - Estante B', 18.00)"
        };
        for (String sql : insertsProductos) {
            db.execSQL(sql);
        }

        // Usuario por defecto: empleado / empleado11
        // NOTA: En producción usar hash (BCrypt). Aquí texto plano para simplicidad.
        db.execSQL("INSERT INTO " + TABLA_USUARIO + " (" + COL_U_USERNAME + ", " + COL_U_PASSWORD + ", " + COL_U_NOMBRE + ", " + COL_U_ROL + ") VALUES ('empleado', 'empleado11', 'Empleado Almacén', 'empleado')");
    }
}