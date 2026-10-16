package cds.conjunto;

import java.time.LocalDate;

import cds.usuarios.Socio;

public class MiembroEquipo {
	private final Socio socio;
	private final String posicion;
	private final int numeroCamiseta;
	private final LocalDate fechaIngreso;


	MiembroEquipo(Socio socio, String posicion, int numeroCamiseta, LocalDate fechaIngreso) {
		this.socio = socio;
		this.posicion = posicion;
		this.numeroCamiseta = numeroCamiseta;
		this.fechaIngreso = fechaIngreso;
	}


	public Socio getSocio() {
		return socio;
	}

	public String getPosicion() {
		return posicion;
	}

	public int getNumeroCamiseta() {
		return numeroCamiseta;
	}

	public LocalDate getFechaIngreso() {
		return fechaIngreso;
	}

}
