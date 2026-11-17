package com.example.gestiondeinventario.vista;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProveedorRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.modelo.Proveedor;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.example.gestiondeinventario.util.CategoriaUtil;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class frmInsertarProducto extends AppCompatActivity {

    private TextInputEditText editNombre, editCodigo, editStock, editStockMinimo, editPrecio;
    private TextInputLayout layoutNombre, layoutCodigo;
    private MaterialAutoCompleteTextView editCategoria, editProveedor;

    private ProductoRepositorio repositorio;
    private ProveedorRepositorio proveedorRepositorio;
    private ProductoNegocio negocio;

    private final List<String> categorias = new ArrayList<>();
    private ArrayAdapter<String> adaptadorCategorias;
    private String categoriaSeleccionada = CategoriaUtil.OTROS;

    private final List<Proveedor> proveedores = new ArrayList<>();
    private final List<String> nombresProveedores = new ArrayList<>();
    private ArrayAdapter<String> adaptadorProveedores;
    private int proveedorSeleccionadoId = 0;

    private boolean esEdicion = false;
    private Producto productoEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_agregar_producto);

        repositorio = new ProductoRepositorio(this);
        proveedorRepositorio = new ProveedorRepositorio(this);
        negocio = new ProductoNegocio();

        // Verificar si venimos en modo edición
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("producto")) {
            esEdicion = true;
            productoEditando = (Producto) intent.getSerializableExtra("producto");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Título según modo
        if (esEdicion && getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar producto");
        }

        editNombre = findViewById(R.id.edit_name);
        editCodigo = findViewById(R.id.edit_sku);
        editStock = findViewById(R.id.edit_stock);
        editStockMinimo = findViewById(R.id.edit_min_stock);
        editPrecio = findViewById(R.id.edit_price);

        layoutNombre = findViewById(R.id.layout_name);
        layoutCodigo = findViewById(R.id.layout_sku);

        configurarCategorias();
        configurarProveedores();

        // Si es edición, poblar campos y deshabilitar código (SKU único)
        if (esEdicion && productoEditando != null) {
            poblarCampos(productoEditando);
            seleccionarCategoria(productoEditando.getCategoria());
            seleccionarProveedor(productoEditando.getProveedorId());
            editCodigo.setEnabled(false); // SKU no se debe cambiar
            editCodigo.setAlpha(0.5f);
        } else {
            seleccionarCategoria(CategoriaUtil.OTROS);
            seleccionarProveedor(0);
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> guardarProducto());
        findViewById(R.id.btn_nuevo_proveedor).setOnClickListener(v -> mostrarDialogoNuevoProveedor());
    }

    private void configurarCategorias() {
        editCategoria = findViewById(R.id.edit_categoria);
        categorias.clear();
        categorias.addAll(CategoriaUtil.obtenerCategorias(this));

        if (esEdicion && productoEditando != null) {
            String actual = CategoriaUtil.normalizar(productoEditando.getCategoria());
            if (!categorias.contains(actual)) {
                int posicionOtros = categorias.indexOf(CategoriaUtil.OTROS);
                if (posicionOtros >= 0) categorias.add(posicionOtros, actual);
                else categorias.add(actual);
            }
        }

        if (!categorias.contains(CategoriaUtil.OTROS)) {
            categorias.add(CategoriaUtil.OTROS);
        }

        adaptadorCategorias = new ArrayAdapter<>(
                this,
                R.layout.item_dropdown_proveedor,
                categorias
        );
        editCategoria.setAdapter(adaptadorCategorias);
        editCategoria.setThreshold(0);
        editCategoria.setOnClickListener(v -> {
            if (adaptadorCategorias.getCount() > 0) editCategoria.showDropDown();
        });
        editCategoria.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < categorias.size()) {
                categoriaSeleccionada = categorias.get(position);
            }
        });
    }

    private void seleccionarCategoria(String categoria) {
        String normalizada = CategoriaUtil.normalizar(categoria);
        categoriaSeleccionada = normalizada;

        if (!categorias.contains(normalizada)) {
            int posicionOtros = categorias.indexOf(CategoriaUtil.OTROS);
            if (posicionOtros >= 0) categorias.add(posicionOtros, normalizada);
            else categorias.add(normalizada);
            if (adaptadorCategorias != null) adaptadorCategorias.notifyDataSetChanged();
        }

        editCategoria.setText(normalizada, false);
    }

    private void configurarProveedores() {
        editProveedor = findViewById(R.id.edit_proveedor);
        adaptadorProveedores = new ArrayAdapter<>(
                this,
                R.layout.item_dropdown_proveedor,
                nombresProveedores
        );
        editProveedor.setAdapter(adaptadorProveedores);
        editProveedor.setThreshold(0);
        editProveedor.setOnClickListener(v -> {
            if (adaptadorProveedores.getCount() > 0) editProveedor.showDropDown();
        });
        editProveedor.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < proveedores.size()) {
                proveedorSeleccionadoId = proveedores.get(position).getId();
            }
        });

        cargarProveedores();
    }

    private void cargarProveedores() {
        proveedores.clear();
        nombresProveedores.clear();

        proveedores.add(new Proveedor(0, "Sin proveedor"));
        nombresProveedores.add("Sin proveedor");

        try {
            List<Proveedor> lista = proveedorRepositorio.obtenerTodos();
            if (lista != null) {
                for (Proveedor p : lista) {
                    proveedores.add(p);
                    nombresProveedores.add(p.getNombre());
                }
            }
        } catch (Exception e) {
            Log.e("frmInsertarProducto", "Error cargando proveedores", e);
            Toast.makeText(this, "No se pudieron cargar los proveedores", Toast.LENGTH_SHORT).show();
        }

        if (adaptadorProveedores != null) adaptadorProveedores.notifyDataSetChanged();
    }

    private void seleccionarProveedor(int proveedorId) {
        proveedorSeleccionadoId = proveedorId;

        for (Proveedor p : proveedores) {
            if (p.getId() == proveedorId) {
                editProveedor.setText(p.getNombre(), false);
                return;
            }
        }

        if (proveedorId > 0) {
            editProveedor.setText("Proveedor no disponible", false);
        } else {
            editProveedor.setText("Sin proveedor", false);
        }
    }

    private void mostrarDialogoNuevoProveedor() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_proveedor, null);

        TextInputEditText etNombre = vista.findViewById(R.id.et_proveedor_nombre);
        TextInputEditText etTelefono = vista.findViewById(R.id.et_proveedor_telefono);
        TextInputEditText etContacto = vista.findViewById(R.id.et_proveedor_contacto);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nuevo proveedor")
                .setView(vista)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String nombre = texto(etNombre);
                    if (nombre.isEmpty()) {
                        etNombre.setError("Ingresa el nombre del proveedor");
                        etNombre.requestFocus();
                        return;
                    }

                    String telefono = texto(etTelefono);
                    String contacto = texto(etContacto);

                    long id = proveedorRepositorio.agregarProveedor(
                            new Proveedor(0, nombre, telefono, contacto, true)
                    );

                    if (id > 0) {
                        cargarProveedores();
                        seleccionarProveedor((int) id);
                        Toast.makeText(this, "Proveedor agregado", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        etNombre.setError("Ese proveedor ya existe o no se pudo guardar");
                        etNombre.requestFocus();
                    }
                }));

        dialog.show();
    }

    private String texto(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void poblarCampos(Producto p) {
        editNombre.setText(p.getNombre());
        editCodigo.setText(p.getCodigo());
        editStock.setText(String.valueOf(p.getStock()));
        editStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        editPrecio.setText(String.valueOf(p.getPrecio()));
    }

    private void guardarProducto() {
        String nombre = editNombre.getText() != null ? editNombre.getText().toString().trim() : "";
        String codigo = editCodigo.getText() != null ? editCodigo.getText().toString().trim() : "";

        String categoria = CategoriaUtil.normalizar(categoriaSeleccionada);

        String stockStr = editStock.getText() != null ? editStock.getText().toString().trim() : "";
        String minStockStr = editStockMinimo.getText() != null ? editStockMinimo.getText().toString().trim() : "";
        String ubicacion = esEdicion && productoEditando != null
                ? productoEditando.getUbicacion()
                : null;
        String precioStr = editPrecio.getText() != null ? editPrecio.getText().toString().trim() : "";

        String error = negocio.validarCampos(nombre, codigo, stockStr, minStockStr, precioStr);
        if (error != null) {
            if (error.contains("nombre")) layoutNombre.setError(error);
            else if (error.contains("código")) layoutCodigo.setError(error);
            else Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }

        layoutNombre.setError(null);
        layoutCodigo.setError(null);

        int stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
        int stockMinimo = minStockStr.isEmpty() ? 0 : Integer.parseInt(minStockStr);
        double precio = precioStr.isEmpty() ? 0.0 : Double.parseDouble(precioStr);

        boolean exito;
        if (esEdicion && productoEditando != null) {
            // MODO EDICIÓN: actualizar producto existente
            productoEditando.setNombre(nombre);
            // codigo no se cambia (es único)
            productoEditando.setCategoria(categoria);
            productoEditando.setStock(stock);
            productoEditando.setStockMinimo(stockMinimo);
            productoEditando.setUbicacion(ubicacion);
            productoEditando.setPrecio(precio);
            productoEditando.setProveedorId(proveedorSeleccionadoId);
            // codigoQr se mantiene igual

            int filas = repositorio.actualizarProducto(productoEditando);
            exito = filas > 0;
        } else {
            // MODO NUEVO: insertar
            Producto nuevoProducto = new Producto(
                    0, nombre, codigo, categoria, stock, stockMinimo,
                    ubicacion, precio, null, proveedorSeleccionadoId
            );
            long resultado = repositorio.agregarProducto(nuevoProducto);
            exito = resultado > 0;
        }

        if (exito) {
            registrarActividad(nombre, ubicacion);
            Toast.makeText(this, esEdicion ? "Producto actualizado" : "Producto guardado correctamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, esEdicion ? "Error al actualizar" : "Error al guardar (¿código duplicado?)", Toast.LENGTH_LONG).show();
        }
    }

    private void registrarActividad(String nombre, String ubicacion) {
        ActividadRepositorio actividadRepositorio = null;
        try {
            actividadRepositorio = new ActividadRepositorio(this);
            String descripcion;
            if (esEdicion) {
                descripcion = "actualizó '" + nombre + "'";
            } else if (ubicacion != null && !ubicacion.isEmpty()) {
                descripcion = "registró '" + nombre + "' en " + ubicacion;
            } else {
                descripcion = "registró '" + nombre + "'";
            }
            actividadRepositorio.registrar(Actividad.TIPO_PRODUCTO, descripcion);
        } catch (Exception e) {
            Log.e("frmInsertarProducto", "Error registrando actividad", e);
        } finally {
            if (actividadRepositorio != null) actividadRepositorio.cerrar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
        if (proveedorRepositorio != null) proveedorRepositorio.cerrar();
    }
}
