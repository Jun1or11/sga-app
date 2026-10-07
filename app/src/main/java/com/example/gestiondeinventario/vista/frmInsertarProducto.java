package com.example.gestiondeinventario.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class frmInsertarProducto extends AppCompatActivity {

    private TextInputEditText editNombre, editCodigo, editCategoria, editStock, editStockMinimo, editUbicacion, editPrecio;
    private TextInputLayout layoutNombre, layoutCodigo;

    private ProductoRepositorio repositorio;
    private ProductoNegocio negocio;

    private boolean esEdicion = false;
    private Producto productoEditando = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_agregar_producto);

        repositorio = new ProductoRepositorio(this);
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
        editCategoria = findViewById(R.id.edit_category);
        editStock = findViewById(R.id.edit_stock);
        editStockMinimo = findViewById(R.id.edit_min_stock);
        editUbicacion = findViewById(R.id.edit_location);
        editPrecio = findViewById(R.id.edit_price);

        layoutNombre = findViewById(R.id.layout_name);
        layoutCodigo = findViewById(R.id.layout_sku);

        // Si es edición, poblar campos y deshabilitar código (SKU único)
        if (esEdicion && productoEditando != null) {
            poblarCampos(productoEditando);
            editCodigo.setEnabled(false); // SKU no se debe cambiar
            editCodigo.setAlpha(0.5f);
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> guardarProducto());
    }

    private void poblarCampos(Producto p) {
        editNombre.setText(p.getNombre());
        editCodigo.setText(p.getCodigo());
        editCategoria.setText(p.getCategoria());
        editStock.setText(String.valueOf(p.getStock()));
        editStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        editUbicacion.setText(p.getUbicacion() != null ? p.getUbicacion() : "");
        editPrecio.setText(String.valueOf(p.getPrecio()));
    }

    private void guardarProducto() {
        String nombre = editNombre.getText() != null ? editNombre.getText().toString().trim() : "";
        String codigo = editCodigo.getText() != null ? editCodigo.getText().toString().trim() : "";
        String categoria = editCategoria.getText() != null ? editCategoria.getText().toString().trim() : "";
        String stockStr = editStock.getText() != null ? editStock.getText().toString().trim() : "";
        String minStockStr = editStockMinimo.getText() != null ? editStockMinimo.getText().toString().trim() : "";
        String ubicacion = editUbicacion.getText() != null ? editUbicacion.getText().toString().trim() : "";
        String precioStr = editPrecio.getText() != null ? editPrecio.getText().toString().trim() : "";

        String error = negocio.validarCampos(nombre, codigo, categoria, stockStr, minStockStr, precioStr);
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
            // codigoQr se mantiene igual

            int filas = repositorio.actualizarProducto(productoEditando);
            exito = filas > 0;
        } else {
            // MODO NUEVO: insertar
            Producto nuevoProducto = new Producto(0, nombre, codigo, categoria, stock, stockMinimo, ubicacion, precio);
            long resultado = repositorio.agregarProducto(nuevoProducto);
            exito = resultado > 0;
        }

        if (exito) {
            Toast.makeText(this, esEdicion ? "Producto actualizado" : "Producto guardado correctamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, esEdicion ? "Error al actualizar" : "Error al guardar (¿código duplicado?)", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
    }
}