package cds.competencias;

import java.time.LocalDate;

import cds.ReglaNegocioException;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.partidos.EstadisticaFutbol;
import cds.partidos.EstadisticaIndividual;
import cds.partidos.Partido;
import cds.partidos.Suspension;
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
		nomina.agregarPartido(this);
	}
	
	@Override
	protected void validarParticipante(Socio s) throws ReglaNegocioException {
		if (!nomina.estaHabilitado(s)) {
			throw new ReglaNegocioException(s.getNombre() + " no esta en la nomina habilitada para esta competencia");
		}
		if(!s.estaHabilitado()) {
			throw new ReglaNegocioException(s.getNombre() + " no esta habilitado para jugar");
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
		Equipo equipo = nomina.getEquipo();
		equipo.cumplirSuspensiones();
		for (EstadisticaIndividual e : getEstadisticas()) {
			if (e instanceof EstadisticaFutbol) {
				EstadisticaFutbol f = (EstadisticaFutbol) e;
				int despues = nomina.amarillasAcumuladas(f.getJugador());
				int antes = despues - f.getAmarillas();
				if (f.generaSuspension() || antes / 2 < despues / 2) {
					String motivo = "amarillas";
					if (f.getRojas() >= 1) {
						motivo = "tarjeta roja";
					}
					equipo.agregarSuspension(new Suspension(f.getJugador(), motivo, this));
				}
			}
		}
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
