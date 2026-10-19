package cds.ventas;

import cds.catalogo.Vendible;

public class LineaVenta {
	private final Vendible producto;
	private final int cantidad;
	private final double precioUnitario;


	public LineaVenta(Vendible producto, int cantidad) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.precioUnitario = producto.getPrecio();
	}

	public LineaVenta(Vendible producto, int cantidad, double precioUnitario) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}


	public double subtotal() {
		return precioUnitario * cantidad;
	}


	public Vendible getProducto() {
		return producto;
	}

	public int getCantidad() {
		return cantidad;
	}

	public double getPrecioUnitario() {
		return precioUnitario;
	}

}
