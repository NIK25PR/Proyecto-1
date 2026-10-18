package cds.competencias;

import java.time.LocalDate;

import cds.ReglaNegocioException;
import cds.conjunto.DeporteConjunto;
import cds.partidos.Partido;
import cds.usuarios.Socio;


public class PartidoCompetencia extends Partido{
	
	private NominaCompetencia nomina;
	private String institucionRival;
	private String escenario;
	private DeporteConjunto deporte;
	private Fase fase;
	private Competencia competencia;
	
	public PartidoCompetencia(LocalDate fecha, NominaCompetencia nomina, String institucionRival, String escenario, DeporteConjunto deporte) {
		super(fecha);
		this.nomina = nomina;
		this.institucionRival = institucionRival;
		this.escenario = escenario;
		this.deporte = deporte;
	}
	
	@Override
	protected void validarParticipante(Socio s) throws ReglaNegocioException {
		if (!nomina.estaHabilitado(s)) {
			throw new ReglaNegocioException(s.getNombre() + " no esta en la nomina habilitada para esta competencia");
		}
		if(nomina.getEquipo().estaSuspendido(s)) {
			throw new ReglaNegocioException(s.getNombre() + " esta suspendido y no puede jugar este partido");
		}
	}
	
	@Override
	protected boolean permiteEmpate() {
		return deporte.permiteEmpate();
	}
	
	@Override
	protected void alFinalizar() {
		if(competencia != null) {
			competencia.registrarResultado(this);
		}
	}

	
	public void asignarFase(Fase fase) {
		this.fase = fase;
		fase.agregarPartido(this);
		
	}
	
	public NominaCompetencia getNomina() {
		return nomina;
		
	}
	
	public String getInstitucionRival() {
		return institucionRival;
		
	}
	
	public String getEscenarios() {
		return escenario;
		
	}
	
	public DeporteConjunto getDeporte() {
		return deporte;
		
	}
	
	public Fase getFase() {
		return fase;
		
	}
	
	public Competencia getComeptencia() {
		return competencia;
	}

}
