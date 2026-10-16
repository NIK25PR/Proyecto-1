package cds.usuarios;

import java.time.LocalDate;

import cds.ReglaNegocioException;

public class RestriccionMedica {
	private final Fisioterapeuta registradaPor;
	private final String motivo;
	private final LocalDate fechaInicio;
	private LocalDate fechaAlta;


	public RestriccionMedica(Fisioterapeuta registradaPor, String motivo, LocalDate fechaInicio) {
		this.registradaPor = registradaPor;
		this.motivo = motivo;
		this.fechaInicio = fechaInicio;
	}


	public boolean estaVigente() {
		return fechaAlta == null;
	}

	public void darDeAlta(LocalDate fecha) throws ReglaNegocioException {
		if (!estaVigente()) {
			throw new ReglaNegocioException("La restriccion ya fue dada de alta");
		}
		if (fecha.isBefore(fechaInicio)) {
			throw new ReglaNegocioException("La fecha de alta no puede ser anterior al inicio de la restriccion");
		}
		fechaAlta = fecha;
	}


	public Fisioterapeuta getRegistradaPor() {
		return registradaPor;
	}

	public String getMotivo() {
		return motivo;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public LocalDate getFechaAlta() {
		return fechaAlta;
	}

}
