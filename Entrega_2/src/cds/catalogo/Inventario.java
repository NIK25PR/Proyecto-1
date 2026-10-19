package cds.catalogo;

import cds.ReglaNegocioException;

public class Inventario {
	private final Articulo articulo;
	private final Ubicacion ubicacion;
	private int cantidad;


	public Inventario(Articulo articulo, Ubicacion ubicacion, int cantidad) {
		this.articulo = articulo;
		this.ubicacion = ubicacion;
		this.cantidad = cantidad;
	}


	public void agregar(int unidades) throws ReglaNegocioException {
		if (unidades <= 0) {
			throw new ReglaNegocioException("Las unidades a agregar deben ser mayores que 0");
		}
		cantidad += unidades;
	}

	public void retirar(int unidades) throws ReglaNegocioException {
		if (unidades <= 0) {
			throw new ReglaNegocioException("Las unidades a retirar deben ser mayores que 0");
		}
		if (unidades > cantidad) {
			throw new ReglaNegocioException("No hay suficientes unidades: hay " + cantidad + " y se piden " + unidades);
		}
		cantidad -= unidades;
	}


	public Articulo getArticulo() {
		return articulo;
	}

	public Ubicacion getUbicacion() {
		return ubicacion;
	}

	public int getCantidad() {
		return cantidad;
	}

}
