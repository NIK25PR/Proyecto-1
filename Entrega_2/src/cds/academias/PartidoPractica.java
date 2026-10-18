package cds.academias;

import java.time.LocalDate;

import cds.ReglaNegocioException;
import cds.partidos.Partido;
import cds.usuarios.Socio;

public class PartidoPractica extends Partido {
	
	private Academia academia;
	private Modalidad modalidad;
	
	public PartidoPractica(LocalDate fecha, Academia academia, Modalidad modalidad) {
		super(fecha);
		this.academia = academia;
		this.modalidad = modalidad;
	}
	
	@Override
	protected void validarParticipante(Socio s) throws ReglaNegocioException {
		InscripcionAcademia inscripcionNuevo = academia.getInscripcion(s);
		if (inscripcionNuevo == null) {
			throw new ReglaNegocioException(s.getNombre() + " no esta inscrito en " + academia.getNombre());
		}
		for (Socio yaInscrito : getParticipantes()) {
			InscripcionAcademia inscripcionExiste = academia.getInscripcion(yaInscrito);
			boolean algunoEsEntrenador = s.esEntrenador() || yaInscrito.esEntrenador();
			if (inscripcionExiste != null && !algunoEsEntrenador && !inscripcionNuevo.esNivelCompatibleCon(inscripcionExiste)) {
				throw new ReglaNegocioException(
						"Los niveles de " + s.getNombre() + " y " + yaInscrito.getNombre() + " no son compatibles");
			}
		}
	}
	
	@Override
	protected boolean permiteEmpate() {
		return false;
		
	}
	
	@Override
	protected void alFinalizar() {
		if(getParticipantes().size()<2) {
			return ;
		}
		Socio jugador1 = getParticipantes().get(0);
		Socio jugador2 = getParticipantes().get(1);
		InscripcionAcademia inscripcion1 = academia.getInscripcion(jugador1);
		InscripcionAcademia inscripcion2 = academia.getInscripcion(jugador2);
		if(inscripcion1 == null || inscripcion2 == null) {
			return ;
		}
		
		double resultado1;
		if(getMarcadorLocal() == getMarcadorVisitante()) {
			resultado1 = 0.5;
		}else if(getMarcadorLocal() > getMarcadorVisitante()) {
			resultado1 = 1;
		}else {
			resultado1 = 0;
		}
		
		double resultado2 = 1.0 -resultado1;
		inscripcion1.actualizarPuntaje(resultado1, inscripcion2.getPuntajeRanking(), getFecha());
		inscripcion2.actualizarPuntaje(resultado2, inscripcion1.getPuntajeRanking(), getFecha());
		
		
	}
	
	public Academia getAcademia() {
		return academia;
		
	}
	
	public Modalidad getModalidad() {
		return modalidad;
	}

}
