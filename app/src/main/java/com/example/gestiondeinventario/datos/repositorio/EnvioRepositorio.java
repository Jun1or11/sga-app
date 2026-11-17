package com.example.gestiondeinventario.datos.repositorio;

import android.content.Context;

import com.example.gestiondeinventario.datos.dao.EmpresaDao;
import com.example.gestiondeinventario.datos.dao.EnvioDao;
import com.example.gestiondeinventario.modelo.Empresa;
import com.example.gestiondeinventario.modelo.Envio;
import com.example.gestiondeinventario.modelo.EnvioDetalle;
import com.example.gestiondeinventario.util.SesionActual;

import java.util.List;

/**
 * Fachada del módulo de envíos: empresas y despachos del almacén.
 */
public class EnvioRepositorio {

    private EmpresaDao empresaDao;
    private EnvioDao envioDao;
    private Context context;

    public EnvioRepositorio(Context context) {
        this.context = context.getApplicationContext();
        empresaDao = new EmpresaDao(this.context);
        empresaDao.abrir();
        envioDao = new EnvioDao(this.context);
        envioDao.abrir();
    }

    public void cerrar() {
        if (empresaDao != null) empresaDao.cerrar();
        if (envioDao != null) envioDao.cerrar();
    }

    // Empresas --------------------------------------------------------

    public List<Empresa> obtenerEmpresas() {
        return empresaDao.obtenerTodas();
    }

    public long agregarEmpresa(String nombre, String ruc, String telefono, String contacto) {
        if (nombre == null || nombre.trim().isEmpty()) return -1;
        Empresa empresa = new Empresa(0, nombre.trim(), ruc, telefono, contacto, true);
        return empresaDao.insertar(empresa);
    }

    // Envíos ----------------------------------------------------------

    public EnvioDao.ResultadoCreacion crearEnvio(int empresaId, String observaciones,
                                                 List<EnvioDetalle> detalles) {
        return envioDao.crearEnvio(empresaId, observaciones,
                SesionActual.getNombre(context), SesionActual.getTurno(context), detalles);
    }

    public List<Envio> obtenerTodos(String estado) {
        return envioDao.obtenerTodos(estado);
    }

    public Envio obtenerPorId(int id) {
        return envioDao.obtenerPorId(id);
    }

    public List<EnvioDetalle> obtenerDetalles(int envioId) {
        return envioDao.obtenerDetalles(envioId);
    }

    public boolean anularEnvio(int envioId) {
        return envioDao.anularEnvio(envioId,
                SesionActual.getNombre(context), SesionActual.getTurno(context));
    }

    public int contarDesde(long desde) {
        return envioDao.contarDesde(desde);
    }
}
