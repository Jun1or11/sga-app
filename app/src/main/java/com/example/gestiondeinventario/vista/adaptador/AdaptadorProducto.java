package com.example.gestiondeinventario.vista.adaptador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.modelo.Producto;
import com.example.gestiondeinventario.negocio.ProductoNegocio;

import java.util.List;

public class AdaptadorProducto extends RecyclerView.Adapter<AdaptadorProducto.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private OnProductoClickListener listener;
    private ProductoNegocio negocio;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    public AdaptadorProducto(List<Producto> listaProductos, OnProductoClickListener listener, ProductoNegocio negocio) {
        this.listaProductos = listaProductos;
        this.listener = listener;
        this.negocio = negocio;
    }

    public void actualizarLista(List<Producto> nuevaLista) {
        this.listaProductos = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.bind(producto, listener, negocio);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, skuCategoria, estado, stock;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.product_name);
            skuCategoria = itemView.findViewById(R.id.product_sku_category);
            estado = itemView.findViewById(R.id.product_status);
            stock = itemView.findViewById(R.id.product_stock);
        }

        public void bind(Producto producto, OnProductoClickListener listener, ProductoNegocio negocio) {
            nombre.setText(producto.getNombre());
            skuCategoria.setText("Código: " + producto.getCodigo() + " | " + producto.getCategoria());
            stock.setText(String.valueOf(producto.getStock()));

            String estadoTexto = negocio.calcularEstado(producto);
            estado.setText(estadoTexto);

            int color;
            if (estadoTexto.equals("Sin stock")) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.error);
            } else if (estadoTexto.equals("Stock bajo")) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.warning);
            } else {
                color = ContextCompat.getColor(itemView.getContext(), R.color.success);
            }
            estado.setTextColor(color);

            itemView.setOnClickListener(v -> listener.onProductoClick(producto));
        }
    }
}