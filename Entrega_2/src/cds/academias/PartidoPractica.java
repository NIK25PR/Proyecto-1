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
			if (inscripcionExiste != null && !inscripcionNuevo.esNivelCompatibleCon(inscripcionExiste)) {
				throw new ReglaNegocioException(
						"Los niveles de " + s.getNombre() + " y " + yaInscrito.getNombre() + " no son compatibles");
			}
		}
	}
	
	@Override
	protected boolean permiteEmpate() {
		return false;
		
	}
	
	public Academia getAcademia() {
		return academia;
		
	}
	
	public Modalidad getModalidad() {
		return modalidad;
	}

}
