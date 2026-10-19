package cds.usuarios;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Turno {
	private final Empleado empleado;
	private DayOfWeek dia;
	private LocalTime horaInicio;
	private LocalTime horaFin;


	public Turno(Empleado empleado, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
		this.empleado = empleado;
		this.dia = dia;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}


	public boolean cubre(DayOfWeek otroDia, LocalTime hora) {
		return dia == otroDia && !horaInicio.isAfter(hora) && !horaFin.isBefore(hora.plusHours(1));
	}

	public void cambiar(DayOfWeek nuevoDia, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
		dia = nuevoDia;
		horaInicio = nuevaHoraInicio;
		horaFin = nuevaHoraFin;
	}


	public Empleado getEmpleado() {
		return empleado;
	}

	public DayOfWeek getDia() {
		return dia;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

}
