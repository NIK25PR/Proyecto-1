package cds.ventas;

import cds.catalogo.Vendible;

/**
 * Un producto dentro de una venta. Guarda el precio del momento de la venta,
 * para que un cambio de precio posterior no altere ventas ya registradas.
 */
public class LineaVenta {
	private final Vendible producto;
	private final int cantidad;
	private final double precioUnitario;


	public LineaVenta(Vendible producto, int cantidad) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.precioUnitario = producto.getPrecio();
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
