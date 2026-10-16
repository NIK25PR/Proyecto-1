package cds.usuarios;

public abstract class Usuario {
	private final String login;
	private String password;
	private String nombre;
	private int puntosFidelidad;


	protected Usuario(String login, String password, String nombre) {
		this.login = login;
		this.password = password;
		this.nombre = nombre;
		puntosFidelidad = 0;
	}


	public boolean verificarPassword(String intento) {
		return password.equals(intento);
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

}
