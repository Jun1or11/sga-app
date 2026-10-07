package com.example.gestiondeinventario.vista;

import android.content.Intent;
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

import com.example.gestiondeinventario.LoginActivity;
import com.example.gestiondeinventario.MainActivity;
import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;

import java.util.List;

public class frmInicio extends Fragment {

    private static final String TAG = "frmInicio";
    private ProductoRepositorio repositorio;
    private ProductoNegocio negocio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_inicio, container, false);

        try {
            repositorio = new ProductoRepositorio(requireContext());
            negocio = new ProductoNegocio();
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
            if (tvMovimientos != null) tvMovimientos.setText("--");

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repositorio != null) repositorio.cerrar();
    }
}