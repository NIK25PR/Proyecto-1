package cds.ventas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
import cds.academias.Instalacion;
import cds.catalogo.Alergeno;
import cds.catalogo.ProductoCafeteria;
import cds.catalogo.Vendible;
import cds.usuarios.Usuario;

public class VentaCafeteria extends Venta {
	public static final double IMPOCONSUMO = 0.08;
	public static final double PROPINA_SUGERIDA = 0.10;

	private double porcentajePropina;


	public VentaCafeteria(LocalDate fecha, Usuario comprador) {
		super(fecha, comprador);
		porcentajePropina = PROPINA_SUGERIDA;
	}


	protected void validarProducto(Vendible producto) throws ReglaNegocioException {
		if (!(producto instanceof ProductoCafeteria)) {
			throw new ReglaNegocioException(producto.getNombre() + " no se vende en la cafeteria");
		}
	}

	public void setPorcentajePropina(double porcentaje) throws ReglaNegocioException {
		if (porcentaje < 0) {
			throw new ReglaNegocioException("El porcentaje de propina no puede ser negativo");
		}
		porcentajePropina = porcentaje;
	}

	public List<Alergeno> alergenos() {
		List<Alergeno> resultado = new ArrayList<>();
		for (LineaVenta linea : getLineas()) {
			ProductoCafeteria producto = (ProductoCafeteria) linea.getProducto();
			for (Alergeno alergeno : producto.getAlergenos()) {
				if (!resultado.contains(alergeno)) {
					resultado.add(alergeno);
				}
			}
		}
		return resultado;
	}

	
	public void validarDespacho(Instalacion dondeEsta) throws ReglaNegocioException {
		if (!tieneBebidaCaliente()) {
			return;
		}
		if (dondeEsta == null) {
			throw new ReglaNegocioException("Debe indicar una instalacion para despachar una bebida caliente");
		}
		if (dondeEsta.restringeBebidaCaliente()) {
			throw new ReglaNegocioException("RN-20: no se despachan bebidas calientes en " + dondeEsta.getTipo());
		}
	}

	public boolean tieneBebidaCaliente() {
		for (LineaVenta linea : getLineas()) {
			ProductoCafeteria producto = (ProductoCafeteria) linea.getProducto();
			if (producto.esBebidaCaliente()) {
				return true;
			}
		}
		return false;
	}

	public double descuento() {
		return 0;
	}

	public double impuesto() {
		return subtotal() * IMPOCONSUMO;
	}

	public double cargosAdicionales() {
		return subtotal() * porcentajePropina;
	}

	public Rubro getRubro() {
		return Rubro.CAFETERIA;
	}


	public double getPorcentajePropina() {
		return porcentajePropina;
	}

}
