package cds.competencias;

import java.util.ArrayList;
import java.util.List;

import cds.conjunto.CategoriaEdad;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;

public class Liga extends Competencia {

	private int puntosVictoria;
	private int puntosEmpate;
	private List<Equipo> equiposTabla;
	private List<Integer> puntosTabla;

	public Liga(String nombre, String organizador, int temporada, DeporteConjunto deporte, CategoriaEdad categoria, int nominaMinima, int puntosVictoria, int puntosEmpate) {
		super(nombre, organizador, temporada, deporte, categoria, nominaMinima);
		this.puntosVictoria = puntosVictoria;
		this.puntosEmpate = puntosEmpate;
		this.equiposTabla = new ArrayList<Equipo>();
		this.puntosTabla = new ArrayList<Integer>();
	}

	@Override
	public void registrarResultado(PartidoCompetencia partido) {
		Equipo equipo = partido.getNomina().getEquipo();

		int puntosGanados = 0;
		if (partido.getMarcadorLocal() == partido.getMarcadorVisitante()) {
			puntosGanados = puntosEmpate;
		} else if (partido.getMarcadorLocal() > partido.getMarcadorVisitante()) {
			puntosGanados = puntosVictoria;
		}

		int posicion = equiposTabla.indexOf(equipo);
		if (posicion == -1) {
			equiposTabla.add(equipo);
			puntosTabla.add(puntosGanados);
		} else {
			puntosTabla.set(posicion, puntosTabla.get(posicion) + puntosGanados);
		}
	}

	public List<Equipo> getEquiposTabla() {
		return equiposTabla;
	}

	public int getPuntos(Equipo equipo) {
		int posicion = equiposTabla.indexOf(equipo);
		if (posicion == -1) {
			return 0;
		}
		return puntosTabla.get(posicion);
	}

	public void restaurarPuntos(Equipo equipo, int puntos) {
		int posicion = equiposTabla.indexOf(equipo);
		if (posicion == -1) {
			equiposTabla.add(equipo);
			puntosTabla.add(puntos);
		} else {
			puntosTabla.set(posicion, puntos);
		}
	}

	public int getPuntosVictoria() {
		return puntosVictoria;
	}

	public int getPuntosEmpate() {
		return puntosEmpate;
	}

}
