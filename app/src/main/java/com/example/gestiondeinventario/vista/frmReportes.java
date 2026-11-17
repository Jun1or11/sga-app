package com.example.gestiondeinventario.vista;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ActividadRepositorio;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Actividad;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.example.gestiondeinventario.util.CategoriaUtil;
import com.example.gestiondeinventario.util.FechaUtil;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class frmReportes extends Fragment {

    private static final String TAG = "frmReportes";
    private static final long UN_DIA = 24L * 60L * 60L * 1000L;

    private static final String[] ETIQUETAS_TIPO = {
            "Todos los tipos", "Sesiones", "Entradas", "Ajustes", "Dañados", "Productos", "Envíos"
    };
    private static final String[] VALORES_TIPO = {
            null, Actividad.TIPO_SESION, Actividad.TIPO_ENTRADA,
            Actividad.TIPO_AJUSTE, Actividad.TIPO_DANO, Actividad.TIPO_PRODUCTO,
            Actividad.TIPO_ENVIO
    };

    private ProductoRepositorio repositorio;
    private ActividadRepositorio actividadRepositorio;
    private ProductoNegocio negocio;

    private long filtroDesde;
    private long filtroHasta;
    private String filtroEmpleado = null;
    private String filtroTipo = null;

    private List<String> listaEmpleados = new ArrayList<>();
    private boolean ajustandoFiltros = true;
    private int ultimoChipRango = R.id.chip_7dias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_reportes, container, false);

        try {
            repositorio = new ProductoRepositorio(requireContext());
            actividadRepositorio = new ActividadRepositorio(requireContext());
            negocio = new ProductoNegocio();
        } catch (Exception e) {
            Log.e(TAG, "Error init repositorio", e);
            Toast.makeText(getContext(), "Error BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return view;
        }

        configurarFiltros(view);
        cargarReportes(view);
        cargarHistorial(view);
        cargarGrafico(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && repositorio != null) {
            cargarReportes(getView());
            cargarHistorial(getView());
            cargarGrafico(getView());
        }
    }

    // ------------------------------------------------------------------
    // Reportes existentes (resumen, categorías, alertas)
    // ------------------------------------------------------------------

    private void cargarReportes(View view) {
        try {
            List<Producto> productos = repositorio.obtenerTodos();

            int totalProductos = productos.size();
            int stockBajo = 0;
            int sinStock = 0;
            double valorTotal = 0;

            for (Producto p : productos) {
                String estado = negocio.calcularEstado(p);
                if (estado.equals("Stock bajo")) stockBajo++;
                else if (estado.equals("Sin stock")) sinStock++;
                valorTotal += negocio.calcularValorInventario(p);
            }

            TextView tvTotalProductos = view.findViewById(R.id.tv_total_productos);
            TextView tvStockBajo = view.findViewById(R.id.tv_stock_bajo);
            TextView tvSinStock = view.findViewById(R.id.tv_sin_stock);
            TextView tvValorInventario = view.findViewById(R.id.tv_valor_inventario);

            if (tvTotalProductos != null) tvTotalProductos.setText(String.valueOf(totalProductos));
            if (tvStockBajo != null) tvStockBajo.setText(String.valueOf(stockBajo));
            if (tvSinStock != null) tvSinStock.setText(String.valueOf(sinStock));
            if (tvValorInventario != null) tvValorInventario.setText("S/ " + String.format("%.2f", valorTotal));

            actualizarValorPorCategoria(view, productos);
            actualizarAlertasStock(view, productos);
        } catch (Exception e) {
            Log.e(TAG, "Error cargarReportes", e);
            Toast.makeText(getContext(), "Error cargando reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarValorPorCategoria(View view, List<Producto> productos) {
        try {
            java.util.Map<String, Double> valorPorCat = new java.util.HashMap<>();
            for (Producto p : productos) {
                String cat = CategoriaUtil.normalizar(p.getCategoria());
                double valor = negocio.calcularValorInventario(p);
                valorPorCat.put(cat, valorPorCat.getOrDefault(cat, 0.0) + valor);
            }

            ViewGroup container = view.findViewById(R.id.container_valor_categoria);
            if (container == null) return;
            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (java.util.Map.Entry<String, Double> entry : valorPorCat.entrySet()) {
                View row = inflater.inflate(R.layout.item_categoria_valor, container, false);
                TextView tvCat = row.findViewById(R.id.tv_cat_name);
                TextView tvVal = row.findViewById(R.id.tv_cat_value);
                if (tvCat != null) tvCat.setText(entry.getKey());
                if (tvVal != null) tvVal.setText("S/ " + String.format("%.2f", entry.getValue()));
                container.addView(row);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error actualizarValorPorCategoria", e);
        }
    }

    private void actualizarAlertasStock(View view, List<Producto> productos) {
        try {
            ViewGroup container = view.findViewById(R.id.container_alertas_stock);
            if (container == null) return;
            container.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            boolean hayAlertas = false;

            for (Producto p : productos) {
                String estado = negocio.calcularEstado(p);
                if (estado.equals("Stock bajo") || estado.equals("Sin stock")) {
                    hayAlertas = true;
                    View row = inflater.inflate(R.layout.item_alerta_stock, container, false);
                    TextView tvAlerta = row.findViewById(R.id.tv_alerta_text);
                    if (tvAlerta != null) {
                        String texto = "• " + p.getNombre() + " (" + estado.toLowerCase() + ": " + p.getStock() + " uds)";
                        tvAlerta.setText(texto);
                        int color = estado.equals("Sin stock") ?
                                requireContext().getColor(R.color.error) :
                                requireContext().getColor(R.color.warning);
                        tvAlerta.setTextColor(color);
                    }
                    container.addView(row);
                }
            }

            if (!hayAlertas) {
                TextView tv = new TextView(getContext());
                tv.setText("✓ Todos los productos tienen stock suficiente");
                tv.setTextColor(requireContext().getColor(R.color.success));
                tv.setTextSize(14);
                tv.setPadding(16, 16, 16, 16);
                container.addView(tv);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error actualizarAlertasStock", e);
        }
    }

    // ------------------------------------------------------------------
    // Historial de actividad con filtro por fechas
    // ------------------------------------------------------------------

    private void configurarFiltros(View view) {
        filtroDesde = inicioDelDia() - 6 * UN_DIA;
        filtroHasta = System.currentTimeMillis();

        Spinner spinnerEmpleado = view.findViewById(R.id.spinner_empleado);
        Spinner spinnerTipo = view.findViewById(R.id.spinner_tipo);

        listaEmpleados = new ArrayList<>();
        listaEmpleados.add("Todos los empleados");
        listaEmpleados.addAll(actividadRepositorio.obtenerUsuariosDistintos());

        ArrayAdapter<String> adaptadorEmpleado = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, listaEmpleados);
        adaptadorEmpleado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEmpleado.setAdapter(adaptadorEmpleado);

        ArrayAdapter<String> adaptadorTipo = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ETIQUETAS_TIPO);
        adaptadorTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adaptadorTipo);

        spinnerEmpleado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                filtroEmpleado = position == 0 ? null : listaEmpleados.get(position);
                if (!ajustandoFiltros) cargarHistorial(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                filtroTipo = VALORES_TIPO[position];
                if (!ajustandoFiltros) cargarHistorial(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ChipGroup grupo = view.findViewById(R.id.chip_group_rango);
        grupo.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            if (ajustandoFiltros) return;
            int id = checkedIds.get(0);

            if (id == R.id.chip_hoy) {
                filtroDesde = inicioDelDia();
                filtroHasta = System.currentTimeMillis();
            } else if (id == R.id.chip_ayer) {
                filtroDesde = inicioDelDia() - UN_DIA;
                filtroHasta = inicioDelDia() - 1;
            } else if (id == R.id.chip_7dias) {
                filtroDesde = inicioDelDia() - 6 * UN_DIA;
                filtroHasta = System.currentTimeMillis();
            } else if (id == R.id.chip_mes) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                filtroDesde = c.getTimeInMillis();
                filtroHasta = System.currentTimeMillis();
            } else if (id == R.id.chip_personalizado) {
                pedirRangoPersonalizado(view, group);
                return;
            }

            ultimoChipRango = id;
            cargarHistorial(view);
        });

        grupo.check(R.id.chip_7dias);
        ajustandoFiltros = false;
    }

    private void pedirRangoPersonalizado(View view, ChipGroup grupo) {
        Calendar inicial = Calendar.getInstance();
        inicial.setTimeInMillis(filtroDesde);

        DatePickerDialog dialogoDesde = new DatePickerDialog(requireContext(),
                (vista, year, month, day) -> {
                    Calendar desde = Calendar.getInstance();
                    desde.set(year, month, day, 0, 0, 0);
                    desde.set(Calendar.MILLISECOND, 0);

                    Calendar limite = Calendar.getInstance();
                    limite.setTimeInMillis(desde.getTimeInMillis());

                    DatePickerDialog dialogoHasta = new DatePickerDialog(requireContext(),
                            (vista2, year2, month2, day2) -> {
                                Calendar hasta = Calendar.getInstance();
                                hasta.set(year2, month2, day2, 23, 59, 59);
                                hasta.set(Calendar.MILLISECOND, 999);

                                if (hasta.getTimeInMillis() < desde.getTimeInMillis()) {
                                    Toast.makeText(getContext(), "La fecha final no puede ser menor", Toast.LENGTH_SHORT).show();
                                    ajustandoFiltros = true;
                                    grupo.check(ultimoChipRango);
                                    ajustandoFiltros = false;
                                    return;
                                }

                                filtroDesde = desde.getTimeInMillis();
                                filtroHasta = hasta.getTimeInMillis();
                                ultimoChipRango = R.id.chip_personalizado;
                                cargarHistorial(view);
                            },
                            limite.get(Calendar.YEAR), limite.get(Calendar.MONTH), limite.get(Calendar.DAY_OF_MONTH));
                    dialogoHasta.setTitle("Hasta");
                    dialogoHasta.setOnCancelListener(d -> {
                        ajustandoFiltros = true;
                        grupo.check(ultimoChipRango);
                        ajustandoFiltros = false;
                    });
                    dialogoHasta.show();
                },
                inicial.get(Calendar.YEAR), inicial.get(Calendar.MONTH), inicial.get(Calendar.DAY_OF_MONTH));
        dialogoDesde.setTitle("Desde");
        dialogoDesde.setOnCancelListener(d -> {
            ajustandoFiltros = true;
            grupo.check(ultimoChipRango);
            ajustandoFiltros = false;
        });
        dialogoDesde.show();
    }

    private void cargarHistorial(View view) {
        try {
            List<Actividad> actividades = actividadRepositorio.obtenerPorRango(
                    filtroDesde, filtroHasta, filtroEmpleado, filtroTipo);

            TextView tvRango = view.findViewById(R.id.tv_historial_rango);
            if (tvRango != null) {
                tvRango.setText("Del " + fechaCorta(filtroDesde) + " al " + fechaCorta(filtroHasta)
                        + " · " + actividades.size() + " "
                        + (actividades.size() == 1 ? "actividad" : "actividades"));
            }

            LinearLayout contenedor = view.findViewById(R.id.container_historial);
            if (contenedor == null) return;
            contenedor.removeAllViews();

            if (actividades.isEmpty()) {
                TextView vacio = new TextView(getContext());
                vacio.setText("Sin actividades en este rango de fechas");
                vacio.setTextColor(requireContext().getColor(R.color.on_surface_variant));
                vacio.setTextSize(14);
                vacio.setPadding(0, 16, 0, 16);
                contenedor.addView(vacio);
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(getContext());
            String diaActual = null;

            for (Actividad actividad : actividades) {
                String dia = fechaCorta(actividad.getFecha());
                if (!dia.equals(diaActual)) {
                    diaActual = dia;
                    TextView header = new TextView(getContext());
                    header.setText(dia);
                    header.setTextColor(requireContext().getColor(R.color.primary));
                    header.setTextSize(13);
                    header.setTypeface(null, android.graphics.Typeface.BOLD);
                    header.setPadding(0, 16, 0, 4);
                    contenedor.addView(header);
                }

                View item = inflater.inflate(R.layout.item_actividad, contenedor, false);
                TextView icono = item.findViewById(R.id.tv_actividad_icono);
                TextView descripcion = item.findViewById(R.id.tv_actividad_descripcion);
                TextView fecha = item.findViewById(R.id.tv_actividad_fecha);

                if (icono != null) icono.setText(iconoPara(actividad.getTipo()));
                if (descripcion != null) descripcion.setText(actividad.getDescripcion());
                if (fecha != null) fecha.setText(FechaUtil.formatear(actividad.getFecha()));

                contenedor.addView(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargarHistorial", e);
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
        if (Actividad.TIPO_ENVIO.equals(tipo)) return "🚚";
        return "•";
    }

    // ------------------------------------------------------------------
    // Gráfico de movimientos (últimos 7 días, datos reales)
    // ------------------------------------------------------------------

    private void cargarGrafico(View view) {
        try {
            LinearLayout contenedor = view.findViewById(R.id.container_grafico);
            LinearLayout etiquetas = view.findViewById(R.id.container_grafico_labels);
            if (contenedor == null || etiquetas == null) return;

            contenedor.removeAllViews();
            etiquetas.removeAllViews();

            long inicioHoy = inicioDelDia();
            int[] conteos = new int[7];
            Calendar dia = Calendar.getInstance();
            dia.setTimeInMillis(inicioHoy - 6 * UN_DIA);

            int maximo = 1;
            for (int i = 0; i < 7; i++) {
                long desde = dia.getTimeInMillis();
                long hasta = desde + UN_DIA - 1;
                conteos[i] = actividadRepositorio.contarEntre(desde, hasta);
                if (conteos[i] > maximo) maximo = conteos[i];
                dia.add(Calendar.DAY_OF_YEAR, 1);
            }

            dia.setTimeInMillis(inicioHoy - 6 * UN_DIA);
            String[] iniciales = {"D", "L", "M", "X", "J", "V", "S"};

            for (int i = 0; i < 7; i++) {
                int alturaDp = 4 + (int) (96.0 * conteos[i] / maximo);
                View barra = new View(requireContext());
                LinearLayout.LayoutParams lpBarra = new LinearLayout.LayoutParams(
                        0, dp(alturaDp), 1f);
                lpBarra.setMargins(dp(3), 0, dp(3), 0);
                barra.setLayoutParams(lpBarra);
                barra.setBackgroundColor(requireContext().getColor(
                        i == 6 ? R.color.primary : R.color.secondary_variant));
                contenedor.addView(barra);

                TextView etiqueta = new TextView(requireContext());
                etiqueta.setText(iniciales[dia.get(Calendar.DAY_OF_WEEK) - 1]);
                etiqueta.setTextSize(10);
                etiqueta.setGravity(android.view.Gravity.CENTER);
                etiqueta.setTextColor(requireContext().getColor(R.color.on_surface_variant));
                LinearLayout.LayoutParams lpEtiqueta = new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                etiqueta.setLayoutParams(lpEtiqueta);
                etiquetas.addView(etiqueta);

                dia.add(Calendar.DAY_OF_YEAR, 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cargarGrafico", e);
        }
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private long inicioDelDia() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private String fechaCorta(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
        if (actividadRepositorio != null) actividadRepositorio.cerrar();
    }
}
