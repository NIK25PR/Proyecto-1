package cds.usuarios;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class SolicitudCambioTurno {
	private final Empleado solicitante;
	private final Turno turno;
	private final DayOfWeek nuevoDia;
	private final LocalTime nuevaHoraInicio;
	private final LocalTime nuevaHoraFin;
	private EstadoSolicitud estado;
	private Administrador resueltaPor;


	public SolicitudCambioTurno(Empleado solicitante, Turno turno, DayOfWeek nuevoDia, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
		this.solicitante = solicitante;
		this.turno = turno;
		this.nuevoDia = nuevoDia;
		this.nuevaHoraInicio = nuevaHoraInicio;
		this.nuevaHoraFin = nuevaHoraFin;
		estado = EstadoSolicitud.PENDIENTE;
	}


	public void resolver(Administrador administrador, EstadoSolicitud nuevoEstado) {
		resueltaPor = administrador;
		estado = nuevoEstado;
	}


	public Empleado getSolicitante() {
		return solicitante;
	}

	public Turno getTurno() {
		return turno;
	}

	public DayOfWeek getNuevoDia() {
		return nuevoDia;
	}

	public LocalTime getNuevaHoraInicio() {
		return nuevaHoraInicio;
	}

	public LocalTime getNuevaHoraFin() {
		return nuevaHoraFin;
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	public Administrador getResueltaPor() {
		return resueltaPor;
	}

}
