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
		int maximo = 2;
		if (modalidad == Modalidad.DOBLES) {
			maximo = 4;
		}
		if (getParticipantes().size() >= maximo) {
			throw new ReglaNegocioException("El partido de " + modalidad + " admite maximo " + maximo + " jugadores");
		}
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
	public void finalizar(int local, int visitante) throws ReglaNegocioException {
		int jugadoresPorLado = modalidad == Modalidad.DOBLES ? 2 : 1;
		int esperados = jugadoresPorLado * 2;
		if (getParticipantes().size() != esperados) {
			throw new ReglaNegocioException("El partido de " + modalidad + " necesita " + esperados + " jugadores");
		}
		super.finalizar(local, visitante);
	}

	@Override
	protected void alFinalizar() {
		int jugadoresPorLado = modalidad == Modalidad.DOBLES ? 2 : 1;
		double puntajeLocal = 0;
		double puntajeVisitante = 0;
		for (int i = 0; i < jugadoresPorLado; i++) {
			puntajeLocal += academia.getInscripcion(getParticipantes().get(i)).getPuntajeRanking();
			puntajeVisitante += academia.getInscripcion(getParticipantes().get(i + jugadoresPorLado)).getPuntajeRanking();
		}
		puntajeLocal = puntajeLocal / jugadoresPorLado;
		puntajeVisitante = puntajeVisitante / jugadoresPorLado;
		double resultadoLocal = getMarcadorLocal() > getMarcadorVisitante() ? 1 : 0;
		double resultadoVisitante = 1 - resultadoLocal;
		for (int i = 0; i < jugadoresPorLado; i++) {
			InscripcionAcademia local = academia.getInscripcion(getParticipantes().get(i));
			InscripcionAcademia visitante = academia.getInscripcion(getParticipantes().get(i + jugadoresPorLado));
			local.actualizarPuntaje(resultadoLocal, puntajeVisitante, getFecha());
			visitante.actualizarPuntaje(resultadoVisitante, puntajeLocal, getFecha());
		}
	}
	
	public Academia getAcademia() {
		return academia;
		
	}
	
	public Modalidad getModalidad() {
		return modalidad;
	}

}
