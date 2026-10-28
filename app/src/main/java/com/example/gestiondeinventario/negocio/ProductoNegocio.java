package com.example.gestiondeinventario.negocio;

import com.example.gestiondeinventario.modelo.Producto;

public class ProductoNegocio {

    public String calcularEstado(int stock, int stockMinimo) {
        if (stock <= 0) return "Sin stock";
        if (stock <= stockMinimo) return "Stock bajo";
        return "Disponible";
    }

    public String calcularEstado(Producto producto) {
        return calcularEstado(producto.getStock(), producto.getStockMinimo());
    }

    public boolean esStockBajo(Producto producto) {
        return producto.getStock() > 0 && producto.getStock() <= producto.getStockMinimo();
    }

    public boolean esSinStock(Producto producto) {
        return producto.getStock() <= 0;
    }

    public boolean validarProducto(Producto producto) {
        return producto != null &&
                producto.getNombre() != null && !producto.getNombre().trim().isEmpty() &&
                producto.getCodigo() != null && !producto.getCodigo().trim().isEmpty() &&
                producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty() &&
                producto.getStock() >= 0 &&
                producto.getStockMinimo() >= 0 &&
                producto.getPrecio() >= 0;
    }

    public String validarCampos(String nombre, String codigo, String categoria, String stockStr, String stockMinStr, String precioStr) {
        if (nombre == null || nombre.trim().isEmpty()) return "El nombre es obligatorio";
        if (codigo == null || codigo.trim().isEmpty()) return "El código es obligatorio";
        if (categoria == null || categoria.trim().isEmpty()) return "La categoría es obligatoria";

        try {
            int stock = stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr);
            if (stock < 0) return "El stock no puede ser negativo";
        } catch (NumberFormatException e) {
            return "Stock inválido";
        }

        try {
            int stockMin = stockMinStr.isEmpty() ? 0 : Integer.parseInt(stockMinStr);
            if (stockMin < 0) return "El stock mínimo no puede ser negativo";
        } catch (NumberFormatException e) {
            return "Stock mínimo inválido";
        }

        try {
            double precio = precioStr.isEmpty() ? 0.0 : Double.parseDouble(precioStr);
            if (precio < 0) return "El precio no puede ser negativo";
        } catch (NumberFormatException e) {
            return "Precio inválido";
        }

        return null; // Sin errores
    }

    public double calcularValorInventario(Producto producto) {
        return producto.getStock() * producto.getPrecio();
    }

    public int calcularCantidadTotal(java.util.List<Producto> productos) {
        int total = 0;
        for (Producto p : productos) {
            total += p.getStock();
        }
        return total;
    }

    public double calcularValorTotal(java.util.List<Producto> productos) {
        double total = 0;
        for (Producto p : productos) {
            total += calcularValorInventario(p);
        }
        return total;
    }
}