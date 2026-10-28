package com.example.gestiondeinventario.vista;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;

import java.util.List;

public class frmReportes extends Fragment {

    private static final String TAG = "frmReportes";
    private ProductoRepositorio repositorio;
    private ProductoNegocio negocio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_reportes, container, false);

        try {
            repositorio = new ProductoRepositorio(requireContext());
            negocio = new ProductoNegocio();
        } catch (Exception e) {
            Log.e(TAG, "Error init repositorio", e);
            Toast.makeText(getContext(), "Error BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return view;
        }

        cargarReportes(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && repositorio != null) {
            cargarReportes(getView());
        }
    }

    private void cargarReportes(View view) {
        try {
            List<Producto> productos = repositorio.obtenerTodos();

            // 1. Resumen general
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

            // Actualizar cards de resumen (buscar por ID en el layout)
            TextView tvTotalProductos = view.findViewById(R.id.tv_total_productos);
            TextView tvStockBajo = view.findViewById(R.id.tv_stock_bajo);
            TextView tvSinStock = view.findViewById(R.id.tv_sin_stock);
            TextView tvValorInventario = view.findViewById(R.id.tv_valor_inventario);

            if (tvTotalProductos != null) tvTotalProductos.setText(String.valueOf(totalProductos));
            if (tvStockBajo != null) tvStockBajo.setText(String.valueOf(stockBajo));
            if (tvSinStock != null) tvSinStock.setText(String.valueOf(sinStock));
            if (tvValorInventario != null) tvValorInventario.setText("S/ " + String.format("%.2f", valorTotal));

            // 2. Valor por categoría
            actualizarValorPorCategoria(view, productos);

            // 3. Alertas de stock bajo / sin stock
            actualizarAlertasStock(view, productos);
        } catch (Exception e) {
            Log.e(TAG, "Error cargarReportes", e);
            Toast.makeText(getContext(), "Error cargando reportes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarValorPorCategoria(View view, List<Producto> productos) {
        try {
            // Agrupar por categoría
            java.util.Map<String, Double> valorPorCat = new java.util.HashMap<>();
            for (Producto p : productos) {
                String cat = p.getCategoria();
                double valor = negocio.calcularValorInventario(p);
                valorPorCat.put(cat, valorPorCat.getOrDefault(cat, 0.0) + valor);
            }

            // Buscar contenedor y limpiar
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
    }
}