package cds.ventas;

import java.time.LocalDate;
import java.util.List;

import cds.ReglaNegocioException;
import cds.catalogo.Articulo;
import cds.catalogo.Inventario;
import cds.catalogo.Vendible;
import cds.usuarios.Empleado;
import cds.usuarios.Usuario;

public class VentaTienda extends Venta {
	public static final double IVA = 0.19;
	public static final double DESCUENTO_DUENO = 0.15;
	public static final double DESCUENTO_COMPARTIDO = 0.08;

	private Empleado duenoCodigo;


	public VentaTienda(LocalDate fecha, Usuario comprador) {
		super(fecha, comprador);
		duenoCodigo = null;
	}


	public void aplicarCodigo(Empleado dueno) throws ReglaNegocioException {
		if (duenoCodigo != null) {
			throw new ReglaNegocioException("La venta ya tiene un codigo de descuento");
		}
		duenoCodigo = dueno;
	}

	protected void validarProducto(Vendible producto) throws ReglaNegocioException {
		if (!(producto instanceof Articulo)) {
			throw new ReglaNegocioException(producto.getNombre() + " no se vende en la tienda");
		}
	}

	public double descuento() {
		if (duenoCodigo == null) {
			return 0;
		}
		if (getComprador() == duenoCodigo) {
			return subtotal() * DESCUENTO_DUENO;
		}
		return subtotal() * DESCUENTO_COMPARTIDO;
	}

	public double impuesto() {
		return (subtotal() - descuento()) * IVA;
	}

	public void descontarInventario(List<Inventario> inventarioTienda) throws ReglaNegocioException {
		for (LineaVenta linea : getLineas()) {
			Articulo articulo = (Articulo) linea.getProducto();

			Inventario encontrado = null;
			for (Inventario inv : inventarioTienda) {
				if (inv.getArticulo() == articulo) {
					encontrado = inv;
				}
			}

			if (encontrado == null) {
				throw new ReglaNegocioException(articulo.getNombre() + " no esta en el inventario de la tienda");
			}
			encontrado.retirar(linea.getCantidad());
		}
	}

	public Rubro getRubro() {
		return Rubro.TIENDA;
	}


	public Empleado getDuenoCodigo() {
		return duenoCodigo;
	}

}
