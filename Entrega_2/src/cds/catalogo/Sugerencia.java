package cds.catalogo;

import java.time.LocalDate;
import cds.ReglaNegocioException;
import cds.usuarios.Empleado;

public class Sugerencia {
	private final Empleado autor;
	private final String descripcion;
	private final LocalDate fecha;
	private EstadoSugerencia estado;


	public Sugerencia(Empleado autor, String descripcion, LocalDate fecha) {
		this.autor = autor;
		this.descripcion = descripcion;
		this.fecha = fecha;
		estado = EstadoSugerencia.PENDIENTE;
	}


	public void aprobar() throws ReglaNegocioException {
		if (estado != EstadoSugerencia.PENDIENTE) {
			throw new ReglaNegocioException("Solo se puede aprobar una sugerencia pendiente (estado actual: " + estado + ")");
		}
		estado = EstadoSugerencia.APROBADA;
	}

	public void rechazar() throws ReglaNegocioException {
		if (estado != EstadoSugerencia.PENDIENTE) {
			throw new ReglaNegocioException("Solo se puede rechazar una sugerencia pendiente (estado actual: " + estado + ")");
		}
		estado = EstadoSugerencia.RECHAZADA;
	}


	public Empleado getAutor() {
		return autor;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public EstadoSugerencia getEstado() {
		return estado;
	}

}
