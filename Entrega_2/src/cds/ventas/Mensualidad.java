package cds.ventas;

import java.time.LocalDate;
import java.time.YearMonth;

import cds.ReglaNegocioException;
import cds.usuarios.Socio;

public class Mensualidad {
	private final Socio socio;
	private final YearMonth mes;
	private final double monto;
	private LocalDate fechaPago;


	public Mensualidad(Socio socio, YearMonth mes, double monto) {
		this.socio = socio;
		this.mes = mes;
		this.monto = monto;
		fechaPago = null;
	}


	public boolean isPagada() {
		return fechaPago != null;
	}

	public void pagar(LocalDate fecha) throws ReglaNegocioException {
		if (isPagada()) {
			throw new ReglaNegocioException("La mensualidad de " + mes + " ya fue pagada el " + fechaPago);
		}
		fechaPago = fecha;
	}


	public Socio getSocio() {
		return socio;
	}

	public YearMonth getMes() {
		return mes;
	}

	public double getMonto() {
		return monto;
	}

	public LocalDate getFechaPago() {
		return fechaPago;
	}

}
