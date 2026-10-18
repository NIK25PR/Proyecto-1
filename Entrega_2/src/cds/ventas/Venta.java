package cds.ventas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cds.ReglaNegocioException;
import cds.catalogo.Vendible;
import cds.usuarios.Usuario;

/**
 * Venta generica. La tienda y la cafeteria cambian que productos aceptan,
 * el descuento, el impuesto y los cargos adicionales.
 */
public abstract class Venta {
	public static final double PORCENTAJE_PUNTOS = 0.02;

	private final LocalDate fecha;
	private final Usuario comprador;
	private final List<LineaVenta> lineas;
	private int puntosRedimidos;
	private boolean confirmada;


	protected Venta(LocalDate fecha, Usuario comprador) {
		this.fecha = fecha;
		this.comprador = comprador;
		lineas = new ArrayList<LineaVenta>();
		puntosRedimidos = 0;
		confirmada = false;
	}


	public void agregarLinea(Vendible producto, int cantidad) throws ReglaNegocioException {
		if (confirmada) {
			throw new ReglaNegocioException("La venta ya fue confirmada");
		}
		if (cantidad <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor que 0");
		}
		validarProducto(producto);
		lineas.add(new LineaVenta(producto, cantidad));
	}

	public double subtotal() {
		double suma = 0;
		for (LineaVenta l : lineas) {
			suma += l.subtotal();
		}
		return suma;
	}

	/**
	 * Valor final que paga el comprador.
	 */
	public double total() {
		return subtotal() - descuento() + impuesto() + cargosAdicionales() - puntosRedimidos;
	}

	/**
	 * Puntos de fidelidad que gana el comprador: 2% del valor (1 punto = 1 peso).
	 */
	public int puntosGenerados() {
		return (int) Math.floor(total() * PORCENTAJE_PUNTOS);
	}

	/**
	 * Usa puntos del comprador como descuento.
	 */
	public void redimirPuntos(int puntos) throws ReglaNegocioException {
		if (confirmada) {
			throw new ReglaNegocioException("La venta ya fue confirmada");
		}
		if (puntos <= 0) {
			throw new ReglaNegocioException("Los puntos a redimir deben ser mayores que 0");
		}
		if (puntosRedimidos + puntos > comprador.getPuntosFidelidad()) {
			throw new ReglaNegocioException("El comprador no tiene suficientes puntos");
		}
		puntosRedimidos += puntos;
	}

	public void confirmar() throws ReglaNegocioException {
		if (confirmada) {
			throw new ReglaNegocioException("La venta ya fue confirmada");
		}
		if (lineas.isEmpty()) {
			throw new ReglaNegocioException("La venta no tiene productos");
		}
		confirmada = true;
	}


	/**
	 * Solo para la persistencia: reconstruye una venta guardada sin volver a
	 * validar reglas que dependen del estado actual (precios, puntos del comprador).
	 */
	public void restaurar(List<LineaVenta> lineasGuardadas, int puntosRedimidos, boolean confirmada) {
		lineas.clear();
		lineas.addAll(lineasGuardadas);
		this.puntosRedimidos = puntosRedimidos;
		this.confirmada = confirmada;
	}


	protected abstract void validarProducto(Vendible producto) throws ReglaNegocioException;

	public abstract double descuento();

	public abstract double impuesto();

	public double cargosAdicionales() {
		return 0;
	}

	public abstract Rubro getRubro();


	public LocalDate getFecha() {
		return fecha;
	}

	public Usuario getComprador() {
		return comprador;
	}

	public List<LineaVenta> getLineas() {
		return Collections.unmodifiableList(lineas);
	}

	public int getPuntosRedimidos() {
		return puntosRedimidos;
	}

	protected void setPuntosRedimidos(int puntosRedimidos) {
		this.puntosRedimidos = puntosRedimidos;
	}

	public boolean isConfirmada() {
		return confirmada;
	}

}
