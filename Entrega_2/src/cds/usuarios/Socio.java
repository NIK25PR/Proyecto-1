package cds.usuarios;

import java.time.LocalDate;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Socio extends Usuario {
	private final String documento;
	private final LocalDate fechaNacimiento;
	private boolean activo;
	private final List<RestriccionMedica> restricciones;
	private boolean entrenador;


	public Socio(String login, String password, String nombre, String documento, LocalDate fechaNacimiento) {
		super(login, password, nombre);
		this.documento = documento;
		this.fechaNacimiento = fechaNacimiento;
		activo = true;
		restricciones = new ArrayList<RestriccionMedica>();
		this.entrenador = false;
	}


	public int getEdad(LocalDate hoy) {
		return Period.between(fechaNacimiento, hoy).getYears();
	}

	public boolean estaHabilitado() {
		if (!activo) {
			return false;
		}
		for (RestriccionMedica r : restricciones) {
			if (r.estaVigente()) {
				return false;
			}
		}
		return true;
	}

	public void agregarRestriccion(RestriccionMedica r) {
		restricciones.add(r);
	}

	public void darDeBaja() {
		activo = false;
	}
	
	public boolean esEntrenador() {
		return entrenador;
	}
	
	public void marcarComoEntrenador() {
		this.entrenador = true;
	}


	public String getDocumento() {
		return documento;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public boolean isActivo() {
		return activo;
	}

	public List<RestriccionMedica> getRestricciones() {
		return restricciones;
	}

}
