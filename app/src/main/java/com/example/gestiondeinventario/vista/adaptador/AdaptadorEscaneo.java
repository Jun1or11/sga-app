package com.example.gestiondeinventario.vista.adaptador;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondeinventario.R;
import com.example.gestiondeinventario.modelo.Producto;

import java.util.List;
import java.util.Map;

public class AdaptadorEscaneo extends RecyclerView.Adapter<AdaptadorEscaneo.ViewHolder> {

    private List<Producto> listaProductos;
    private Map<Integer, Integer> cantidades;
    private OnEscaneoInteractionListener listener;

    public interface OnEscaneoInteractionListener {
        void onCantidadCambiada();
        void onItemClick(Producto producto);
    }

    public AdaptadorEscaneo(List<Producto> listaProductos, Map<Integer, Integer> cantidades, OnEscaneoInteractionListener listener) {
        this.listaProductos = listaProductos;
        this.cantidades = cantidades;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_escaneo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto producto = listaProductos.get(position);
        holder.nombre.setText(producto.getNombre());
        holder.precio.setText("S/ " + String.format("%.2f", producto.getPrecio()) + " c/u");

        int cantidad = cantidades.getOrDefault(producto.getId(), 1);
        holder.cantidadTextView.setText(String.valueOf(cantidad));

        holder.btnAgregar.setOnClickListener(v -> {
            cantidades.put(producto.getId(), cantidad + 1);
            notifyItemChanged(position);
            listener.onCantidadCambiada();
        });

        holder.btnQuitar.setOnClickListener(v -> {
            if (cantidad > 1) {
                cantidades.put(producto.getId(), cantidad - 1);
                notifyItemChanged(position);
            } else {
                int id = producto.getId();
                listaProductos.remove(position);
                cantidades.remove(id);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listaProductos.size());
            }
            listener.onCantidadCambiada();
        });

        holder.itemView.setOnClickListener(v -> listener.onItemClick(producto));
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, precio, cantidadTextView;
        View btnAgregar, btnQuitar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.item_name);
            precio = itemView.findViewById(R.id.item_price);
            cantidadTextView = itemView.findViewById(R.id.item_quantity);
            btnAgregar = itemView.findViewById(R.id.btn_add_quantity);
            btnQuitar = itemView.findViewById(R.id.btn_remove);
        }
    }
}