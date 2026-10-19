package cds.ventas;

import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
import cds.academias.Instalacion;
import cds.catalogo.Articulo;
import cds.catalogo.Inventario;
import cds.catalogo.ProductoCafeteria;
import cds.catalogo.Sugerencia;
import cds.catalogo.Ubicacion;

public class TiendaCafeteria {
	private final List<Ubicacion> ubicaciones;
	private final List<Articulo> articulos;
	private final List<ProductoCafeteria> productos;
	private final List<Inventario> inventarios;
	private final List<Sugerencia> sugerencias;
	private final List<Venta> ventas;
	private final List<Mensualidad> mensualidades;


	public TiendaCafeteria() {
		ubicaciones = new ArrayList<Ubicacion>();
		articulos = new ArrayList<Articulo>();
		productos = new ArrayList<ProductoCafeteria>();
		inventarios = new ArrayList<Inventario>();
		sugerencias = new ArrayList<Sugerencia>();
		ventas = new ArrayList<Venta>();
		mensualidades = new ArrayList<Mensualidad>();
	}


	public void registrarUbicacion(Ubicacion u) throws ReglaNegocioException {
		if (buscarUbicacion(u.getNombre()) != null) {
			throw new ReglaNegocioException("Ya existe la ubicacion " + u.getNombre());
		}
		ubicaciones.add(u);
	}

	public void registrarArticulo(Articulo a) throws ReglaNegocioException {
		if (buscarArticulo(a.getNombre()) != null) {
			throw new ReglaNegocioException("Ya existe el articulo " + a.getNombre());
		}
		articulos.add(a);
	}

	public void registrarProducto(ProductoCafeteria p) throws ReglaNegocioException {
		if (buscarProducto(p.getNombre()) != null) {
			throw new ReglaNegocioException("Ya existe el producto " + p.getNombre());
		}
		productos.add(p);
	}

	public void registrarInventario(Inventario i) throws ReglaNegocioException {
		if (buscarInventario(i.getArticulo(), i.getUbicacion()) != null) {
			throw new ReglaNegocioException("Ya hay inventario de " + i.getArticulo().getNombre()
					+ " en " + i.getUbicacion().getNombre());
		}
		inventarios.add(i);
	}

	public void registrarSugerencia(Sugerencia s) {
		sugerencias.add(s);
	}

	public void registrarVenta(Venta v) throws ReglaNegocioException {
		if (!v.isConfirmada()) {
			throw new ReglaNegocioException("Solo se registran ventas confirmadas");
		}
		if (v instanceof VentaCafeteria && ((VentaCafeteria) v).tieneBebidaCaliente()) {
			throw new ReglaNegocioException("Debe indicar una instalacion para despachar una bebida caliente");
		}
		v.procesarPuntosFidelidad();
		ventas.add(v);
	}

	public void registrarVenta(VentaCafeteria v, Instalacion dondeEsta) throws ReglaNegocioException {
		if (!v.isConfirmada()) {
			throw new ReglaNegocioException("Solo se registran ventas confirmadas");
		}
		v.validarDespacho(dondeEsta);
		v.procesarPuntosFidelidad();
		ventas.add(v);
		if (v.tieneBebidaCaliente()) {
			v.getComprador().marcarBebidaCaliente();
		}
	}

	public void restaurarVenta(Venta v, int puntosRedimidos, boolean confirmada, boolean puntosProcesados) {
		v.restaurarRegistro(puntosRedimidos, confirmada, puntosProcesados);
		ventas.add(v);
	}

	public void registrarMensualidad(Mensualidad m) {
		mensualidades.add(m);
	}


	public void trasladar(Articulo articulo, Ubicacion origen, Ubicacion destino, int unidades)
			throws ReglaNegocioException {
		if (origen == destino) {
			throw new ReglaNegocioException("El origen y el destino son la misma ubicacion");
		}
		Inventario desde = buscarInventario(articulo, origen);
		if (desde == null) {
			throw new ReglaNegocioException("No hay " + articulo.getNombre() + " en " + origen.getNombre());
		}
		desde.retirar(unidades);

		Inventario hacia = buscarInventario(articulo, destino);
		if (hacia == null) {
			hacia = new Inventario(articulo, destino, 0);
			registrarInventario(hacia);
		}
		hacia.agregar(unidades);
	}


	public Ubicacion buscarUbicacion(String nombre) {
		for (Ubicacion u : ubicaciones) {
			if (u.getNombre().equals(nombre)) {
				return u;
			}
		}
		return null;
	}

	public Articulo buscarArticulo(String nombre) {
		for (Articulo a : articulos) {
			if (a.getNombre().equals(nombre)) {
				return a;
			}
		}
		return null;
	}

	public ProductoCafeteria buscarProducto(String nombre) {
		for (ProductoCafeteria p : productos) {
			if (p.getNombre().equals(nombre)) {
				return p;
			}
		}
		return null;
	}

	public Inventario buscarInventario(Articulo articulo, Ubicacion ubicacion) {
		for (Inventario i : inventarios) {
			if (i.getArticulo() == articulo && i.getUbicacion() == ubicacion) {
				return i;
			}
		}
		return null;
	}

	public List<Inventario> inventariosEn(Ubicacion ubicacion) {
		List<Inventario> resultado = new ArrayList<Inventario>();
		for (Inventario i : inventarios) {
			if (i.getUbicacion() == ubicacion) {
				resultado.add(i);
			}
		}
		return resultado;
	}


	public List<Ubicacion> getUbicaciones() {
		return ubicaciones;
	}

	public List<Articulo> getArticulos() {
		return articulos;
	}

	public List<ProductoCafeteria> getProductos() {
		return productos;
	}

	public List<Inventario> getInventarios() {
		return inventarios;
	}

	public List<Sugerencia> getSugerencias() {
		return sugerencias;
	}

	public List<Venta> getVentas() {
		return ventas;
	}

	public List<Mensualidad> getMensualidades() {
		return mensualidades;
	}

}
