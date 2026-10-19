package cds.usuarios;

import cds.ReglaNegocioException;

public abstract class Usuario {
	private final String login;
	private String password;
	private String nombre;
	private int puntosFidelidad;
	private boolean tieneBebidaCaliente;


	protected Usuario(String login, String password, String nombre) {
		this.login = login;
		this.password = password;
		this.nombre = nombre;
		puntosFidelidad = 0;
		tieneBebidaCaliente = false;
	}


	public boolean verificarPassword(String intento) {
		return password.equals(intento);
	}


	public String getPassword() {
		return password;
	}

	public String getLogin() {
		return login;
	}

	public String getNombre() {
		return nombre;
	}

	public int getPuntosFidelidad() {
		return puntosFidelidad;
	}

	public void acreditarPuntosFidelidad(int puntos) {
		if (puntos > 0) {
			puntosFidelidad += puntos;
		}
	}

	public void redimirPuntosFidelidad(int puntos) throws ReglaNegocioException {
		if (puntos < 0 || puntos > puntosFidelidad) {
			throw new ReglaNegocioException("No hay suficientes puntos de fidelidad");
		}
		puntosFidelidad -= puntos;
	}

	public boolean tieneBebidaCaliente() {
		return tieneBebidaCaliente;
	}

	public void marcarBebidaCaliente() {
		tieneBebidaCaliente = true;
	}

	public void terminarBebidaCaliente() {
		tieneBebidaCaliente = false;
	}

}
