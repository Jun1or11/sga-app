package com.example.gestiondeinventario.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.datos.repositorio.EnvioRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.AsistenciaNegocio;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.example.gestiondeinventario.util.TurnoUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel de notificaciones (campanita de Inicio).
 * Muestra solo RESUMEN: alertas de stock, asistencia de hoy y avisos de horario.
 * El detalle completo con filtro de fechas está en Reportes.
 */
public class frmNotificaciones extends AppCompatActivity {

    public static final String EXTRA_VER_HISTORIAL = "ver_historial";

    private ProductoRepositorio productoRepositorio;
    private ActividadRepositorio actividadRepositorio;
    private EnvioRepositorio envioRepositorio;
    private ProductoNegocio productoNegocio;
    private AsistenciaNegocio asistenciaNegocio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_notificaciones);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        productoRepositorio = new ProductoRepositorio(this);
        actividadRepositorio = new ActividadRepositorio(this);
        envioRepositorio = new EnvioRepositorio(this);
        productoNegocio = new ProductoNegocio();
        asistenciaNegocio = new AsistenciaNegocio();

        MaterialButton btnHistorial = findViewById(R.id.btn_ver_historial);
        btnHistorial.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_VER_HISTORIAL, true);
            setResult(RESULT_OK, data);
            finish();
        });

        cargar();
    }

    private void cargar() {
        long ahora = System.currentTimeMillis();
        long inicioDia = inicioDelDia();

        List<Producto> productos = productoRepositorio.obtenerTodos();
        List<Producto> alertas = new ArrayList<>();
        for (Producto p : productos) {
            String estado = productoNegocio.calcularEstado(p);
            if ("Stock bajo".equals(estado) || "Sin stock".equals(estado)) {
                alertas.add(p);
            }
        }

        List<Actividad> actividadesHoy = actividadRepositorio.obtenerPorRango(inicioDia, ahora, null, null);
        List<Actividad> sesionesHoy = actividadRepositorio.obtenerPorRango(inicioDia, ahora, null, Actividad.TIPO_SESION);

        int danados = 0;
        for (Actividad a : actividadesHoy) {
            if (Actividad.TIPO_DANO.equals(a.getTipo())) danados++;
        }
        int movimientos = Math.max(0, actividadesHoy.size() - sesionesHoy.size());

        List<AsistenciaNegocio.Fila> filas = asistenciaNegocio.resumir(sesionesHoy);
        List<String> avisos = asistenciaNegocio.avisos(filas);

        TextView tvResumen = findViewById(R.id.tv_resumen);
        String alertasTxt = alertas.size() + " "
                + (alertas.size() == 1 ? "alerta de stock" : "alertas de stock");
        String danadosTxt = danados + " " + (danados == 1 ? "dañado" : "dañados");
        String empleadosTxt = filas.size() + " "
                + (filas.size() == 1 ? "empleado registrado hoy" : "empleados registrados hoy");
        int enviosHoy = envioRepositorio.contarDesde(inicioDia);
        String enviosTxt = enviosHoy + " "
                + (enviosHoy == 1 ? "envío hoy" : "envíos hoy");
        tvResumen.setText(movimientos + " " + (movimientos == 1 ? "movimiento" : "movimientos")
                + " · " + alertasTxt + " · " + danadosTxt + "\n"
                + empleadosTxt + " · " + enviosTxt);

        mostrarAlertas(alertas);
        mostrarAsistencia(filas);
        mostrarAvisos(avisos);
    }

    private void mostrarAlertas(List<Producto> alertas) {
        LinearLayout contenedor = findViewById(R.id.container_alertas);
        contenedor.removeAllViews();

        if (alertas.isEmpty()) {
            contenedor.addView(texto("✓ Sin alertas de stock", 14, R.color.success));
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Producto p : alertas) {
            View row = inflater.inflate(R.layout.item_alerta_stock, contenedor, false);
            TextView tv = row.findViewById(R.id.tv_alerta_text);
            String estado = productoNegocio.calcularEstado(p);
            boolean sinStock = "Sin stock".equals(estado);
            String texto = (sinStock ? "⚠ " : "⚠ ") + p.getNombre() + " — "
                    + (sinStock ? "sin stock" : "quedan " + p.getStock() + " uds (mín. " + p.getStockMinimo() + ")");
            tv.setText(texto);
            tv.setTextColor(getColor(sinStock ? R.color.error : R.color.warning));
            contenedor.addView(row);
        }
    }

    private void mostrarAsistencia(List<AsistenciaNegocio.Fila> filas) {
        LinearLayout contenedor = findViewById(R.id.container_asistencia);
        contenedor.removeAllViews();

        if (filas.isEmpty()) {
            contenedor.addView(texto("Todavía no hay sesiones registradas hoy", 14, R.color.on_surface_variant));
            return;
        }

        Map<String, List<AsistenciaNegocio.Fila>> porTurno = new LinkedHashMap<>();
        porTurno.put("mañana", new ArrayList<>());
        porTurno.put("noche", new ArrayList<>());
        for (AsistenciaNegocio.Fila fila : filas) {
            String clave = TurnoUtil.esNoche(fila.turno) ? "noche" : "mañana";
            porTurno.get(clave).add(fila);
        }

        for (Map.Entry<String, List<AsistenciaNegocio.Fila>> entry : porTurno.entrySet()) {
            List<AsistenciaNegocio.Fila> lista = entry.getValue();
            if (lista.isEmpty()) continue;

            TextView header = texto(TurnoUtil.icono(entry.getKey()) + " " + TurnoUtil.etiqueta(entry.getKey()),
                    14, R.color.primary);
            header.setPadding(0, 8, 0, 4);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            contenedor.addView(header);

            for (AsistenciaNegocio.Fila fila : lista) {
                TextView nombre = texto("👤 " + fila.usuario, 14, R.color.on_surface);
                nombre.setPadding(16, 4, 0, 0);
                contenedor.addView(nombre);

                String entrada = fila.entrada != null ? TurnoUtil.formatoHora(fila.entrada) : "--:--";
                String salida = fila.salida != null ? TurnoUtil.formatoHora(fila.salida) : "(en turno)";
                TextView detalle = texto("Entrada " + entrada + " · Salida " + salida, 13, R.color.on_surface_variant);
                detalle.setPadding(32, 0, 0, 6);
                contenedor.addView(detalle);
            }
        }
    }

    private void mostrarAvisos(List<String> avisos) {
        LinearLayout contenedor = findViewById(R.id.container_avisos);
        contenedor.removeAllViews();

        if (avisos.isEmpty()) {
            contenedor.addView(texto("✓ Todos dentro de su horario", 14, R.color.success));
            return;
        }

        for (String aviso : avisos) {
            contenedor.addView(texto("• " + aviso, 14, R.color.warning));
        }
    }

    private TextView texto(String contenido, int sizeSp, int colorRes) {
        TextView tv = new TextView(this);
        tv.setText(contenido);
        tv.setTextSize(sizeSp);
        tv.setTextColor(getColor(colorRes));
        tv.setPadding(0, 2, 0, 2);
        return tv;
    }

    private long inicioDelDia() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (productoRepositorio != null) productoRepositorio.cerrar();
        if (actividadRepositorio != null) actividadRepositorio.cerrar();
        if (envioRepositorio != null) envioRepositorio.cerrar();
    }
}
