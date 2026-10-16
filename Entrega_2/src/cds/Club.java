package cds;

import java.util.ArrayList;
import java.util.List;

import cds.usuarios.Usuario;
import cds.conjunto.Equipo;
import cds.partidos.Partido;

public class Club {
	private List<Usuario> usuarios;
	private List<Equipo> equipos;
	private List<Partido> partidos;
	
	public Club() {
		usuarios = new ArrayList<Usuario>();
		equipos = new ArrayList<Equipo>();
		partidos = new ArrayList<Partido>();
	}
	

}
