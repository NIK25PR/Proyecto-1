package cds.ventas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
import cds.academias.Instalacion;
import cds.academias.TipoInstalacion;
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

	/**
	 * El socio puede cambiar la propina sugerida.
	 */
	public void setPorcentajePropina(double porcentaje) throws ReglaNegocioException {
		if (porcentaje < 0) {
			throw new ReglaNegocioException("El porcentaje de propina no puede ser negativo");
		}
		porcentajePropina = porcentaje;
	}

	/**
	 * Alergenos de todos los productos de la venta, sin repetir,
	 * para informarlos antes de confirmar.
	 */
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
		if (dondeEsta == null) {
			return;
		}

		boolean tieneBebidaCaliente = false;
		for (LineaVenta linea : getLineas()) {
			ProductoCafeteria producto = (ProductoCafeteria) linea.getProducto();
			if (producto.esBebidaCaliente()) {
				tieneBebidaCaliente = true;
			}
		}

		if (tieneBebidaCaliente) {
			boolean esPadel = dondeEsta.getTipo() == TipoInstalacion.CANCHA_PADEL;
			boolean esTenisMesaTechada = dondeEsta.getTipo() == TipoInstalacion.MESA_TENIS_MESA
					&& dondeEsta.isTechada();
			if (esPadel || esTenisMesaTechada) {
				throw new ReglaNegocioException("RN-20: no se despachan bebidas calientes en " + dondeEsta.getTipo());
			}
		}
	}

	public double descuento() {
		return 0;
	}

	/**
	 * Impuesto al consumo del 8% sobre el subtotal.
	 */
	public double impuesto() {
		return subtotal() * IMPOCONSUMO;
	}

	/**
	 * Propina: porcentajePropina sobre el subtotal.
	 */
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
