package com.example.gestiondeinventario.vista;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.controlador.ProductoControlador;
import com.example.gestiondeinventario.datos.repositorio.ProductoRepositorio;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;
import com.example.gestiondeinventario.vista.adaptador.AdaptadorProducto;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class frmInventario extends Fragment implements AdaptadorProducto.OnProductoClickListener {

    private static final String TAG = "frmInventario";

    private AdaptadorProducto adaptador;
    private List<Producto> todosLosProductos;
    private String filtroActual = "Todos";
    private String busquedaActual = "";

    private ProductoRepositorio repositorio;
    private ProductoNegocio negocio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_inventario, container, false);

        try {
            repositorio = new ProductoRepositorio(requireContext());
            negocio = new ProductoNegocio();
        } catch (Exception e) {
            Log.e(TAG, "Error init repositorio", e);
            Toast.makeText(getContext(), "Error BD: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return view;
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_products);
        // Asegurar LayoutManager (aunque esté en XML, por seguridad)
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        // Carga inicial
        cargarProductos();

        adaptador = new AdaptadorProducto(new ArrayList<>(todosLosProductos), this, negocio);
        recyclerView.setAdapter(adaptador);

        TextInputEditText searchEditText = view.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                busquedaActual = s.toString().toLowerCase();
                aplicarFiltros();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ChipGroup filterGroup = view.findViewById(R.id.filter_chip_group);
        filterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                filtroActual = "Todos";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.chip_all) filtroActual = "Todos";
                else if (id == R.id.chip_low_stock) filtroActual = "Stock bajo";
                else if (id == R.id.chip_out_of_stock) filtroActual = "Sin stock";
            }
            aplicarFiltros();
        });

        view.findViewById(R.id.fab_add_product).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), frmInsertarProducto.class);
            startActivity(intent);
        });

        return view;
    }

    private void cargarProductos() {
        try {
            todosLosProductos = repositorio.obtenerTodos();
            if (todosLosProductos == null) {
                todosLosProductos = new ArrayList<>();
            }
            Log.d(TAG, "Productos cargados: " + todosLosProductos.size());
        } catch (Exception e) {
            Log.e(TAG, "Error cargarProductos", e);
            todosLosProductos = new ArrayList<>();
            Toast.makeText(getContext(), "Error cargando productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void aplicarFiltros() {
        if (todosLosProductos == null || adaptador == null) return;

        try {
            List<Producto> filtrados = new ArrayList<>();
            for (Producto p : todosLosProductos) {
                boolean coincideBusqueda = p.getNombre().toLowerCase().contains(busquedaActual) ||
                        p.getCodigo().toLowerCase().contains(busquedaActual);

                boolean coincideFiltro = filtroActual.equals("Todos") ||
                        negocio.calcularEstado(p).equals(filtroActual);

                if (coincideBusqueda && coincideFiltro) {
                    filtrados.add(p);
                }
            }
            Log.d(TAG, "Filtrados: " + filtrados.size() + " de " + todosLosProductos.size());
            adaptador.actualizarLista(filtrados);
        } catch (Exception e) {
            Log.e(TAG, "Error aplicarFiltros", e);
        }
    }

    // Método público para forzar refresco desde fuera si hace falta
    public void refrescarLista() {
        cargarProductos();
        aplicarFiltros();
    }

    @Override
    public void onProductoClick(Producto producto) {
        Intent intent = new Intent(getActivity(), frmDetalleProducto.class);
        intent.putExtra("producto", producto);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - refrescando lista");
        if (repositorio != null) {
            cargarProductos();
            aplicarFiltros();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (repositorio != null) {
            repositorio.cerrar();
        }
    }
}