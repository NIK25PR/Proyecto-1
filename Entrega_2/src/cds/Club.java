package cds;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cds.usuarios.Fisioterapeuta;
import cds.usuarios.RestriccionMedica;
import cds.usuarios.Socio;
import cds.usuarios.Usuario;
import cds.conjunto.DeporteConjunto;
import cds.conjunto.Equipo;
import cds.conjunto.PartidoInterno;
import cds.partidos.Partido;

public class Club {
	private final List<Usuario> usuarios;
	private final List<Equipo> equipos;
	private final List<Partido> partidos;

	public Club() {
		usuarios = new ArrayList<Usuario>();
		equipos = new ArrayList<Equipo>();
		partidos = new ArrayList<Partido>();
	}


	
	public void registrarUsuario(Usuario u) throws ReglaNegocioException {
		if (buscarUsuario(u.getLogin()) != null) {
			throw new ReglaNegocioException("Ya existe un usuario con el login " + u.getLogin());
		}
		usuarios.add(u);
	}

	public Usuario buscarUsuario(String login) {
		for (Usuario u : usuarios) {
			if (u.getLogin().equals(login)) {
				return u;
			}
		}
		return null;
	}

	
	
	public Usuario autenticar(String login, String password) throws ReglaNegocioException {
		Usuario u = buscarUsuario(login);
		if (u == null || !u.verificarPassword(password)) {
			throw new ReglaNegocioException("Login o password incorrectos");
		}
		return u;
	}

	public void registrarEquipo(Equipo e) throws ReglaNegocioException {
		for (Equipo actual : equipos) {
			if (actual.getNombre().equals(e.getNombre())) {
				throw new ReglaNegocioException("Ya existe un equipo llamado " + e.getNombre());
			}
		}
		equipos.add(e);
	}

	public Equipo buscarEquipoDe(Socio s, DeporteConjunto d) {
		for (Equipo e : equipos) {
			if (e.getDeporte() == d && e.tieneJugador(s)) {
				return e;
			}
		}
		return null;
	}

	public void inscribirJugador(Equipo e, Socio s, String posicion, int numero, LocalDate hoy) throws ReglaNegocioException {
		Equipo actual = buscarEquipoDe(s, e.getDeporte());
		if (actual != null) {
			throw new ReglaNegocioException(s.getNombre() + " ya juega " + e.getDeporte().getNombre() + " en " + actual.getNombre());
		}
		e.agregarJugador(s, posicion, numero, hoy);
	}

	public PartidoInterno registrarPartidoInterno(Equipo local, Equipo visitante, LocalDate fecha) throws ReglaNegocioException {
		PartidoInterno p = new PartidoInterno(local, visitante, fecha);
		
		partidos.add(p);
		return p;
	}

	public void registrarRestriccion(Fisioterapeuta fisio, Socio s, String motivo, LocalDate fecha) {
		s.agregarRestriccion(new RestriccionMedica(fisio, motivo, fecha));
	}


	public List<Usuario> getUsuarios() {
		return Collections.unmodifiableList(usuarios);
	}
	

	public List<Equipo> getEquipos() {
		return Collections.unmodifiableList(equipos);
	}

	public List<Partido> getPartidos() {
		return Collections.unmodifiableList(partidos);
	}

}
