package cds.ventas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import cds.ReglaNegocioException;
import cds.catalogo.Vendible;
import cds.usuarios.Usuario;

public abstract class Venta {
	public static final double PORCENTAJE_PUNTOS = 0.02;

	private final LocalDate fecha;
	private final Usuario comprador;
	private final List<LineaVenta> lineas;
	private int puntosRedimidos;
	private boolean confirmada;
	private boolean puntosProcesados;


	protected Venta(LocalDate fecha, Usuario comprador) {
		this.fecha = fecha;
		this.comprador = comprador;
		lineas = new ArrayList<LineaVenta>();
		puntosRedimidos = 0;
		confirmada = false;
		puntosProcesados = false;
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

	public double total() {
		return subtotal() - descuento() + impuesto() + cargosAdicionales() - puntosRedimidos;
	}

	public int puntosGenerados() {
		return (int) Math.floor(total() * PORCENTAJE_PUNTOS);
	}

	public void redimirPuntos(int puntos) throws ReglaNegocioException {
		if (confirmada) {
			throw new ReglaNegocioException("La venta ya fue confirmada");
		}
		if (puntos <= 0) {
			throw new ReglaNegocioException("Los puntos a redimir deben ser mayores que 0");
		}
		int nuevosPuntosRedimidos = puntosRedimidos + puntos;
		if (nuevosPuntosRedimidos > comprador.getPuntosFidelidad()) {
			throw new ReglaNegocioException("El comprador no tiene suficientes puntos");
		}
		if (nuevosPuntosRedimidos > subtotal() - descuento() + impuesto() + cargosAdicionales()) {
			throw new ReglaNegocioException("Los puntos no pueden superar el total de la compra");
		}
		puntosRedimidos = nuevosPuntosRedimidos;
	}

	public void confirmar() throws ReglaNegocioException {
		if (confirmada) {
			throw new ReglaNegocioException("La venta ya fue confirmada");
		}
		if (lineas.isEmpty()) {
			throw new ReglaNegocioException("La venta no tiene productos");
		}
		validarPuntosRedimidos();
		confirmada = true;
	}

	void procesarPuntosFidelidad() throws ReglaNegocioException {
		if (!confirmada) {
			throw new ReglaNegocioException("La venta debe confirmarse antes de registrarse");
		}
		if (puntosProcesados) {
			throw new ReglaNegocioException("Los puntos de esta venta ya fueron procesados");
		}
		validarPuntosRedimidos();
		int nuevosPuntos = puntosGenerados();
		if (puntosRedimidos > 0) {
			comprador.redimirPuntosFidelidad(puntosRedimidos);
		}
		comprador.acreditarPuntosFidelidad(nuevosPuntos);
		puntosProcesados = true;
	}

	void restaurarRegistro(int puntos, boolean estaConfirmada, boolean estanProcesados) {
		puntosRedimidos = puntos;
		confirmada = estaConfirmada;
		puntosProcesados = estanProcesados;
	}

	public boolean isPuntosProcesados() {
		return puntosProcesados;
	}

	private void validarPuntosRedimidos() throws ReglaNegocioException {
		if (puntosRedimidos > comprador.getPuntosFidelidad()) {
			throw new ReglaNegocioException("El comprador no tiene suficientes puntos");
		}
		if (puntosRedimidos > subtotal() - descuento() + impuesto() + cargosAdicionales()) {
			throw new ReglaNegocioException("Los puntos no pueden superar el total de la compra");
		}
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
		return lineas;
	}

	public int getPuntosRedimidos() {
		return puntosRedimidos;
	}

	public boolean isConfirmada() {
		return confirmada;
	}

}
