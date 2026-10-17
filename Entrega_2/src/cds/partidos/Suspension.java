package cds.partidos;

import cds.usuarios.Socio;

public class Suspension {
	private final Socio socio;
	private final String motivo;
	private final Partido origen;
	private boolean cumplida;


	public Suspension(Socio socio, String motivo, Partido origen) {
		this.socio = socio;
		this.motivo = motivo;
		this.origen = origen;
		cumplida = false;
	}


	public void cumplir() {
		cumplida = true;
	}


	public Socio getSocio() {
		return socio;
	}

	public String getMotivo() {
		return motivo;
	}

	public Partido getOrigen() {
		return origen;
	}

	public boolean isCumplida() {
		return cumplida;
	}

}
