package com.example.gestiondeinventario.vista;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.EnvioRepositorio;
import com.example.gestiondeinventario.modelo.Envio;
import com.example.gestiondeinventario.modelo.EnvioDetalle;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Detalle de un envío. Permite anularlo y devolver el stock al inventario.
 */
public class frmDetalleEnvio extends AppCompatActivity {

    public static final String EXTRA_ENVIO_ID = "envio_id";

    private EnvioRepositorio repositorio;
    private Envio envio;
    private int envioId;

    private LinearLayout contenedor;
    private TextView tvNumero;
    private TextView tvEstado;
    private TextView tvEmpresa;
    private TextView tvFecha;
    private TextView tvUsuario;
    private TextView tvObservaciones;
    private TextView tvTotal;
    private TextView tvAviso;
    private MaterialButton btnAnular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_detalle_envio);

        envioId = getIntent().getIntExtra(EXTRA_ENVIO_ID, -1);
        if (envioId <= 0) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        repositorio = new EnvioRepositorio(this);

        contenedor = findViewById(R.id.container_detalle_items);
        tvNumero = findViewById(R.id.tv_detalle_numero);
        tvEstado = findViewById(R.id.tv_detalle_estado);
        tvEmpresa = findViewById(R.id.tv_detalle_empresa);
        tvFecha = findViewById(R.id.tv_detalle_fecha);
        tvUsuario = findViewById(R.id.tv_detalle_usuario);
        tvObservaciones = findViewById(R.id.tv_detalle_observaciones);
        tvTotal = findViewById(R.id.tv_detalle_total);
        tvAviso = findViewById(R.id.tv_detalle_aviso);
        btnAnular = findViewById(R.id.btn_anular_envio);

        btnAnular.setOnClickListener(v -> confirmarAnulacion());

        cargar();
    }

    @Override
    protected void onDestroy() {
        if (repositorio != null) repositorio.cerrar();
        super.onDestroy();
    }

    private void cargar() {
        envio = repositorio.obtenerPorId(envioId);
        if (envio == null) {
            Toast.makeText(this, "El envío ya no existe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<EnvioDetalle> detalles = repositorio.obtenerDetalles(envioId);

        tvNumero.setText(envio.getNumero());
        tvEstado.setText(envio.getEstado());
        tvEstado.setTextColor(getColor(colorEstado(envio.getEstado())));
        tvEmpresa.setText("Enviado a: " + envio.getEmpresaNombre());

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvFecha.setText(formato.format(new Date(envio.getFecha())));
        tvUsuario.setText("Registrado por " + (envio.getUsuario() != null
                ? envio.getUsuario() : "Sistema"));

        if (envio.getObservaciones() != null && !envio.getObservaciones().trim().isEmpty()) {
            tvObservaciones.setVisibility(View.VISIBLE);
            tvObservaciones.setText(envio.getObservaciones());
        } else {
            tvObservaciones.setVisibility(View.GONE);
        }

        int total = 0;
        for (EnvioDetalle detalle : detalles) {
            total += detalle.getCantidad();
        }
        tvTotal.setText(total + " uds");

        boolean registrado = envio.estaRegistrado();
        btnAnular.setVisibility(registrado ? View.VISIBLE : View.GONE);
        if (registrado) {
            tvAviso.setText("Si anulas el envío, las unidades vuelven al stock del almacén.");
        } else {
            tvAviso.setText("Este envío fue anulado y el stock ya fue restaurado.");
        }

        mostrarDetalles(detalles);
    }

    private void mostrarDetalles(List<EnvioDetalle> detalles) {
        contenedor.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (EnvioDetalle detalle : detalles) {
            View row = inflater.inflate(R.layout.item_detalle_envio, contenedor, false);

            TextView tvNombre = row.findViewById(R.id.tv_item_nombre);
            TextView tvCantidad = row.findViewById(R.id.tv_item_cantidad);
            TextView tvCodigo = row.findViewById(R.id.tv_item_codigo);

            tvNombre.setText(detalle.getProductoNombre());
            tvCantidad.setText(detalle.getCantidad() + " uds");
            String codigo = detalle.getProductoCodigo() == null || detalle.getProductoCodigo().isEmpty()
                    ? "" : detalle.getProductoCodigo() + " · ";
            tvCodigo.setText(codigo + "Cantidad enviada: " + detalle.getCantidad());

            contenedor.addView(row);
        }

        if (detalles.isEmpty()) {
            TextView vacio = new TextView(this);
            vacio.setText("Este envío no tiene productos.");
            vacio.setTextColor(getColor(R.color.on_surface_variant));
            contenedor.addView(vacio);
        }
    }

    private void confirmarAnulacion() {
        new AlertDialog.Builder(this)
                .setTitle("Anular envío")
                .setMessage("¿Anular el envío " + envio.getNumero()
                        + " y devolver " + envio.getUnidades() + " uds al stock?")
                .setPositiveButton("Sí, anular", (dialog, which) -> {
                    if (repositorio.anularEnvio(envioId)) {
                        Toast.makeText(this, "Envío anulado. Stock restaurado.",
                                Toast.LENGTH_LONG).show();
                        cargar();
                    } else {
                        Toast.makeText(this, "No se pudo anular el envío",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private int colorEstado(String estado) {
        if (Envio.ESTADO_REGISTRADO.equals(estado)) return R.color.success;
        return R.color.on_surface_variant;
    }
}
