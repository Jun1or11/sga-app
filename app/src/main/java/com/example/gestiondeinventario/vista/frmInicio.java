package com.example.gestiondeinventario.vista;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gestiondeinventario.LoginActivity;
import com.example.gestiondeinventario.MainActivity;
import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.AsistenciaNegocio;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.example.gestiondeinventario.util.FechaUtil;

import java.util.Calendar;
import java.util.List;

public class frmInicio extends Fragment {

    private static final String TAG = "frmInicio";
    private ProductoRepositorio repositorio;
    private ActividadRepositorio actividadRepositorio;
    private ProductoNegocio negocio;
    private AsistenciaNegocio asistenciaNegocio;
    private ActivityResultLauncher<Intent> notificacionesLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificacionesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK
                            && result.getData() != null
                            && result.getData().getBooleanExtra(frmNotificaciones.EXTRA_VER_HISTORIAL, false)) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setSelectedTab(R.id.nav_reports);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_inicio, container, false);

        try {
            repositorio = new ProductoRepositorio(requireContext());
            actividadRepositorio = new ActividadRepositorio(requireContext());
            negocio = new ProductoNegocio();
            asistenciaNegocio = new AsistenciaNegocio();
        } catch (Exception e) {
            Log.e(TAG, "Error init repositorio", e);
            Toast.makeText(getContext(), "Error BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return view;
        }

        // Botones de acceso rápido
        view.findViewById(R.id.btn_add_home).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), frmInsertarProducto.class);
            startActivity(intent);
        });

        view.findViewById(R.id.btn_scan_home).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_scan);
            }
        });

        view.findViewById(R.id.btn_reports_home).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_reports);
            }
        });

        view.findViewById(R.id.btn_notifications).setOnClickListener(v ->
                notificacionesLauncher.launch(new Intent(requireContext(), frmNotificaciones.class)));

        view.findViewById(R.id.btn_envios_home).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), frmEnvios.class)));

        // Cargar dashboard
        cargarDashboard(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && repositorio != null) {
            cargarDashboard(getView());
        }
    }

    private void cargarDashboard(View view) {
        try {
            List<Producto> productos = repositorio.obtenerTodos();

            int totalProductos = productos.size();
            int stockBajo = 0;
            int sinStock = 0;
            double valorTotal = 0;

            StringBuilder alertas = new StringBuilder();
            boolean hayAlertas = false;

            for (Producto p : productos) {
                String estado = negocio.calcularEstado(p);
                double valor = negocio.calcularValorInventario(p);
                valorTotal += valor;

                if (estado.equals("Stock bajo")) {
                    stockBajo++;
                    if (alertas.length() > 0) alertas.append("\n");
                    alertas.append("• ").append(p.getNombre()).append(" - Quedan ").append(p.getStock()).append(" uds");
                    hayAlertas = true;
                } else if (estado.equals("Sin stock")) {
                    sinStock++;
                    if (alertas.length() > 0) alertas.append("\n");
                    alertas.append("• ").append(p.getNombre()).append(" - Sin stock");
                    hayAlertas = true;
                }
            }

            // Actualizar cards (null-safe)
            TextView tvTotal = view.findViewById(R.id.tv_total_productos_home);
            TextView tvBajo = view.findViewById(R.id.tv_stock_bajo_home);
            TextView tvValor = view.findViewById(R.id.tv_valor_inventario_home);
            TextView tvMovimientos = view.findViewById(R.id.tv_movimientos_home);
            TextView tvAlertas = view.findViewById(R.id.tv_alertas_home);

            if (tvTotal != null) tvTotal.setText(String.valueOf(totalProductos));
            if (tvBajo != null) tvBajo.setText(String.valueOf(stockBajo + sinStock));
            if (tvValor != null) tvValor.setText("S/ " + String.format("%.2f", valorTotal));
            if (tvMovimientos != null) tvMovimientos.setText(String.valueOf(actividadRepositorio.contarHoy()));

            cargarActividadReciente(view);
            actualizarBadgeNotificaciones(view, stockBajo + sinStock);

            if (tvAlertas != null && getContext() != null) {
                if (hayAlertas) {
                    tvAlertas.setText(alertas.toString());
                    tvAlertas.setTextColor(getContext().getColor(R.color.on_surface));
                } else {
                    tvAlertas.setText("✓ Todos los productos tienen stock suficiente");
                    tvAlertas.setTextColor(getContext().getColor(R.color.success));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargarDashboard", e);
            Toast.makeText(getContext(), "Error cargando dashboard: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarActividadReciente(View view) {
        LinearLayout contenedor = view.findViewById(R.id.contenedor_actividad);
        TextView tvVacia = view.findViewById(R.id.tv_actividad_vacia);
        if (contenedor == null) return;

        contenedor.removeAllViews();
        List<Actividad> actividades = actividadRepositorio.obtenerRecientes(5);

        if (actividades.isEmpty()) {
            if (tvVacia != null) tvVacia.setVisibility(View.VISIBLE);
            return;
        }
        if (tvVacia != null) tvVacia.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Actividad actividad : actividades) {
            View item = inflater.inflate(R.layout.item_actividad, contenedor, false);

            TextView icono = item.findViewById(R.id.tv_actividad_icono);
            TextView descripcion = item.findViewById(R.id.tv_actividad_descripcion);
            TextView fecha = item.findViewById(R.id.tv_actividad_fecha);

            if (icono != null) icono.setText(iconoPara(actividad.getTipo()));
            if (descripcion != null) descripcion.setText(actividad.getDescripcion());
            if (fecha != null) fecha.setText(FechaUtil.formatear(actividad.getFecha()));

            contenedor.addView(item);
        }
    }

    private void actualizarBadgeNotificaciones(View view, int alertasStock) {
        TextView badge = view.findViewById(R.id.tv_notif_badge);
        if (badge == null) return;

        int avisosHorario = contarAvisosHorario();
        int total = alertasStock + avisosHorario;

        if (total > 0) {
            badge.setText(total > 99 ? "99+" : String.valueOf(total));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private int contarAvisosHorario() {
        try {
            Calendar inicio = Calendar.getInstance();
            inicio.set(Calendar.HOUR_OF_DAY, 0);
            inicio.set(Calendar.MINUTE, 0);
            inicio.set(Calendar.SECOND, 0);
            inicio.set(Calendar.MILLISECOND, 0);

            List<Actividad> sesiones = actividadRepositorio.obtenerPorRango(
                    inicio.getTimeInMillis(), System.currentTimeMillis(), null, Actividad.TIPO_SESION);
            return asistenciaNegocio.avisos(asistenciaNegocio.resumir(sesiones)).size();
        } catch (Exception e) {
            Log.e(TAG, "Error contando avisos de horario", e);
            return 0;
        }
    }

    private String iconoPara(String tipo) {
        if (Actividad.TIPO_SESION.equals(tipo)
                || Actividad.TIPO_SESION_INICIO.equals(tipo)
                || Actividad.TIPO_SESION_FIN.equals(tipo)) return "👤";
        if (Actividad.TIPO_ENTRADA.equals(tipo)) return "⬇";
        if (Actividad.TIPO_DANO.equals(tipo)) return "⚠";
        if (Actividad.TIPO_PRODUCTO.equals(tipo)) return "📦";
        if (Actividad.TIPO_AJUSTE.equals(tipo)) return "✎";
        return "•";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
        if (actividadRepositorio != null) actividadRepositorio.cerrar();
    }
}